
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.DoGFeature;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.DoGFeature.class)
public class DoGPixelFeature<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements DoGFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private double maxSteps;

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> doGFunctions;

	@Override
	public void initialize() {
		maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		doGFunctions = new ArrayList<>();

		for (int i = 0; i <= maxSteps; i++) {
			for (int j = i + 1; j <= maxSteps; j++) {
				Double sigma1 = new Double(Math.pow(2, i) * minSigma);
				Double sigma2 = new Double(Math.pow(2, j) * minSigma);
				UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> tempOp = RAIs.function(ops(),
						Ops.Filter.DoG.class, in(), sigma1, sigma2);
				doGFunctions.add(tempOp);
			}
		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		IntervalView<T> extendedIn = Views.interval(Views.extendMirrorDouble(input), input);
		List<RandomAccessibleInterval<T>> dogImages = new ArrayList<>();
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> doGFunction : doGFunctions) {
			dogImages.add(doGFunction.calculate(extendedIn));
		}
		return Views.stack(dogImages);
	}
}
