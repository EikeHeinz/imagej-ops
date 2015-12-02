package net.imagej.ops.image.pixelfeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.DoGPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.DoGPxFeature.class, name = Ops.Image.DoGPxFeature.NAME)
public class DoGPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements DoGPxFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	// private double[] sigmas;

	private double maxSteps;

	private RandomAccessibleInterval<T> output;

	private List<ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval>> doGOps;

	@Override
	public void initialize() {
		maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));
		
		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length-1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length-1] = (long) maxSteps;

		// FIXME replace with createOp = ops().function(Create.Img.class,
		// RandomAccessibleInterval.class, long[].class);
		output = (RandomAccessibleInterval<T>) ops().create().img(dims);

		doGOps = new ArrayList<ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval>>();
		for (int i = 0; i < maxSteps - 1; i++) {
			for (int j = i + 1; j < maxSteps; j++) {
				double[] sigmas1 = new double[(int) maxSteps];
				double[] sigmas2 = new double[(int) maxSteps];
				Arrays.fill(sigmas1, Math.pow(2, i) * minSigma);
				Arrays.fill(sigmas2, Math.pow(2, j) * minSigma);
				// FIXME Nullpointer in DoGVaryingSigmas.conforms()
				// in() is not initialized in conforms method, because no input is given
				ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> tempOp = ops().computer(
						Ops.Filter.DoG.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class, sigmas1,
						sigmas2);
				doGOps.add(tempOp);
			}
		}
	}

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> extendedIn = Views.interval(Views.extendMirrorDouble(input), input);
		int i = 0;
		for (ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> doGOp : doGOps) {

			RandomAccessibleInterval<T> outSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 1), 2, i);
			// FIXME no subtract method for DoG found
			doGOp.compute(input, outSlice);
			i++;
		}
		return output;
	}

}
