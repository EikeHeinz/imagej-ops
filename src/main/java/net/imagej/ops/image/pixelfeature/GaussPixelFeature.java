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
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.GaussPxFeature.class, name = Ops.Image.GaussPxFeature.NAME)
public class GaussPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements GaussPxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double minSigma;

	@Parameter(type = ItemIO.INPUT)
	private double maxSigma;

	private FunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;

	private List<ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval>> gaussOps;

	private RandomAccessibleInterval<T> output;

	@Override
	public void initialize() {
		double temp = Math.log(maxSigma) / Math.log(2);
		double maxSteps = ops().math().floor(temp);
		// FIXME remove dimension restriction
		output = (RandomAccessibleInterval<T>) ops().create().img(in().dimension(0), in().dimension(1), in().dimension(2),
				(long) maxSteps);

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

		RandomAccessibleInterval<T> extendedIn = Views.interval(Views.extendMirrorDouble(in), in);

		// FIXME for i >2 throws array index out of bounds exception
		for (int i = 0; i < gaussOps.size(); i++) {
			IntervalView<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, i);

			gaussOps.get(i).compute(extendedIn, outSlice);
		}

		return output;
	}

}
