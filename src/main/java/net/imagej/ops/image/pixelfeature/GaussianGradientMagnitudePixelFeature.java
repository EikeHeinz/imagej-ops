package net.imagej.ops.image.pixelfeature;

import java.util.Arrays;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.GaussianGradientMagnitudePxFeature;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussianGradientMagnitudePxFeature.class, name = Ops.Image.GaussianGradientMagnitudePxFeature.NAME)
public class GaussianGradientMagnitudePixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T>
		implements GaussianGradientMagnitudePxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double sigma;

	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;
	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> gaussOp;
	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> convolverKernelX;
	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> convolverKernelY;

	@Override
	public void initialize() {
		createOp = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class);
		// FIXME hardcoded
		RandomAccessibleInterval<T> kernelX = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { 1, 2, 1, 0, 0, 0, -1, -2, -1 }, 3L, 3L);
		RandomAccessibleInterval<T> kernelY = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 }, 3L, 3L);
		double[] sigmas = new double[in().numDimensions()];
		Arrays.fill(sigmas, sigma);
		gaussOp = Functions.unary(ops(), Ops.Filter.Gauss.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class, sigmas);
		convolverKernelX = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(),
				kernelX);
		convolverKernelY = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(),
				kernelY);
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = createOp.compute1(input);

		RandomAccessibleInterval<T> extended = Views.interval(Views.extendMirrorSingle(input), input);
		// smoothing image
		RandomAccessibleInterval<T> blurred = gaussOp.compute1(input);

		// TODO maybe use separable kernel?

		RandomAccessibleInterval<T> convolveX = convolverKernelX.compute1((Img<T>) blurred);
		RandomAccessibleInterval<T> convolveY = convolverKernelY.compute1((Img<T>) blurred);

		// calculate gradient magnitude in each pixel G = sqrt(Gx^2 + Gy^2)
		ops().map(convolveX, convolveX, ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class));
		ops().map(convolveY, convolveY, ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class));

		output = (RandomAccessibleInterval<T>) ops().math().add(output, convolveX);
		output = (RandomAccessibleInterval<T>) ops().math().add(output, convolveY);
		ops().map(output, output, ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class));

		return output;
	}

}
