package net.imagej.ops.image.pixelfeature;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.FunctionOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.DoGPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Image.DoGPxFeature.class, name = Ops.Image.DoGPxFeature.NAME)
public class DoGPixelFeature<T extends RealType<T>> extends
		AbstractPixelFeatureOp<T> implements DoGPxFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private double maxSteps;

	private RandomAccessibleInterval<T> output;

	private List<FunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval>> doGOps;

	private RandomAccessibleInterval<T> extendedIn;

	@Override
	public void initialize() {
		maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = (long) maxSteps;

		// FIXME replace with createOp = ops().function(Create.Img.class,
		// RandomAccessibleInterval.class, long[].class);
		output = (RandomAccessibleInterval<T>) ops().create().img(dims);
		// FIXME extension necessary?
		extendedIn = (RandomAccessibleInterval<T>) Views.interval(
				Views.extendMirrorDouble(in()), in());

		doGOps = new ArrayList<FunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval>>();
		for (int i = 0; i < maxSteps - 1; i++) {
			for (int j = i + 1; j <= maxSteps; j++) {
				FunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> tempOp = ops()
						.function(Ops.Filter.DoG.class,
								RandomAccessibleInterval.class, extendedIn,
								Math.pow(2, i) * minSigma,
								Math.pow(2, j) * minSigma);
				doGOps.add(tempOp);
			}
		}
	}

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {

		int i = 0;
		for (FunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> doGOp : doGOps) {

			RandomAccessibleInterval<T> outSlice = Views.hyperSlice(
					Views.hyperSlice(output, 3, 1), 2, i);
			RandomAccessibleInterval tempOut = doGOp.compute(extendedIn);
			// FIXME copy tempOut into outSlice
			// throws ArrayIndexOutOfBoundsException after 3rd iteration
			ops().copy().rai(outSlice, tempOut);
			
			i++;
		}
		return output;
	}

}
