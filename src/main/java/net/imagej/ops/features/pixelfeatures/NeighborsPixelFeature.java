package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.NeighborsFeature;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.NeighborsFeature.class)
public class NeighborsPixelFeature<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements NeighborsFeature {

	@Parameter
	private int minSigma;

	@Parameter
	private int maxSigma;

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (int sigma = minSigma; sigma <= maxSigma; sigma *= 2) {
			IntervalView<T> extendedInput = Views.interval(Views.extendMirrorDouble(input), input);
			results.add(Views.interval(Views.translate(extendedInput, sigma, sigma), input));
			results.add(Views.interval(Views.translate(extendedInput, sigma, 0), input));
			results.add(Views.interval(Views.translate(extendedInput, sigma, -sigma), input));
			results.add(Views.interval(Views.translate(extendedInput, 0, sigma), input));
			results.add(Views.interval(Views.translate(extendedInput, 0, -sigma), input));
			results.add(Views.interval(Views.translate(extendedInput, -sigma, sigma), input));
			results.add(Views.interval(Views.translate(extendedInput, -sigma, 0), input));
			results.add(Views.interval(Views.translate(extendedInput, -sigma, -sigma), input));
		}
		return Views.stack(results);
	}

}
