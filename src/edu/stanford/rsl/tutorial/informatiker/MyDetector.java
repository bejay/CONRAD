package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Point2D;



public class MyDetector extends Grid2D {

	int numProjections;
	int numDetectorPixels;
	double detectorSpacing;

	double delta_x;
	double delta_y;

	double epsilon = 0.0001;

	MyPhantom phantom;

	
	// constructor
	public MyDetector(int numProjections, double detectorSpacing,
			int numDetectorPixels, MyPhantom phantom) {

		super(numDetectorPixels, numProjections);
		setSpacing(detectorSpacing, Math.PI / numProjections);
		setOrigin(-(numDetectorPixels-1)*0.5*detectorSpacing, 0);
		
		this.numProjections = numProjections;
		this.detectorSpacing = detectorSpacing;
		this.numDetectorPixels = numDetectorPixels;
		this.phantom = phantom;
		delta_x = phantom.getWidth() * 0.5 * phantom.getSpacing()[0];
		delta_y = phantom.getHeight() * 0.5* phantom.getSpacing()[1];

		initializeGrid();

	}

	// creates the sinogram, automatically called by constructor
	void initializeGrid() {

		double theta = 0; // angle theta between 0 and 180 degrees
		double s = 0; // position on the detector between -sd ... 0 ... +sd
		double detector_pos = 0; // Equivalent detector position at the x-axes
		double rad90 = Math.PI*0.5; // 90 degrees in radians
		Point2D start; // start and end position of the ray within the phantom grid
		Point2D end; // perpendicular to the detector

		// iterate over all angles
		for (int i = 0; i < numProjections; ++i) {

			theta = this.indexToPhysical(0, i)[1];
			
			// iterate over all detector positions
			for (int j = 0; j < numDetectorPixels; ++j) {

				s = this.indexToPhysical(j, 0)[0];
				
				// calculate starting and ending Point within bounding box
				// coordinates are double values, they do not have to be exactly
				// on the borders

				// all coordinates are given in the phantom space in World Coordinates
				// the detector origin is said to be in the middle of the phantom

				// special case theta == 0, 90 degrees to avoid division by zero
				// actually eclipse throws no error, but result is equal to
				// infinity
				if (Math.abs(theta) < epsilon) {
					start = new Point2D(s, -delta_y);
					end = new Point2D(s, delta_y);

				} else if (Math.abs(theta - rad90) < epsilon) {
					start = new Point2D(delta_x, s);
					end = new Point2D(-delta_x, s);

				} else {

					// differentiate between < 90 and > 90 degrees
					if (theta - rad90 < epsilon) {
						
						detector_pos = s / Math.cos(theta);
						double x = delta_y / Math.tan(rad90 - theta);

						// hits top border
						if (x + detector_pos - delta_x < epsilon) {
							start = new Point2D(x + detector_pos, -delta_y);
						
						// hits right border
						} else {
							double y = (delta_x - detector_pos)
									* Math.tan(rad90 - theta);
							start = new Point2D(delta_x, -y);
						}
						
						// hits bottom border
						if (x - detector_pos - delta_x < epsilon) {
							end = new Point2D(-x + detector_pos, delta_y);
						
						// hits left border
						} else {
							double y = (delta_x + detector_pos)
									* Math.tan(rad90 - theta);
							end = new Point2D(-delta_x, y);
						}
						

					} else {

						detector_pos = -s / Math.cos(Math.PI - theta);
						double x = delta_y
								/ Math.tan(theta - rad90);

						// hits bottom border
						if (x + detector_pos - delta_x < epsilon) {
							start = new Point2D(x + detector_pos, delta_y);
						
						// hits right border
						} else {
							double y = (delta_x - detector_pos)
									* Math.tan(theta - rad90);
							start = new Point2D( delta_x, y );
						}
						
						// hits top border
						if (x - detector_pos - delta_x < epsilon) {
							end = new Point2D( -x + detector_pos, -delta_y );
						
						// hits left border
						} else {
							double y = (delta_x + detector_pos)
									* Math.tan(theta - rad90);
							end = new Point2D( -delta_x, -y );
						}
					}
				}		
							
				// summing up the line integral
				float detector_value = 0f;
				
				double current_pos_x = start.getX();
				double current_pos_y = start.getY();
								
				// Unit direction vector
				double step_x = (end.getX() - start.getX()) / Math.abs(end.getX() - start.getX());
				double step_y = (end.getY() - start.getY()) / Math.abs(end.getY() - start.getY());
				
				// STEP SIZE in [mm]
				double step_size = 1.0;
				double step_size_x = step_size * Math.abs(end.getX() - start.getX()) / (Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY()));
				double step_size_y = step_size * Math.abs(end.getY() - start.getY()) / (Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY()));
								
				double max_steps = Math.abs(end.getX() - start.getX()) / step_size_x;
				// end.getX() - start.getX() == 0 --> theta = 0
				if(Math.abs(step_size_x) < epsilon) {
					max_steps = Math.abs(end.getY() - start.getY()) / step_size_y;
					step_x = 0.0;
				}
				// end.getX() - start.getX() == 0 --> theta = 0
				if(Math.abs(step_size_y) < epsilon) {
					step_y = 0.0;
				}
				// walk down the line and add up phantom values
				for (int k = 0; k <= (int) max_steps; ++k) {
		
					
					current_pos_x += step_x * step_size_x;
					current_pos_y += step_y * step_size_y;
					double[] p = phantom.physicalToIndex(current_pos_x, current_pos_y);
					detector_value += InterpolationOperators.interpolateLinear(phantom, p[0], p[1]);

				}
				
				// write new value into the sinogram
				setAtIndex(j, i, detector_value);
			}
		}
	}

	
	// to visualize the detector line
	// does the same as the line integral
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
		for(int l = 0; l < 600; ++l){
			linePhantom.setAtIndex(300, l, 5f);
			linePhantom.setAtIndex(l, 300, 5f);
		}
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