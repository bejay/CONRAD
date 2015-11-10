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
		filteredData.setSpacing(sinogram.getSpacing());
		filteredData.setOrigin(sinogram.getOrigin());
		
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
		
		// for all rows in phantom
		for( int i = 0; i < this.getWidth(); ++i ) {
			
			// for all cols in phantom
			for( int j = 0; j < this.getHeight(); ++j ) {
				
				float detector_value = 0f;
				
				// for all angles theta
				// calculate the detector position and add up the values
				for( int k = 0; k < sinogram.getHeight(); ++k ) {
					
					double theta = filteredData.indexToPhysical(0, k)[1];
					double s = 0.;
					
					if (Math.abs(theta - Math.PI * 0.5) < epsilon) {
						s = this.indexToPhysical(i, j)[1];
					} else {
						s = this.indexToPhysical(i, j)[0] / Math.cos(theta);
					}
					System.out.printf("i: %s ,j: %s ,theta: %s ,s: %s \n", i,j,theta/Math.PI*180,s);
					// biliniear interpolation --> linear interpolation is sufficient
					double[] pos = filteredData.physicalToIndex(s, theta);
					System.out.printf("%s  %s    %s\n", pos[0],pos[1], InterpolationOperators.interpolateLinear(filteredData, pos[0], pos[1]));
					detector_value += InterpolationOperators.interpolateLinear(filteredData, pos[0], pos[1]);
					
				}
				this.setAtIndex(i, j, detector_value);
			break;
			}
			break;
		}
		this.show();
	
	}
	


}