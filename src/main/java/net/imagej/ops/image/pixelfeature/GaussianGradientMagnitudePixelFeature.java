package net.imagej.ops.image.pixelfeature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.GaussianGradientMagnitudePxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussianGradientMagnitudePxFeature.class, name = Ops.Image.GaussianGradientMagnitudePxFeature.NAME)
public class GaussianGradientMagnitudePixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T>
		implements GaussianGradientMagnitudePxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double sigma;

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = ops().create().img(input);

		RandomAccessibleInterval<T> output1 = ops().create().img(input);
		RandomAccessibleInterval<T> output2 = ops().create().img(input);

		// FIXME hardcoded for testing purpose - kernels might not be correct
		// double[][] kernelX = new double[][] { { 1, 2, 1 }, { -1, 0, 1 } };
		// double[][] kernelY = new double[][] { { -1, 0, 1 }, { 1, 2, 1 } };
		RandomAccessibleInterval<T> kernelX = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { 1, 2, 1, 0, 0, 0, -1, -2, -1 }, 3L, 3L);
		RandomAccessibleInterval<T> kernelY = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 }, 3L, 3L);

		RandomAccessibleInterval<T> extended = Views.interval(Views.extendMirrorSingle(input), input);
		// smoothing image
		RandomAccessibleInterval<T> blurred = ops().filter().gauss(extended, sigma);
	

//		ThreadPoolExecutor pool = null;
		// try {

		RandomAccessibleInterval<T> convolveX = (RandomAccessibleInterval<T>) ops().filter().convolve((Img<T>) blurred,
				kernelX);
		RandomAccessibleInterval<T> convolveY = (RandomAccessibleInterval<T>) ops().filter().convolve((Img<T>) blurred,
				kernelY);
//		ImageJFunctions.show(convolveX);
//		ImageJFunctions.show(convolveY);
		// FIXME blocking queue size, threads, both instances
//		int numThreads = Runtime.getRuntime().availableProcessors();
//		pool = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS,
//				new ArrayBlockingQueue<Runnable>(32000));

		// ImageJFunctions.show(blurred, "begin");
		// convolve the image with both sobel filters
		// FIXME both convolution calls throw
		// ArrayIndexOutOfBoundsExceptions
		// SeparableSymmetricConvolution.convolve(kernelX, blurred, output1,
		// pool);
//		pool.shutdown();
		// ImageJFunctions.show(output1, "out1");

		// FIXME see above
//		pool = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS,
//				new ArrayBlockingQueue<Runnable>(32000));

		// SeparableSymmetricConvolution.convolve(kernelY, blurred, output2,
		// pool);
		// ImageJFunctions.show(output2, "out2");

//		pool.shutdown();

		// calculate gradient magnitude in each pixel G = sqrt(Gx^2 + Gy^2)
		ops().map(convolveX, convolveX, ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class));
		ops().map(convolveY, convolveY, ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class));

		output = (RandomAccessibleInterval<T>) ops().math().add(output, convolveX);
		output = (RandomAccessibleInterval<T>) ops().math().add(output, convolveY);
		ops().map(output, output, ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class));

		// } catch (IncompatibleTypeException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		return output;
	}

}
