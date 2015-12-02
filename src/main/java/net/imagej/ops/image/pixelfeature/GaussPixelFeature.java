package net.imagej.ops.image.pixelfeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.FunctionOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
import net.imagej.ops.Ops.Image.GaussPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussPxFeature.class, name = Ops.Image.GaussPxFeature.NAME)
public class GaussPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements GaussPxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double minSigma;

	@Parameter(type = ItemIO.INPUT)
	private double maxSigma;

	private FunctionOp<long[], RandomAccessibleInterval> createOp;

	private List<ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval>> gaussOps;

	private RandomAccessibleInterval<T> output;

	private long[] dims;

	@Override
	public void initialize() {
		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = (long) maxSteps;

		// FIXME replace with createOp = ops().function(Create.Img.class,
		// RandomAccessibleInterval.class, long[].class);
		output = (RandomAccessibleInterval<T>) ops().create().img(dims);

		gaussOps = new ArrayList<ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval>>();

		for (int i = 0; i < maxSteps; i++) {
			double[] sigmas = new double[(int) maxSteps];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(ops().computer(Gauss.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class,
					sigmas));
		}
	}

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> in) {

		// RandomAccessibleInterval out = createOp.compute(dims);

		RandomAccessibleInterval<T> extendedIn = Views.interval(Views.extendMirrorDouble(in), in);
		int i = 0;
		for (ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> gaussOp : gaussOps) {
			IntervalView<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, i);

			gaussOp.compute(extendedIn, outSlice);
			i++;
		}

		return output;
	}

}
