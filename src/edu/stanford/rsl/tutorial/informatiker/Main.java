package edu.stanford.rsl.tutorial.informatiker;


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