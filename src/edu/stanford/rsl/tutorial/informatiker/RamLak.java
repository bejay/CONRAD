package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;



public class RamLak extends Grid1DComplex {

	public RamLak( int numDetectorPixels, double detectorSpacing ) {
		
		super( numDetectorPixels );
		this.setSpacing(detectorSpacing);

		for(int i = 0; i < this.getSize()[0]/2; ++i) {

			if(i == 0) {
				setAtIndex( i, 0.25f*(float)detectorSpacing);
			} else if( i%2 == 0) {
				setAtIndex( i, 0.0f);
			} else {
				setAtIndex(i, (float) ((-1.0)/(detectorSpacing*detectorSpacing*i*i*Math.PI*Math.PI)));
			}
		}
		
		for(int i = this.getSize()[0]/2; i < this.getSize()[0]; ++i) {
			if(i == 0) {
				setAtIndex( i, 0.25f*(float)detectorSpacing);
			} else if( i%2 == 0) {
				setAtIndex( i, 0.0f);
			} else {
				setAtIndex(i, (float) ((-1.0)/(detectorSpacing*detectorSpacing*(-this.getSize()[0] + i)*(-this.getSize()[0]+i)*Math.PI*Math.PI)));
			}
		}
		
		this.transformForward();
	}

}
