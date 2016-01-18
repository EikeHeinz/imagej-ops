package net.imagej.ops.image.pixelfeature;

import java.util.Arrays;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.GaussianGradientMagnitudePxFeature;
import net.imagej.ops.Ops.Math.Sqr;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryComputerOp;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussianGradientMagnitudePxFeature.class, name = Ops.Image.GaussianGradientMagnitudePxFeature.NAME)
public class GaussianGradientMagnitudePixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T>
		implements GaussianGradientMagnitudePxFeature {

	@Parameter
	private double sigma;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createOp;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> convolverKernelX;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> convolverKernelY;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> squareMapOp;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrtMapOp;


	@Override
	public void initialize() {
		createOp = Functions.unary(ops(), Ops.Create.Img.class,RandomAccessibleInterval.class, in());

		// TODO get dimensionality from input
		RandomAccessibleInterval<T> sobelKernel = (RandomAccessibleInterval<T>) ops().create().kernelSobel(new double[]{3,3});
		RandomAccessibleInterval<T> kernelX = Views.hyperSlice(Views.hyperSlice(sobelKernel, 3, 0), 2, 0);
		RandomAccessibleInterval<T> kernelY = Views.hyperSlice(Views.hyperSlice(sobelKernel, 3, 0), 2, 1);
		
		double[] sigmas = new double[in().numDimensions()];
		Arrays.fill(sigmas, sigma);
		gaussOp = Computers.unary(ops(), Ops.Filter.Gauss.class, in(), in(), sigmas);
		
		convolverKernelX = Computers.unary(ops(), Ops.Filter.Convolve.class, in(), in(), kernelX);
		convolverKernelY = Computers.unary(ops(), Ops.Filter.Convolve.class, in(), in(), kernelY);
		
		
		
		Sqr squareOp = ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class);
		squareMapOp = Computers.unary(ops(), Ops.Map.class, in(), in(), squareOp);
		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMapOp = Computers.unary(ops(), Ops.Map.class, in(), in(), sqrtOp);
			
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {

		RandomAccessibleInterval<T> extended = Views.interval(Views.extendMirrorSingle(input), input);
		// smoothing image
		RandomAccessibleInterval<T> blurred = createOp.compute1(input);
		gaussOp.compute1(extended, blurred);

		RandomAccessibleInterval<T> convolveX = createOp.compute1(input);
		RandomAccessibleInterval<T> convolveY = createOp.compute1(input);
		convolverKernelX.compute1(blurred, convolveX);
		convolverKernelY.compute1(blurred, convolveY);

		// calculate gradient magnitude in each pixel G = sqrt(Gx^2 + Gy^2)
		squareMapOp.compute1(convolveX, convolveX);
		squareMapOp.compute1(convolveY, convolveY);

		RandomAccessibleInterval<T> output = createOp.compute1(input);
		
		// FIXME create Op in initialize method
		output = (RandomAccessibleInterval<T>) ops().math().add(output, convolveX);
		output = (RandomAccessibleInterval<T>) ops().math().add(output,convolveY);

		sqrtMapOp.compute1(output, output);

		return output;
	}

}
