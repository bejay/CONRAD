package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.*;


public class FilteredBackProjector extends Grid2D {
	
	Grid2D sinogram;
	Grid2D filteredData;
	Grid1DComplex filter;
	
	double epsilon = 0.0001;

	public FilteredBackProjector(int size_x, int size_y, double spacing_x, double spacing_y, Grid2D sinogram, Grid1DComplex filter) {
		
		super(size_x, size_y);
		setSpacing(spacing_x, spacing_y);
		setOrigin(-(size_x-1)*spacing_x*0.5, -(size_y-1)*spacing_x*0.5);
		
		this.sinogram = sinogram;
		this.filter = filter;
		filteredData = new Grid2D(sinogram.getSize()[0], sinogram.getSize()[1]);
		filteredData.setSpacing(sinogram.getSpacing());
		filteredData.setOrigin(sinogram.getOrigin());
				
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
				
				// Apply filtering 
				// Complex Multiplication
				detectorRow.setRealAtIndex(j, detectorRow.getRealAtIndex(j) * filter.getRealAtIndex(j) - detectorRow.getImagAtIndex(j) * filter.getImagAtIndex(j));
				detectorRow.setImagAtIndex(j, detectorRow.getRealAtIndex(j) * filter.getImagAtIndex(j) + detectorRow.getImagAtIndex(j) * filter.getRealAtIndex(j));
				
				// do not filter
				//detectorRow.setAtIndex( j, detectorRow.getRealAtIndex(j) );
				
			}
			
			detectorRow.transformInverse();
			
			// set at filteredData  --- something like set subGrid?
			for( int j = 0; j < detectorRow.getSize()[0]; j++ ) {
			
				filteredData.setAtIndex(j,i,detectorRow.getRealAtIndex(j));
				
			}
		}
//		filteredData.show("FilteredSinogram");	
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
					if(theta >= Math.PI) theta %= Math.PI;
					
					// calculate intersection between detector line d=(0,0)+a(cos(theta),-sin(theta))
					// and line through point p othorgonal to d
					// s = xcos(theta)-sin(theta) (cos(theta), -sin(theta)
					double val = indexToPhysical(i, j)[0] * Math.cos(theta) + indexToPhysical(i, j)[1] * Math.sin(theta);
					double s_x = val * Math.cos(theta);
					double s_y = val * Math.sin(theta);
					double s = Math.sqrt(s_x*s_x + s_y*s_y);
					// correct sign of s
					if(Math.abs(Math.signum(s_y)) != 0 ) {
						s = s * Math.signum(s_y);
					} else if( Math.abs(Math.signum(s_x)) != 0 ) {
						s = s * Math.signum(s_x);
					}

					// Bilinear interpolation --> linear interpolation is sufficient
					double[] pos = filteredData.physicalToIndex(s, theta);
					detector_value += InterpolationOperators.interpolateLinear(filteredData, pos[0], pos[1]);
					
				}

				this.setAtIndex(i, j, detector_value);
			}
		}
	}
}