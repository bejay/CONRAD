package edu.stanford.rsl.tutorial.informatiker;


import ij.ImageJ;


public class Main {
	public static void main( String[] args ) {
		
		
//		new ImageJ();
		MyPhantom phantom = new MyPhantom( 600, 600, 1, 1);
		phantom.show();
		
		
		// int numProjections, double detectorSpacing, int numDetectorPixels, MyPhantom phantom
		MyDetector detector = new MyDetector( 360, 1, 1024, phantom );
		detector.show();


	}
}