package net.imagej.ops.image.pixelfeature;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
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

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> doGOpsFunction;

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void initialize() {
		maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		doGOpsFunction = new ArrayList<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>>();

		for (int i = 0; i < maxSteps - 1; i++) {
			for (int j = i + 1; j <= maxSteps; j++) {
				Double sigma1 = new Double(Math.pow(2, i) * minSigma);
				Double sigma2 = new Double(Math.pow(2, j) * minSigma);

				@SuppressWarnings("unchecked")
				UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> tempOp = (UnaryFunctionOp) Functions
						.unary(ops(), Ops.Filter.DoG.class, RandomAccessibleInterval.class, in(), sigma1, sigma2);

				doGOpsFunction.add(tempOp);

			}
		}
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		// maxSteps+1 choose 2 -1 because counting starts with 0
		double amountOfOutSlices = (maxSteps * (maxSteps + 1) / 2) - 1;

		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = (long) amountOfOutSlices;
		Dimensions dim = FinalDimensions.wrap(dims);

		RandomAccessibleInterval<T> output = ops().create().img(dim);

		IntervalView<T> extendedIn = Views.interval(Views.extendMirrorDouble(input), input);
		int i = 0;
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> doGOp : doGOpsFunction) {

			RandomAccessibleInterval<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, i);
			RandomAccessibleInterval<T> tempOut = doGOp.compute1(extendedIn);
			ops().copy().rai(outSlice, tempOut);

			i++;
		}

		return output;
	}

}
