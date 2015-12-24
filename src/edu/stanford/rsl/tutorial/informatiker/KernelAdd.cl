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

