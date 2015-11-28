package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Point2D;

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

		// assert Source or Detector is always outside of Phantom
		// (assumption: )
//		if (dSI * dSI < delta_x * delta_x + delta_y * delta_y ) {
//			System.err.println("Source inside Phantom!");
//		}
//		if ((dSD - dSI) * (dSD - dSI) < delta_x * delta_x + delta_y * delta_y ) {
//			System.err.println("Detector inside Phantom!");
//		}
		
		initializeGrid();
	}

	void initializeGrid() {
		double theta;
		double s;
		
		// iterate over all angles
		for (int i = 0; i < numProjections; ++i) {
			i = 90;
			theta = this.indexToPhysical(0, i)[1];
			
			// iterate over all detector positions
			for (int j = 0; j < numDetectorPixels; ++j) {
				j = 512;
				s = this.indexToPhysical(j, 0)[0];
				// Angle to X axes
				double alpha = theta - Math.atan(s/dSD);
				// Position on X axes
				double d = dSI * Math.tan(alpha); //s/dSD;
		
				Point2D start;
				Point2D end;
				
				// case beam parallel to x axes
				if ( Math.abs(alpha) < epsilon || Math.abs(alpha - Math.PI) < epsilon ) {
					start = new Point2D(delta_x, s);
					end = new Point2D(-delta_x, s);
				} else {
		
					double x = delta_y / Math.tan(alpha);

					// hits top border
					if (x + d - delta_x < epsilon) {
						start = new Point2D(x + d, -delta_y);
						
					// hits right border
					} else {
						double y = (delta_x - d)
									* Math.tan(alpha);
						start = new Point2D(delta_x, -y);
					}
						
					// hits bottom border
					if (x - d - delta_x < epsilon) {
						end = new Point2D(-x + d, delta_y);
						
					// hits left border
					} else {
						double y = (delta_x + d)
									* Math.tan(alpha);
						end = new Point2D(-delta_x, y);
					}
				}
				
				show_line( start,  end);
				break;
			}
			break;
		}
	}
	
	void show_line(Point2D start, Point2D end) {

		double current_pos_x = start.getX();
		double current_pos_y = start.getY();
		double step_x = (end.getX() - start.getX()) / Math.abs(end.getX() - start.getX());
		double step_y = (end.getY() - start.getY()) / Math.abs(end.getY() - start.getY());
		double step_size = 1.0;
		double step_size_x = step_size * Math.abs(end.getX() - start.getX()) / (Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY()));
		double step_size_y = step_size * Math.abs(end.getY() - start.getY()) / (Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY()));			
		double max_steps = Math.abs(end.getX() - start.getX()) / step_size_x;
		if(Math.abs(step_size_x) < epsilon) {
			max_steps = Math.abs(end.getY() - start.getY()) / step_size_y;
			step_x = 0.0;
		}
		if(Math.abs(step_size_y) < epsilon) {
			step_y = 0.0;
		}
		MyPhantom linePhantom = new MyPhantom(600,600, 1, 1);
//		for(int l = 0; l < 600; ++l){
//			linePhantom.setAtIndex(300, l, 5f);
//			linePhantom.setAtIndex(l, 300, 5f);
//		}
		for (int k = 0; k <= (int) max_steps; ++k) {
			current_pos_x += step_x * step_size_x;
			current_pos_y += step_y * step_size_y;
			double[] p = phantom.physicalToIndex(current_pos_x, current_pos_y);
			if(p[0] <= 0 || p[1] <= 0) continue; //setAtIndex checks for values > size but not < 0
			linePhantom.setAtIndex((int)p[0], (int)p[1], 10f);
		}
		linePhantom.show();
	}

	
}
