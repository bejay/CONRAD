package edu.stanford.rsl.tutorial.informatiker;


import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;



public class RampFilter extends Grid1DComplex {

	public RampFilter( int numDetectorPixels, double detectorSpacing ) {
		
		// ( log2 (numDetectorPixels) ) % 2 = 0
		
		super( numDetectorPixels );
		
		setSpacing( 1.0 / ( detectorSpacing * this.getSize()[0] ) );
	
		for( int i = 0; i < this.getSize()[0] >> 1; i++ ) {
//			double j = this.indexToPhysical(i);
			setAtIndex( i, (float) i);
		}
		for( int i = (this.getSize()[0] >> 1); i < this.getSize()[0]; i++ ) {
//			double j = this.indexToPhysical(i);
			setAtIndex( i, (float) (this.getSize()[0] - i));
		}

		// to illustrate only the Real part, imaginary part is zero		
		this.getRealSubGrid(0, this.getSize()[0]).show("RampFilter");
		
		//
		this.getImagSubGrid(0, this.getSize()[0]).show("RampFilter_img");
	}

}
