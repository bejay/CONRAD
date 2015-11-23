package edu.stanford.rsl.tutorial.informatiker;


import ij.ImageJ;


public class Main {
	public static void main( String[] args ) {
		
		
		int phantomWidth = 600;
		int phantomHeight = 600;
		double phantomSpacingX = 1.0;
		double phantomSpacingY = 1.0;
		
		int numProjections = 360;
		int numDetectorPixels = 1024;
		double detectorSpacing = 1.0;
		
		
		
//		new ImageJ();
		MyPhantom phantom = new MyPhantom( phantomWidth, phantomHeight, phantomSpacingX, phantomSpacingY );
//		phantom.show("Phantom");
		
		// int numProjections, double detectorSpacing, int numDetectorPixels, MyPhantom phantom
		MyDetector detector = new MyDetector( numProjections, detectorSpacing, numDetectorPixels, phantom );
//		detector.show("Sinogram");
		
		/* RAMPFILTER --- Filter defined in Frequency Domain */
		RampFilter rampfilter = new RampFilter( numDetectorPixels, detectorSpacing );
		rampfilter.getRealSubGrid(0, rampfilter.getSize()[0]).show("RampFilter");

//		FilteredBackProjector filterbackprojector2 = new FilteredBackProjector(phantom, detector, rampfilter);
//		filterbackprojector2.show("Backprojection_ramp");
		
		/* RAMLAKFILTER --- Filter defined in Spatial Domain */
		RamLak rampfilter_spatial = new RamLak( numDetectorPixels, detectorSpacing );
		rampfilter_spatial.getRealSubGrid(0, rampfilter_spatial.getSize()[0]).show("Ramlak");
		rampfilter_spatial.getImagSubGrid(0, rampfilter_spatial.getSize()[0]).show("Ramlak_Imaginary");
		
		FilteredBackProjector filterbackprojector = new FilteredBackProjector(phantom, detector, rampfilter_spatial);
		filterbackprojector.show("Backprojection_ramlak");


	}
}