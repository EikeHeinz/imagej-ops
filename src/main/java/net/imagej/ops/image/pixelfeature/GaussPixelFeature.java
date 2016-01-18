package net.imagej.ops.image.pixelfeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.UnaryComputerOp;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussPxFeature.class, name = Ops.Image.GaussPxFeature.NAME)
public class GaussPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private List<UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	private RandomAccessibleInterval<T> output;

	@Override
	public void initialize() {
		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = (long) maxSteps;
		Dimensions dim = FinalDimensions.wrap(dims);
		output = ops().create().img(dim);

		gaussOps = new ArrayList<UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>>();

		for (int i = 0; i < maxSteps; i++) {
			double[] sigmas = new double[(int) maxSteps];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(Computers.unary(ops(), Gauss.class, in(),
					in(), sigmas));
		}
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		IntervalView<T> extendedIn = Views.interval(Views.extendMirrorDouble(input), input);

		int i = 0;
		for (UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			IntervalView<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, i);

			gaussOp.compute1(extendedIn, outSlice);
			i++;
		}

		return output;
	}

}
