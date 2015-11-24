package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class FanBeamDetector extends Grid2D {

	// Assumption: isocenter is origin and center of phantom
	
	int numProjections;
	int numDetectorPixels;
	double detectorSpacing;
	// increment of rotation Angle β in rad
	double rotationAngleIncrement;
	// source-to-isocenter distance
	double dSI;
	// source-to-detector distance 
	double dSD;
	

	double delta_x;
	double delta_y;

	double epsilon = 0.0001;

	MyPhantom phantom;

	
	public FanBeamDetector(int numProjections, double detectorSpacing,
			int numDetectorPixels, double rotationAngleIncrement, double dSI, double dSD , MyPhantom phantom) {
		super(numDetectorPixels, numProjections);
		setSpacing(detectorSpacing, rotationAngleIncrement);
		setOrigin(-(numDetectorPixels-1)*0.5*detectorSpacing, 0);
		
		this.numProjections = numProjections;
		this.detectorSpacing = detectorSpacing;
		this.numDetectorPixels = numDetectorPixels;
		this.rotationAngleIncrement = rotationAngleIncrement;
		this.dSI = dSI;
		this.dSD = dSD;
		this.phantom = phantom;
		delta_x = phantom.getWidth() * 0.5 * phantom.getSpacing()[0];
		delta_y = phantom.getHeight() * 0.5* phantom.getSpacing()[1];

		if (dSI * dSI < delta_x * delta_x + delta_y * delta_y ) {
			System.err.println("Source inside Phantom!");
		}
		if ((dSD - dSI) * (dSD - dSI) < delta_x * delta_x + delta_y * delta_y ) {
			System.err.println("Detector inside Phantom!");
		}
		
		//initializeGrid();
	}

}
