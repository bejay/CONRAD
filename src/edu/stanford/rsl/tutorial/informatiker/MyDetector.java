package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
//import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
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
	public MyDetector( int numProjections, double detectorSpacing, int numDetectorPixels, MyPhantom phantom ) {
		
		super( numProjections, numDetectorPixels );
		this.numProjections = numProjections;
		this.detectorSpacing = detectorSpacing;
		this.numDetectorPixels = numDetectorPixels;
		this.phantom = phantom;	// does java copy by value?
		delta_x = phantom.getWidth() * 0.5;
		delta_y = phantom.getHeight() * 0.5;
				
		initializeGrid();
		
	}
	
	// creates the sinogram, automatically called by contructor
	void initializeGrid() {
		
		double theta = 0; 	// angle theta between 0 and 180 degrees
		double s = 0;		// position on the detector between -sd ... 0 ... +sd
		double detector_pos = 0;	// aquivalent detector position at the x-achses
		Point2D start;	// start and end position of the ray within the phantom grid 
		Point2D end;	// perpendicular to the detector
		
		// iterate over all angles
		for( int i = 0; i < numProjections; ++i ) {
		
			theta = 180.0 / numProjections * i;

			// iterate over all detector positions
			for( int j = 0; j < numDetectorPixels; j++ ) {
				
				s = detectorSpacing * j - detectorSpacing * numDetectorPixels * 0.5;

				// calculate starting and ending Point within bounding box
				// coordinates are double values, they do not have to be exactly on the borders

				// assumption: detector is smaller than grid
				// 			   otherwise trigeonometry does not work like this
				
				// all coordinates are given in the phantom space
				// the detector origin is said to be in the middle of the phantom
				// but values are finally converted and stored with origin at the top left corner
				
				// special case theta == 0, 90 degrees to avoid division by zero
				// actually eclipse gives no error, but result is equal to infinity
				if( Math.abs( theta ) < epsilon ) {
					
					start = new Point2D( s + delta_x, 0 );
					end = new Point2D( s + delta_x, delta_y * 2 );
					
				} else if ( Math.abs( theta - 90 ) < epsilon) {
					
					start = new Point2D( delta_x * 2, delta_y + s );
					end = new Point2D( 0, delta_y + s );
					
				} else {
					
					// differentiate between < 90 and > 90 degrees
					// conversion from theta to actual angle differs
					if( theta - 90 < epsilon ) {
						
						detector_pos = s / Math.sin( Math.toRadians( 90 - theta ) );
						double x = delta_y / Math.tan( Math.toRadians( 90 - theta ) );
						
						// hits top border
						if ( x + detector_pos - delta_x < epsilon ) {
							start = new Point2D( x + delta_x + detector_pos, 0 );
						// hits right border
						} else {
							double y = ( delta_x - detector_pos ) * Math.tan( Math.toRadians( 90 - theta ) );
							start = new Point2D( 2*delta_x, delta_y - y );
						}
						// hits bottom border						
						if ( x - detector_pos - delta_x < epsilon ) {
							end = new Point2D( delta_x - x + detector_pos, 2*delta_y );
						// hits left border
						} else {
							double y = ( delta_x + detector_pos ) * Math.tan( Math.toRadians( 90 - theta ) );
							end = new Point2D( 0, y + delta_y );
						}
						
					} else {
						
						detector_pos = s / Math.sin( Math.toRadians( theta - 90 ) );
						double x = delta_y / Math.tan( Math.toRadians( theta - 90 ) );

						// hits bottom border
						if ( x - detector_pos - delta_x < epsilon ) {
							start = new Point2D( x + delta_x - detector_pos, 2*delta_y );
						// hits right border
						} else {
							double y = (delta_x + detector_pos) * Math.tan( Math.toRadians( theta - 90 ) );
							start = new Point2D( 2*delta_x, delta_y + y );
						}
						// hits top border						
						if ( x + detector_pos - delta_x < epsilon ) {
							end = new Point2D( delta_x - x - detector_pos, 0 );
						// hits left border
						} else {
							double y = ( delta_x - detector_pos ) * Math.tan( Math.toRadians( theta - 90 ) );
							end = new Point2D( 0, delta_y - y );
						}
					}
				}

				// line integral what are we using for the stepsize?
				double max_steps = delta_x*4;
				double current_pos_x = 0;
				double current_pos_y = 0;
				float detector_value = 0; //
				
				// walk down the line and add up phantom values
				for( int k = 0; k <= max_steps; ++k ) {

					current_pos_x = start.getX() + ( end.getX() - start.getX() ) * k / max_steps;  
					current_pos_y = start.getY() + ( end.getY() - start.getY() ) * k / max_steps; 
					detector_value += interpolateLinear( (Grid2D) phantom, current_pos_x, current_pos_y );
					
				}
				
				// write new value into the sinogram
				setAtIndex( i, j, detector_value );
//				show_line(start, end);
				
			}
		}

	}
	
	
	// How to use interpolateLinear from the InterpolationOperators class with type MyPhantom and not Grid2D
	public static float interpolateLinear(final Grid1D grid, double i) {
		int lower = (int) Math.floor(i);
		double d = i - lower; // d is in [0, 1)
		return (float) (
				(1.0-d)*grid.getAtIndex(lower)
				+ ((d != 0.0) ? d*grid.getAtIndex(lower+1) : 0.0)
		);
	}
	
	public static float interpolateLinear(final Grid2D grid, double x, double y) {
		if (grid == null) return 0;
		if (x < 0 || x > grid.getSize()[0]-1 || y < 0 || y > grid.getSize()[1]-1)
			return 0;
		
		int lower = (int) Math.floor(y);
		double d = y - lower; // d is in [0, 1)

		return (float) (
				(1.0-d)*interpolateLinear(grid.getSubGrid(lower), x)
				+ ((d != 0.0) ? d*interpolateLinear(grid.getSubGrid(lower+1), x) : 0.0)
		);
	}

	// visualizes the line between start and end 
	void show_line( Point2D start, Point2D end ) {
		
		MyPhantom linePhantom = new MyPhantom((int) delta_x*2, (int) delta_y * 2);
		
		double max_steps = delta_x*8;
		double current_pos_x = 0;
		double current_pos_y = 0;
		for( int k = 0; k <= max_steps; ++k ) {

			current_pos_x = start.getX() + ( end.getX() - start.getX() ) * k / max_steps;  
			current_pos_y = start.getY() + ( end.getY() - start.getY() ) * k / max_steps; 
			
			int cx = 0;
			int cy = 0;
			if ((int)current_pos_x < 0) {
				cx = 0;
			} else if ((int)current_pos_x > (int) delta_x*2  - 1 ) {
				cx = (int) delta_x*2  - 1;
			} else {
				cx = (int)current_pos_x;
			}
			if ((int)current_pos_y < 0) {
				cy = 0;
			} else if ((int)current_pos_y > (int) delta_y*2  - 1 ) {
				cy = (int) delta_y*2  - 1;
			} else {
				cy = (int)current_pos_y;
			}
		
			linePhantom.setAtIndex( cx, cy, 10f );
		}
		linePhantom.show();
	}
	
}