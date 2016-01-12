package edu.stanford.rsl.tutorial.informatiker;

import java.io.IOException;
import java.nio.FloatBuffer;

import mdbtools.dbengine.tasks.FilterData;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import edu.stanford.rsl.conrad.data.numeric.*;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;


public class FilteredBackProjector extends Grid2D {
	
	Grid2D sinogram;
	Grid2D filteredData;
	Grid1DComplex filter;
	
	double epsilon = 0.0001;

	public FilteredBackProjector(int size_x, int size_y, double spacing_x, double spacing_y, Grid2D sinogram, Grid1DComplex filter) {
		
		super(size_x, size_y);
		setSpacing(spacing_x, spacing_y);
		setOrigin(-(size_x-1)*spacing_x*0.5, -(size_y-1)*spacing_x*0.5);
		
		this.sinogram = sinogram;
		this.filter = filter;
		filteredData = new Grid2D(sinogram.getSize()[0], sinogram.getSize()[1]);
		filteredData.setSpacing(sinogram.getSpacing());
		filteredData.setOrigin(sinogram.getOrigin());
				
		filter();
//		filteredData.show("filteredData");
//		backproject();
		
		long t0 = System.nanoTime();
		
//		backproject();
		
		long t1 = System.nanoTime();
		long diff = t1 - t0;
		long time1 = diff;
		long ns = diff % 1000;
		long us = diff / 1000;
		long ms = us / 1000;
		us %= 1000;
		long s = ms / 1000;
		ms %= 1000;
		System.out.println("Backprojection CPU : Took "+s+"s "+ms+"ms "+us+"us "+ns+"ns");
		
		t0 = System.nanoTime();
		
		backprojectOpenCL();
		
		t1 = System.nanoTime();
		diff = t1 - t0;
		long time2 = diff;
		ns = diff % 1000;
		us = diff / 1000;
		ms = us / 1000;
		us %= 1000;
		s = ms / 1000;
		ms %= 1000;
		System.out.println("Backprojection GPU: Took "+s+"s "+ms+"ms "+us+"us "+ns+"ns");
		System.out.println("Speedup: " + time1/time2);
	}
	
	
	void filter() {
		
		
		// iterate over all projections (theta) 
		for(int i = 0; i < sinogram.getSize()[1]; i++) {
			
			Grid1DComplex detectorRow = new Grid1DComplex(sinogram.getSubGrid(i));
			detectorRow.transformForward();
			
			// apply filter
			for( int j = 0; j < detectorRow.getSize()[0]; j++ ) {
				
				// Apply filtering 
				// Complex Multiplication
				detectorRow.setRealAtIndex(j, detectorRow.getRealAtIndex(j) * filter.getRealAtIndex(j) - detectorRow.getImagAtIndex(j) * filter.getImagAtIndex(j));
				detectorRow.setImagAtIndex(j, detectorRow.getRealAtIndex(j) * filter.getImagAtIndex(j) + detectorRow.getImagAtIndex(j) * filter.getRealAtIndex(j));
				
				// do not filter
				//detectorRow.setAtIndex( j, detectorRow.getRealAtIndex(j) );
				
			}
			
			detectorRow.transformInverse();
			
			// set at filteredData  --- something like set subGrid?
			for( int j = 0; j < detectorRow.getSize()[0]; j++ ) {
			
				filteredData.setAtIndex(j,i,detectorRow.getRealAtIndex(j));
				
			}
		}
	}
	
	
	void backproject() {
		
		// for all rows in phantom
		for( int i = 0; i < this.getWidth(); ++i ) {
			
			// for all cols in phantom
			for( int j = 0; j < this.getHeight(); ++j ) {
				
				float detector_value = 0f;
				
				// for all angles theta
				// calculate the detector position and add up the values
				for( int k = 0; k < sinogram.getHeight(); ++k ) {
					
					double theta = filteredData.indexToPhysical(0, k)[1];
					if(theta >= Math.PI) theta %= Math.PI;
					
					// calculate intersection between detector line d=(0,0)+a(cos(theta),-sin(theta))
					// and line through point p othorgonal to d
					// s = xcos(theta)-sin(theta) (cos(theta), -sin(theta)
					double val = indexToPhysical(i, j)[0] * Math.cos(theta) + indexToPhysical(i, j)[1] * Math.sin(theta);
					double s_x = val * Math.cos(theta);
					double s_y = val * Math.sin(theta);
					double s = Math.sqrt(s_x*s_x + s_y*s_y);
					// correct sign of s		
					if(Math.abs(Math.signum(s_y)) != 0 ) {
						s = s * Math.signum(s_y);
					} else if( Math.abs(Math.signum(s_x)) != 0 ) {
						s = s * Math.signum(s_x);
					}

					// Bilinear interpolation --> linear interpolation is sufficient
					double[] pos = filteredData.physicalToIndex(s, theta);
					detector_value += InterpolationOperators.interpolateLinear(filteredData, pos[0], pos[1]);
					
				}

				this.setAtIndex(i, j, detector_value);
			}
		}
	}
	
	private void backprojectOpenCL() {

		// create context and device
		CLContext context = OpenCLUtil.createContext();
		CLDevice device = context.getMaxFlopsDevice();
		System.out.println(device.toString());

		// set work sizes
		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 8); // Local work size dimensions
		int globalWorkSizeX = OpenCLUtil.roundUp(localWorkSize, this.getWidth()); // rounded up to the nearest multiple of localWorkSize
		int globalWorkSizeY = OpenCLUtil.roundUp(localWorkSize, this.getHeight()); // rounded up to the nearest multiple of localWorkSize
//		int globalWorkSizeZ = OpenCLUtil.roundUp(localWorkSize, sinogram.getHeight()); // rounded up to the nearest multiple of localWorkSize
			
		// load sources, create and build program
		CLProgram program = null;
		try {
			program = context.createProgram(this.getClass().getResourceAsStream("KernelAdd.cl"))
					.build();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		//write filterData to buffer
		CLBuffer<FloatBuffer> imageBuffer = context.createFloatBuffer(filteredData.getSize()[0]*filteredData.getSize()[1], Mem.READ_ONLY);	
		for (int i=0;i<filteredData.getSize()[1];++i){
			for (int j=0;j<filteredData.getSize()[0];++j)
				imageBuffer.getBuffer().put(filteredData.getAtIndex(j, i));
		}
		imageBuffer.getBuffer().rewind();

		// create memory for result
		CLBuffer<FloatBuffer> result = context.createFloatBuffer(this.getSize()[0]*this.getSize()[1], Mem.WRITE_ONLY);
//		result.getBuffer().put(new float[]{this.getSize()[0]*this.getSize()[1]});
//		result.getBuffer().rewind();

		// copy params
		CLKernel kernel = program.createCLKernel("backproject");
		kernel.putArg(imageBuffer).putArg(result)
			  .putArg(this.getSize()[0]).putArg(this.getSize()[1])
			  .putArg(filteredData.getSize()[0]).putArg(filteredData.getSize()[1])
			  .putArg((float)this.getSpacing()[0]).putArg((float)this.getSpacing()[1])
			  .putArg((float)this.getOrigin()[0]).putArg((float)this.getOrigin()[1])
			  .putArg((float)sinogram.getSpacing()[0]).putArg((float)sinogram.getSpacing()[1])
			  .putArg((float)sinogram.getOrigin()[0]);

		/* GPU --- TIME MEASURING */
		CLCommandQueue queue = device.createCommandQueue();

		// createCommandQueue
		
		queue
			.putWriteBuffer(imageBuffer, true)
			.finish()
//			.put3DRangeKernel(kernel, 0, 0, 0, globalWorkSizeX, globalWorkSizeY, globalWorkSizeZ, localWorkSize,
//					localWorkSize, localWorkSize).putBarrier()
			.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSize,
					localWorkSize).putBarrier()
			.putReadBuffer(result, true)
			.finish();
			
				
		// write result back
		result.getBuffer().rewind();
		for (int i = 0; i < this.getBuffer().length; ++i) {
			this.getBuffer()[i] = result.getBuffer().get();
		}

		// clean up
		queue.release();
		imageBuffer.release();
		result.release();
		kernel.release();
		program.release();
		context.release();
			
		return;
		
	}
	
}