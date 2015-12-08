package net.imagej.ops.image.pixelfeature;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.GaussianGradientMagnitudePxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.SeparableSymmetricConvolution;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Image.GaussianGradientMagnitudePxFeature.class, name = Ops.Image.GaussianGradientMagnitudePxFeature.NAME)
public class GaussianGradientMagnitudePixelFeature<T extends RealType<T>>
		extends AbstractPixelFeatureOp<T> implements
		GaussianGradientMagnitudePxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double sigma;

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = ops().create().img(input);

		RandomAccessibleInterval<T> output1 = ops().create().img(input);
		RandomAccessibleInterval<T> output2 = ops().create().img(input);
		double[][] kernelX = new double[][] { { 1, 2, 1 }, { -1, 0, 1 } };
		double[][] kernelY = new double[][] { { -1, 0, 1 }, { 1, 2, 1 } };

		// FIXME blocking queue size, threads
		int numThreads = Runtime.getRuntime().availableProcessors();
		ThreadPoolExecutor pool = new ThreadPoolExecutor(numThreads,
				numThreads, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(32000));

		RandomAccessibleInterval<T> extended = Views.interval(
				Views.extendMirrorSingle(input), input);
		RandomAccessibleInterval<T> blurred = ops().filter().gauss(extended,
				sigma);
		try {

			SeparableSymmetricConvolution.convolve(kernelX, blurred, output1,
					pool);
			ops().map(output1, output1,
					ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class));
			SeparableSymmetricConvolution.convolve(kernelY, blurred, output2,
					pool);
			ops().map(output2, output2,
					ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class));
			ops().math().add(output, output1);
			ops().math().add(output, output2);
			ops().map(output, output,
					ops().op(Ops.Math.Sqrt.class, RealType.class,
							RealType.class));

		} catch (IncompatibleTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return output1;
	}

}
