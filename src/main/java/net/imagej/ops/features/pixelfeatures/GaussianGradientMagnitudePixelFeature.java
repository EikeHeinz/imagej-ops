
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.GaussianGradientMagnitudePixelFeature.class,
	name = Ops.Pixelfeatures.GaussianGradientMagnitudePixelFeature.NAME)
public class GaussianGradientMagnitudePixelFeature<T extends RealType<T>>
	extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>>
	implements Ops.Pixelfeatures.GaussianGradientMagnitudePixelFeature
{

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sobelOp;

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	@Override
	public void initialize() {
		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		gaussOps = new ArrayList<>();

		for (int i = 0; i <= maxSteps; i++) {
			double[] sigmas = new double[in().numDimensions()];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(RAIs.function(ops(), Gauss.class, in(), sigmas));
		}

		sobelOp = RAIs.function(ops(), Ops.Filter.Sobel.class, in());
	}

	@Override
	public CompositeIntervalView<T, RealComposite<T>> calculate(
		RandomAccessibleInterval<T> input)
	{
		RandomAccessibleInterval<T> extended = Views.interval(Views
			.extendMirrorSingle(input), input);
		List<RandomAccessibleInterval<T>> blurredImgs = new ArrayList<>();

		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			blurredImgs.add(gaussOp.calculate(extended));
		}

		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (RandomAccessibleInterval<T> blurredImg : blurredImgs) {
			results.add(sobelOp.calculate(blurredImg));
		}

		return Views.collapseReal(Views.stack(results));
	}
}
