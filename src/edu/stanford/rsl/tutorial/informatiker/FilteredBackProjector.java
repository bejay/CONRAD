package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.*;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Point2D;



public class FilteredBackProjector extends Grid2D {
	
	Grid2D phantom;
	Grid2D sinogram;
	Grid2D filteredData;
	Grid1DComplex filter;
	
	double epsilon = 0.0001;
	double delta_x;
	double delta_y;


	public FilteredBackProjector(Grid2D phantom, Grid2D sinogram, Grid1DComplex filter) {
		
		super(phantom.getSize()[0], phantom.getSize()[1]);
		setSpacing(phantom.getSpacing()[0], phantom.getSpacing()[1]);
		setOrigin(phantom.getOrigin()[0], phantom.getOrigin()[1]);
		
		this.phantom = phantom;
		this.sinogram = sinogram;
		this.filter = filter;
		filteredData = new Grid2D(sinogram.getSize()[0], sinogram.getSize()[1]);
		
		delta_x = phantom.getWidth() * 0.5 * phantom.getSpacing()[0];
		delta_y = phantom.getHeight() * 0.5* phantom.getSpacing()[1];
		
		filter();
		backproject();
		
	}
	
	
	void filter() {
		
		
		// iterate over all projections (theta) 
		for(int i = 0; i < sinogram.getSize()[1]; i++) {
			
			Grid1DComplex detectorRow = new Grid1DComplex(sinogram.getSubGrid(i));
			detectorRow.transformForward();
			
			// apply filter
			for( int j = 0; j < detectorRow.getSize()[0]; j++ ) {
				
				detectorRow.setAtIndex(j, detectorRow.getRealAtIndex(j) * filter.getRealAtIndex(j));
				//filteredData.setAtIndex( j, i, detectorRow.getAtIndex(j) * filter.getRealAtIndex(j) );
				
			}
			
			detectorRow.transformInverse();
			
			// set at filteredData  --- something like set subGrid?
			for( int j = 0; j < detectorRow.getSize()[0]; j++ ) {
			
				filteredData.setAtIndex(j,i,detectorRow.getRealAtIndex(j));
				
			}
		}
		filteredData.show();	
	}
	
	void backproject() {
		
		double theta = 0; // angle theta between 0 and 180 degrees
		double s = 0; // position on the detector between -sd ... 0 ... +sd
		double detector_pos = 0; // Equivalent detector position at the x-axes
		double rad90 = Math.PI*0.5; // 90 degrees in radians
		Point2D start; // start and end position of the ray within the phantom grid
		Point2D end; // perpendicular to the detector

		// iterate over all angles
		for (int i = 0; i < sinogram.getHeight(); ++i) {

			theta = this.indexToPhysical(0, i)[1];
			
			// iterate over all detector positions
			for (int j = 0; j < sinogram.getWidth(); ++j) {

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
				double[] det = physicalToIndex(s, theta);
				float detector_value = filteredData.getAtIndex((int)det[0], (int)det[1]);
				
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
				for (int k = 2; k <= (int) max_steps-5; ++k) {
		
					current_pos_x += step_x * step_size_x;
					current_pos_y += step_y * step_size_y;
					
					
					double[] p = this.physicalToIndex(current_pos_x, current_pos_y);
					System.out.printf("%s  %s \n", p[0], p[1]);
					this.addAtIndex((int)p[0], (int)p[1], detector_value);
//					
//					detector_value += InterpolationOperators.interpolateLinear(
//							(Grid2D) phantom, p[0], p[1]);

				}
			}
		}
		this.show();
	}
	


}