package net.imagej.ops.image.pixelfeature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.GaussPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussPxFeature.class, name = Ops.Image.GaussPxFeature.NAME)
public class GaussPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements GaussPxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double minSigma;

	@Parameter(type = ItemIO.INPUT)
	private double maxSigma;

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {

		double temp = Math.log(maxSigma) / Math.log(2);
		double maxSteps = ops.math().floor(temp);

		// TODO optimize with regard to plane selection in for loop
		RandomAccessibleInterval<T> out = (RandomAccessibleInterval<T>) ops.create().img(input.dimension(0),
				input.dimension(1), input.dimension(2), (long) maxSteps);

		// create stack of blurred images with varying sigma (between maxSigma
		// and minSigma)
		for (int i = 0; i < maxSteps; i++) {
			double currentSigma = Math.pow(2, i) * minSigma;
			RandomAccessibleInterval<T> interval = Views.hyperSlice(Views.hyperSlice(out, 3, 1), 2, i);
			// FIXME works but throws indexoutofboundsexception
			ops.filter().gauss(interval, input, currentSigma);

		}
		return out;
	}

}
