package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.HessianPxFeature.class, name = Ops.Image.HessianPxFeature.NAME)
public class HessianPixelFeatureOp<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	// TODO init()
	// TODO kernel is hardcoded

	

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> convolverKernelX;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> convolverKernelY;

	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernelX = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { 1, 2, 1, 0, 0, 0, -1, -2, -1 }, 3L, 3L);
		RandomAccessibleInterval<T> kernelY = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 }, 3L, 3L);
		convolverKernelX = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(), kernelX);
		convolverKernelY = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(), kernelY);

	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {

		// TODO optimize convolve calls

		ImageJFunctions.show(input);

		RandomAccessibleInterval<T> convolveX = convolverKernelX.compute1(input);
		RandomAccessibleInterval<T> convolveY = convolverKernelY.compute1(input);
		RandomAccessibleInterval<T> convolveXX = convolverKernelX.compute1(convolveX);
		RandomAccessibleInterval<T> convolveXY = convolverKernelY.compute1(convolveX);
		RandomAccessibleInterval<T> convolveYX = convolverKernelX.compute1(convolveY);
		RandomAccessibleInterval<T> convolveYY = convolverKernelY.compute1(convolveY);
		convolveX = null;
		convolveY = null;

		// dereference X and Y??

		// output px value in hessian matrix is: [[XX,XY],[YX,YY]] =
		// [[a,b],[c,d]]
		// Trace T=a+d
		// Determinant D=ad-bc

		// First eigenvalue: (T/2) + sqrt((4b^2+(a-d)^2)/2)
		// Second eigenvalue: (T/2) - sqrt((4b^2+(a-d)^2)/2)

		return null;
	}

}
