package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.DoGPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.DoGPxFeature.class, name = Ops.Image.DoGPxFeature.NAME)
public class DoGPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements DoGPxFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
		
		double temp = Math.log(maxSigma) / Math.log(2);
		double maxSteps = ops().math().floor(temp);
		double[] sigmas = new double[(int) maxSteps];

		for (int i = 0; i < maxSteps; i++) {
			sigmas[i] = Math.pow(2, i) * minSigma;
		}
		
		// FIXME limited to two dimensions
		RandomAccessibleInterval<T> out = (RandomAccessibleInterval<T>) ops().create().img(input.dimension(0),
				input.dimension(1), input.dimension(2), (long) maxSteps);

		for (int i = 0; i < maxSteps - 1; i++) {
			for (int j = i + 1; j < maxSteps; j++) {
				RandomAccessibleInterval<T> interval = Views.hyperSlice(Views.hyperSlice(out, 3, 1), 2, i);
				// FIXME no subtract method for DoG found
				ops().filter().dog(interval, input, sigmas[i], sigmas[j]);
			}
		}
		return out;
	}

}
