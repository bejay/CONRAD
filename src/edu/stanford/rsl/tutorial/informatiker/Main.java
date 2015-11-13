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
		
		
		
		new ImageJ();
		MyPhantom phantom = new MyPhantom( phantomWidth, phantomHeight, phantomSpacingX, phantomSpacingY );
//		phantom.show();
		
		
		// int numProjections, double detectorSpacing, int numDetectorPixels, MyPhantom phantom
		MyDetector detector = new MyDetector( numProjections, detectorSpacing, numDetectorPixels, phantom );
//		detector.show();
		
		RampFilter rampfilter = new RampFilter( numDetectorPixels, detectorSpacing );
		FilteredBackProjector filterbackprojector = new FilteredBackProjector(phantom, detector, rampfilter);


	}
}