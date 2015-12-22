// OpenCL Kernel Function for element by element grid addition

kernel void add2DGrid(
	global const float *a,
	global float *b,
	int numElements)
{
	int id = get_global_id(0);
	if(id >= numElements) {
		return;
	}
	b[id] = a[id] + a[id];
}


/*
__kernel void add2DGrid(
	__global const float *a,
	__global float *b)
{
	int id = get_global_id(0);
	b[id] = a[id] + a[id];
}
*/
