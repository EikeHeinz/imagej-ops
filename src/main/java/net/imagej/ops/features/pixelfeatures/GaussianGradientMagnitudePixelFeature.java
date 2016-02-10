
package net.imagej.ops.features.pixelfeatures;

import java.util.Arrays;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.GaussianGradientMagnitudePxFeature;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;


public class GaussianGradientMagnitudePixelFeature<T extends RealType<T>>
	extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements GaussianGradientMagnitudePxFeature
{

	@Parameter
	private double sigma;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> sobelFunction;

	@Override
	public void initialize() {
		createOp = Functions.unary(ops(), Ops.Create.Img.class,
			RandomAccessibleInterval.class, in());
		double[] sigmas = new double[in().numDimensions()];
		Arrays.fill(sigmas, sigma);
		gaussOp = Computers.unary(ops(), Ops.Filter.Gauss.class, in(), in(),
			sigmas);
		sobelFunction = Functions.unary(ops(), Ops.Filter.Sobel.class,
			RandomAccessibleInterval.class, in());
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> compute1(
		RandomAccessibleInterval<T> input)
	{

		RandomAccessibleInterval<T> extended = Views.interval(Views
			.extendMirrorSingle(input), input);
		// smoothing image
		RandomAccessibleInterval<T> blurred = createOp.compute1(input);
		gaussOp.compute1(extended, blurred);

		RandomAccessibleInterval<T> output = sobelFunction.compute1(blurred);

		return output;
	}

}
