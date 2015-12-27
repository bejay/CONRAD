// OpenCL Kernel Function for element by element grid addition

kernel void add2DGrid(
	global const float *a,
	global float *b,
	int rows,
	int cols)
{
	int x = get_global_id(0);
	int y = get_global_id(1);
	if(x >= cols || y >= rows) {
		return;
	}
	b[y*cols + x] = a[y*cols + x] + a[y*cols + x];
}



kernel void backproject(
	global const float *a,
	global float *b,
	int rows,
	int cols,
	int sinRows,
	int sinCols,
	float spacingX,
	float spacingY,
	float originX,
	float originY,
	float sinSpacingX,
	float sinSpacingY,
	float sinOriginX)
{
	int x = get_global_id(0);
	int y = get_global_id(1);
	int z = get_global_id(2);
	
	if(x < rows && y < cols && z < sinCols) {
	
		//NOT SYNCHRONIZED <-- LOCAL BARRIER or init buffer???
		if(z==1) {
			b[y*rows + x] = 0.0;
		//}
		// barrier(CLK_GLOBAL_MEM_FENCE);
		for(int i = 0; i<360;i++){ z = i;
		
		// indexToPhysical
		float x_ = x * spacingX + originX;
		float y_ = y * spacingY + originY;
		
		float theta = z * sinSpacingY;
		float val = x_ * cos(theta) + y_ * sin(theta);
		float s_x = val * cos(theta);
		float s_y = val * sin(theta);
		float s = sqrt(s_x*s_x + s_y*s_y);
		
		// correct sign of s
		if( s_y < 0 ) {
			s = s * -1.;
		} else if(s_x < 0 && s_y == 0) {
			s = s * -1.;
		}
		
		float posX = (s - sinOriginX) / sinSpacingX;
		// linear interpolation
		float rounded = posX - (int) posX;
		b[y*rows + x] += (1.0 - rounded) * a[z*sinRows + ((int) posX)] + rounded * a[z*sinRows + ((int) posX + 1 )];
	}}
	}
}

