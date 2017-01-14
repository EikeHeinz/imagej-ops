
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.LoGPixelFeature.class,
	name = Ops.Pixelfeatures.LoGPixelFeature.NAME)
public class LoGPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Ops.Pixelfeatures.LoGPixelFeature
{

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> loGOps;

	@Override
	public void initialize() {

		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		loGOps = new ArrayList<>();

		for (int i = 0; i <= maxSteps; i++) {
			double sigma = 0.0d;
			sigma = Math.pow(2, i) * minSigma;
			RandomAccessibleInterval<T> kernel = ops().create().kernelLog(sigma, in()
				.numDimensions(), Util.getTypeFromInterval(in()));
			loGOps.add(RAIs.function(ops(), Convolve.class, in(), kernel));
		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(
		RandomAccessibleInterval<T> input)
	{
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> loGOp : loGOps) {
			results.add(loGOp.calculate(Views.interval(Views.extendMirrorDouble(
				input), input)));
		}
		return Views.stack(results);
	}

}
