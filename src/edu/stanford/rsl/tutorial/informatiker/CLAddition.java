package edu.stanford.rsl.tutorial.informatiker;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;


public class CLAddition {
	
	MyPhantom phantom;
	
	public CLAddition(MyPhantom phantom) {
		this.phantom = phantom;
		init();
	}
	
	private void init() {

		// create context and device
		CLContext context = OpenCLUtil.createContext();
		CLDevice device = context.getMaxFlopsDevice();
		System.out.println(device.toString());
		
		// set work sizes
		int gridSize = phantom.getSize()[0] * phantom.getSize()[1];
//		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 8); // Local work size dimensions
//		int globalWorkSizeT = OpenCLUtil.roundUp(localWorkSize, 512); // rounded up to the nearest multiple of localWorkSize
//		int globalWorkSizeBeta = OpenCLUtil.roundUp(localWorkSize, 512); // rounded up to the nearest multiple of localWorkSize

		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 32); // Local work size dimensions
		int globalWorkSizeT = OpenCLUtil.roundUp(localWorkSize, phantom.getSize()[0]); // rounded up to the nearest multiple of localWorkSize
		int globalWorkSizeBeta = OpenCLUtil.roundUp(localWorkSize, phantom.getSize()[1]); // rounded up to the nearest multiple of localWorkSize
		//WORKSIZE???????
		
		// load sources, create and build program
		CLProgram program = null;
		try {
			program = context.createProgram(this.getClass().getResourceAsStream("KernelAdd.cl"))
					.build();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		CLBuffer<FloatBuffer> imageBuffer = context.createFloatBuffer(gridSize, Mem.READ_ONLY);	
		for (int i=0;i<phantom.getSize()[1];++i){
			for (int j=0;j<phantom.getSize()[0];++j)
				imageBuffer.getBuffer().put(phantom.getAtIndex(j, i));
		}
		imageBuffer.getBuffer().rewind();

		
		// create memory for result
		CLBuffer<FloatBuffer> result = context.createFloatBuffer(gridSize, Mem.WRITE_ONLY);

		// copy params
		CLKernel kernel = program.createCLKernel("add2DGrid");
		kernel.putArg(imageBuffer).putArg(result)
			  .putArg(gridSize);

		/* GPU --- TIME MEASURING */
		CLCommandQueue queue = device.createCommandQueue();
		long t0 = System.nanoTime();
		// createCommandQueue
		
		for(int i = 0; i < 1; i++) {

			queue
				.putWriteBuffer(imageBuffer, true)
				.finish()
				.put2DRangeKernel(kernel, 0, 0, globalWorkSizeBeta, globalWorkSizeT,
						localWorkSize, localWorkSize).putBarrier()
				.putReadBuffer(result, true)
				.finish();
		}
		long t1 = System.nanoTime();
		long diff = t1 - t0;
		long ns = diff % 1000;
		long us = diff / 1000;
		long ms = us / 1000;
		us %= 1000;
		long s = ms / 1000;
		ms %= 1000;
		System.out.println("GPU: Took "+s+"s "+ms+"ms "+us+"us "+ns+"ns");

		
		// write result back to grid2D
		Grid2D res = new Grid2D(new float[gridSize], phantom.getSize()[0], phantom.getSize()[1]);
		res.setSpacing(phantom.getSpacing()[0], phantom.getSpacing()[1]);
		result.getBuffer().rewind();
		for (int i = 0; i < res.getBuffer().length; ++i) {
			res.getBuffer()[i] = result.getBuffer().get();
	}

		// clean up
		queue.release();
		imageBuffer.release();
		result.release();
		kernel.release();
		program.release();
		context.release();
		
		res.show();
		
		// alternative approach
//		OpenCLGrid2D phantomCL = new OpenCLGrid2D(phantom);
//		OpenCLMemoryDelegate delegate = phantomCL.getDelegate();
//		CLDevice device = phantomCL.getDelegate().getCLDevice(); 
//		System.out.println(device.toString());
//		delegate.prepareForDeviceOperation();
//		delegate.prepareForHostOperation();

		
		
		return;
		
	}

}
