package edu.stanford.rsl.tutorial.informatiker;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;


public class MyPhantom extends Grid2D {
	public MyPhantom(int width, int height, double spacingX, double spacingY) {
		super(width, height);
		
		this.setSpacing(spacingX, spacingY);
		this.setOrigin(-(width-1)*spacingX*0.5, -(height-1)*spacingY*0.5);
		
		
		// Draw a square with size 100,100 with center 150,150
		for (int x = 100; x <= 200; x++) {
			for (int y = 100; y <= 200; y++) {
				setAtIndex(x, y, 0.5f);
			}
		}

		// Draw a circle at cx, cy with radius sqrt(r)
		int cx = 250;
		int cy = 250;
		int r = 100;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (Math.abs(x - cx) * Math.abs(x - cx) + Math.abs(y - cy) * Math.abs(y - cy) < r) {
					setAtIndex(x, y, 2f);
				}
			}
		}

		// Draw a line at the top border
		for (int x = 67; x < 600; x++) {
			for (int y = 0; y < 16; y++) {
				setAtIndex(x, y, 0.25f);
			}
		}
	}
}