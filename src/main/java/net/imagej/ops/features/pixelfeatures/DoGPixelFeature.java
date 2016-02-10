
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.DoGPixelFeature.class,
name = Ops.Pixelfeatures.DoGPixelFeature.NAME)
public class DoGPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Ops.Pixelfeatures.DoGPixelFeature
{

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private double maxSteps;

	@SuppressWarnings("rawtypes")
	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval>> doGFunctions;

	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> copyRAI;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<Dimensions, RandomAccessibleInterval> createRAIFromDim;

	@SuppressWarnings("rawtypes")
	@Override
	public void initialize() {
		maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		createRAIFromDim = Functions.unary(ops(), Ops.Create.Img.class,
			RandomAccessibleInterval.class, Dimensions.class);

		copyRAI = Computers.unary(ops(), Ops.Copy.RAI.class,
			RandomAccessibleInterval.class, in());

		doGFunctions = new ArrayList<>();

		for (int i = 0; i < maxSteps - 1; i++) {
			for (int j = i + 1; j <= maxSteps; j++) {
				Double sigma1 = new Double(Math.pow(2, i) * minSigma);
				Double sigma2 = new Double(Math.pow(2, j) * minSigma);

				UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> tempOp =
					Functions.unary(ops(), Ops.Filter.DoG.class,
						RandomAccessibleInterval.class, in(), sigma1, sigma2);

				doGFunctions.add(tempOp);

			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public RandomAccessibleInterval<T> compute1(
		RandomAccessibleInterval<T> input)
	{
		// maxSteps+1 choose 2 -1 because counting starts with 0
		double amountOfOutSlices = (maxSteps * (maxSteps + 1) / 2) - 1;

		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = (long) amountOfOutSlices;
		Dimensions dim = FinalDimensions.wrap(dims);

		RandomAccessibleInterval<T> output = createRAIFromDim.compute1(dim);

		IntervalView<T> extendedIn = Views.interval(Views.extendMirrorDouble(input),
			input);
		int i = 0;
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> doGFunction : doGFunctions) {

			RandomAccessibleInterval<T> outSlice = Views.hyperSlice(Views.hyperSlice(
				output, 3, 0), 2, i);
			RandomAccessibleInterval<T> tempOut = doGFunction.compute1(extendedIn);
			copyRAI.compute1(tempOut, outSlice);

			i++;
		}

		return output;
	}

}