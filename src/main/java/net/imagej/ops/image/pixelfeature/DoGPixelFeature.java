package net.imagej.ops.image.pixelfeature;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.DoGPxFeature.class, name = Ops.Image.DoGPxFeature.NAME)
public class DoGPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private double maxSteps;

	private RandomAccessibleInterval<T> output;

	@SuppressWarnings("rawtypes")
	private List<UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval>> doGOpsFunction;

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void initialize() {
		maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		// maxSteps+1 choose 2 -1 because counting starts with 0
		double amountOfOutSlices = (maxSteps * (maxSteps + 1) / 2) - 1;

		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = (long) amountOfOutSlices;

		output = (RandomAccessibleInterval<T>) ops().create().img(dims);

		doGOpsFunction = new ArrayList<UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval>>();

		for (int i = 0; i < maxSteps - 1; i++) {
			for (int j = i + 1; j <= maxSteps; j++) {
				Double sigma1 = new Double(Math.pow(2, i) * minSigma);
				Double sigma2 = new Double(Math.pow(2, j) * minSigma);
				UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> tempOp = Functions.unary(ops(),
						Ops.Filter.DoG.class, RandomAccessibleInterval.class, in(), sigma1, sigma2);

				doGOpsFunction.add(tempOp);

			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		IntervalView<T> extendedIn = Views
				.interval(Views.extendMirrorDouble(input), input);
		int i = 0;
		for (UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> doGOp : doGOpsFunction) {

			RandomAccessibleInterval<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, i);
			RandomAccessibleInterval<T> tempOut = doGOp.compute1(extendedIn);
			ops().copy().rai(outSlice, tempOut);

			i++;
		}

		return output;
	}

}
