package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;



public class RamLak extends Grid1DComplex {

	public RamLak( int numDetectorPixels, double detectorSpacing ) {
		
		super( numDetectorPixels );
		this.setSpacing(detectorSpacing);
//		RamLak_Spatial.setOrigin(origin);
		
		for(int i = 0; i < numDetectorPixels; ++i) {
			if(i == 0) {
				setAtIndex( i, 0.25f);
			} else if( i%2 == 0) {
				setAtIndex( i, 0.0f);
			} else {
				setAtIndex(i, (float) ((-1.f)/(detectorSpacing*detectorSpacing*i*i*Math.PI*Math.PI)));
			}
		}
		
		this.transformForward();
		// to illustrate only the Real part, imaginary part is zero		
		this.getRealSubGrid(0, this.getSize()[0]).show("RampFilter_Spatial");
	
	}

}
