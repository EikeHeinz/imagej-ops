
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
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

@Plugin(type = Ops.Pixelfeatures.GaussPixelFeature.class,
name = Ops.Pixelfeatures.GaussPixelFeature.NAME)
public class GaussPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Ops.Pixelfeatures.GaussPixelFeature
{

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private List<UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<Dimensions, RandomAccessibleInterval> createRAIFromDim;

	@Override
	public void initialize() {
		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		createRAIFromDim = Functions.unary(ops(), Ops.Create.Img.class,
			RandomAccessibleInterval.class, Dimensions.class);

		gaussOps = new ArrayList<>();

		for (int i = 0; i < maxSteps; i++) {
			double[] sigmas = new double[(int) maxSteps];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(Computers.unary(ops(), Gauss.class, in(), in(), sigmas));
		}
	}

	@Override
	public RandomAccessibleInterval<T> compute1(
		RandomAccessibleInterval<T> input)
	{
		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = gaussOps.size();
		Dimensions dim = FinalDimensions.wrap(dims);

		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<T> output = createRAIFromDim.compute1(dim);

		IntervalView<T> extendedIn = Views.interval(Views.extendMirrorDouble(input),
			input);

		int i = 0;
		for (UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			IntervalView<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3,
				0), 2, i);

			gaussOp.compute1(extendedIn, outSlice);
			i++;
		}

		return output;
	}

}
