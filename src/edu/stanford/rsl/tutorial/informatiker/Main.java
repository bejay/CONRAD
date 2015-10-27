package edu.stanford.rsl.tutorial.informatiker;


//import ij.IJ;
//import ij.plugin.PlugIn;

public class Main {
	public static void main( String[] args ) {
		
		MyPhantom phantom = new MyPhantom( 641, 641 );
		phantom.show();
		
		// int numProjections, double detectorSpacing, int numDetectorPixels, MyPhantom phantom
		MyDetector detector = new MyDetector( 360, 1.0, 1024, phantom );
		detector.show();
		
	}
}