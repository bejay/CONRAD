package edu.stanford.rsl.tutorial.informatiker;


import ij.ImageJ;


public class Main {
	public static void main( String[] args ) {
		
		
//		new ImageJ();
		MyPhantom phantom = new MyPhantom( 600, 600, 1, 1);
		phantom.show();
		
		
		// int numProjections, double detectorSpacing, int numDetectorPixels, MyPhantom phantom
<<<<<<< HEAD
		MyDetector detector = new MyDetector( 360, 1, 1024, phantom );
=======
		MyDetector detector = new MyDetector( 360, 1.0, 1024, phantom );
>>>>>>> 639838c9721f867ddc268b0664cc3922966a2a21
		detector.show();


	}
}