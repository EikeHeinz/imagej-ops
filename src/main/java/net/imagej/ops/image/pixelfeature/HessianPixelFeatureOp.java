package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryComputerOp;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.HessianPxFeature.class, name = Ops.Image.HessianPxFeature.NAME)
public class HessianPixelFeatureOp<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	// TODO init()

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> convolverKernelX;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> convolverKernelY;
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrtMapOp;

	@Override
	public void initialize() {
		// TODO get dimensionality from input
		RandomAccessibleInterval<T> sobelKernel = (RandomAccessibleInterval<T>) ops().create()
				.kernelSobel(new double[] { 3, 3 });
		RandomAccessibleInterval<T> kernelX = Views.hyperSlice(Views.hyperSlice(sobelKernel, 3, 0), 2, 0);
		RandomAccessibleInterval<T> kernelY = Views.hyperSlice(Views.hyperSlice(sobelKernel, 3, 0), 2, 1);
		convolverKernelX = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(),
				kernelX);
		convolverKernelY = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(),
				kernelY);
		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMapOp = Computers.unary(ops(), Ops.Map.class, in(), in(), sqrtOp);

	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {

		// TODO optimize convolve calls
		RandomAccessibleInterval<T> convolveX = convolverKernelX.compute1(input);
		RandomAccessibleInterval<T> convolveY = convolverKernelY.compute1(input);
		RandomAccessibleInterval<T> convolveXX = convolverKernelX.compute1(convolveX);
		RandomAccessibleInterval<T> convolveXY = convolverKernelY.compute1(convolveX);
		RandomAccessibleInterval<T> convolveYX = convolverKernelX.compute1(convolveY);
		RandomAccessibleInterval<T> convolveYY = convolverKernelY.compute1(convolveY);

		// dereference X and Y??
		convolveX = null;
		convolveY = null;

		// create output img containing slice for each feature
		// (trace,determinant, 1st/2nd eigenvalue)
		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = 4;
		// TODO create Dimensions object instead of using long array
		RandomAccessibleInterval<T> output = (RandomAccessibleInterval<T>) ops().create().img(dims);
		// TODO optimize runtime
		IntervalView<T> traceSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 0);
		IntervalView<T> determinantSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 1);
		IntervalView<T> eigenvalue1Slice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 2);
		IntervalView<T> eigenvalue2Slice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 3);

		// calculate trace
		RandomAccessibleInterval<T> trace = (RandomAccessibleInterval<T>) ops().math().add(convolveXX, convolveYY);
		ops().copy().rai(traceSlice, trace);

		// calculate determinant
		RandomAccessibleInterval<T> multiplyXXYY = (RandomAccessibleInterval<T>) ops().math().multiply(convolveXX,
				convolveYY);

		RandomAccessibleInterval<T> multiplyXYYX = (RandomAccessibleInterval<T>) ops().math().multiply(convolveXY,
				convolveYX);
		RandomAccessibleInterval<T> determinant = (RandomAccessibleInterval<T>) ops().math().subtract(multiplyXXYY,
				multiplyXYYX);
		ops().copy().rai(determinantSlice, determinant);

		// eigenvalues
		RandomAccessibleInterval<T> tempTraceSquare = (RandomAccessibleInterval<T>) ops().math().multiply(trace, trace);
		T type = Util.getTypeFromInterval(determinant);
		type.setReal(4.0f);
		RandomAccessibleInterval<T> tempDeterminant = (RandomAccessibleInterval<T>) ops().math().multiply(determinant,
				type);
		RandomAccessibleInterval<T> temp = ops().create().img(input);
		RandomAccessibleInterval<T> temp1 = (RandomAccessibleInterval<T>) ops().math().subtract(tempTraceSquare,
				tempDeterminant);
		sqrtMapOp.compute1(temp1, temp);
		RandomAccessibleInterval<T> addSqrt = (RandomAccessibleInterval<T>) ops().math().add(temp, trace);
		RandomAccessibleInterval<T> subtractSqrt = (RandomAccessibleInterval<T>) ops().math().subtract(temp, trace);
		type.setReal(2.0d);
		
		RandomAccessibleInterval<T> eigenvalue1 = (RandomAccessibleInterval<T>) ops().math().divide(addSqrt, type);
		RandomAccessibleInterval<T> eigenvalue2 = (RandomAccessibleInterval<T>) ops().math().divide(subtractSqrt, type);
		ops().copy().rai(eigenvalue1Slice, eigenvalue1);
		ops().copy().rai(eigenvalue2Slice, eigenvalue2);
		// output px value in hessian matrix is: [[XX,XY],[YX,YY]] =
		// [[a,b],[c,d]]
		// Trace T=a+d
		// Determinant D=ad-bc

		// First eigenvalue: (T/2) + sqrt((4b^2+(a-d)^2)/2)
		// Second eigenvalue: (T/2) - sqrt((4b^2+(a-d)^2)/2)
		// using trace and determinant: (1/2) * (trace +/- sqrt(trace*trace -
		// 4*determinant))

		return output;

	}

}
