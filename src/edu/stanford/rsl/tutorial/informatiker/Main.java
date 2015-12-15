package edu.stanford.rsl.tutorial.informatiker;


import com.jogamp.opencl.*;

import edu.emory.mathcs.utils.IOUtils;
import edu.stanford.rsl.conrad.data.OpenCLMemoryDelegate;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import ij.ImageJ;




public class Main {
	public static void main( String[] args ) {
		
		// PHANTOM SIZE
		int phantomWidth = 300;
		int phantomHeight = 300;
		double phantomSpacingX = 1.0;
		double phantomSpacingY = 1.0;
		
		int numProjections = 360;
		int numDetectorPixels = 512;
		double detectorSpacing = 1.0;
		
		
		// BACKPROJECTION SIZE
		int backprojectSizeX = 600;
		int backprojectSizeY = 600;
		double backprojectSpacingX = 1.0;
		double backprojectSpacingY = 1.0;

		
		
		new ImageJ();
		MyPhantom phantom = new MyPhantom( phantomWidth, phantomHeight, phantomSpacingX, phantomSpacingY );
		phantom.show("Phantom");
		
		private static String programSource =
			    "__kernel void "+
			    "sampleKernel(__global const float *a,"+
			    "             __global const float *b,"+
			    "             __global float *c)"+
			    "{"+
			    "    int gid = get_global_id(0);"+
			    "    c[gid] = a[gid] + b[gid];"+
			    "}";

		
		long t0 = System.nanoTime();
		for (int i=0; i < 100; i++) {
			//NumericPointwiseOperators.addedBy(phantom, phantom);
		}
		long t1 = System.nanoTime();
		long diff = t1 - t0;
		long ns = diff % 1000;
		long us = diff / 1000;
		long ms = us / 1000;
		us %= 1000;
		long s = ms / 1000;
		ms %= 1000;
		System.out.println("CPU: Took "+s+"s "+ms+"ms "+us+"us "+ns+"ns");
		
		OpenCLGrid2D phantomCL = new OpenCLGrid2D(phantom);
		OpenCLMemoryDelegate delegate = phantomCL.getDelegate();
		
		delegate.prepareForDeviceOperation();
		
		// DO STUFF
		CLBuffer buf = delegate.getCLBuffer();
		
        String src = IOUtils.readText(Informatiker.class.getResource("KernelAdd.cl"));
        CLProgram program = context.createProgram(src);

		
		CLKernel addFloatsKernel = program.createKernel("add_floats");

		delegate.prepareForHostOperation();
		
		return;
		
		/* PARALLEL BEAM RECONSTRUCTION */
		MyDetector detector = new MyDetector( numProjections, detectorSpacing, numDetectorPixels, phantom );

		/* RAMPFILTER --- Filter defined in Frequency Domain */
		RampFilter rampfilter = new RampFilter( numDetectorPixels, detectorSpacing );

		FilteredBackProjector filterbackprojector = new FilteredBackProjector(backprojectSizeX, backprojectSizeY, backprojectSpacingX, backprojectSpacingY, detector, rampfilter);
		
		/* RAMLAKFILTER --- Filter defined in Spatial Domain */
//		RamLak rampfilter_spatial = new RamLak( numDetectorPixels, detectorSpacing );

//		FilteredBackProjector filterbackprojector = new FilteredBackProjector(backprojectSizeX, backprojectSizeY, backprojectSpacingX, backprojectSpacingY, detector, rampfilter_spatial);

		// SHOW RESULTS
//		detector.show("Sinogram Original");
//		rampfilter.getRealSubGrid(0, rampfilter.getSize()[0]).show("RampFilter");
		filterbackprojector.show("Backprojection_ramp");
//		rampfilter_spatial.getMagSubGrid(0, rampfilter_spatial.getSize()[0]).show("Ramlak");
//		filterbackprojector.show("Backprojection_ramlak");
		
		/* CONE BEAM RECONSTRUCTION */
		double rotationAngleIncrement = 1.0 * Math.PI / 180.0;
		double dSI = 800;
		double dSD = 1200;

		FanBeamDetector fanbeamdetector = new FanBeamDetector( numProjections, detectorSpacing, numDetectorPixels, rotationAngleIncrement, dSI, dSD, phantom );

		/* RAMPFILTER --- Filter defined in Frequency Domain */
		RampFilter rampfilter2 = new RampFilter( numDetectorPixels, detectorSpacing );

		FilteredBackProjector filterbackprojector2 = new FilteredBackProjector(backprojectSizeX, backprojectSizeY, backprojectSpacingX, backprojectSpacingY, fanbeamdetector.sinogram, rampfilter2);

//		fanbeamdetector.show("Fanogram");
//		fanbeamdetector.sinogram.show("Sinogram Rebinning");
//		rampfilter.getRealSubGrid(0, rampfilter.getSize()[0]).show("RampFilter");
		filterbackprojector2.show("Backprojection_ramp");
				
		
	}
}

