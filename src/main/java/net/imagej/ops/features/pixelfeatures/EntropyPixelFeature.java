package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.BinMapper1d;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.EntropyFeature.class)
public class EntropyPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements Ops.Pixelfeatures.EntropyFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	@Parameter
	private int radius;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;
	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	@Override
	public void initialize() {
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());
		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));
		gaussOps = new ArrayList<>();
		for (int i = 0; i <= maxSteps; i++) {
			double[] sigmas = new double[in().numDimensions()];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(RAIs.function(ops(), Gauss.class, in(), sigmas));
		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			RandomAccessibleInterval<T> blurredInput = gaussOp
					.calculate(Views.interval(Views.extendMirrorDouble(input), input));
			for (int currentBinSize = 32; currentBinSize <= 256; currentBinSize *= 2) {
				RandomAccessibleInterval<T> output = createOp.calculate(input);
				RandomAccess<T> outRA = output.randomAccess();
				Cursor<T> cursor = Views.iterable(blurredInput).cursor();
				IntervalView<T> extendedInput = Views.interval(Views.extendMirrorDouble(blurredInput), blurredInput);
				while (cursor.hasNext()) {
					cursor.next();
					// TODO min, max value
					BinMapper1d<T> binMapper = new Real1dBinMapper<>(0, 1, currentBinSize, false);
					Histogram1d<T> histogram = new Histogram1d<>(binMapper);

					long[] currentCursorPos = new long[2];
					cursor.localize(currentCursorPos);
					long[] coords = new long[] { currentCursorPos[0] - radius, currentCursorPos[1] - radius,
							currentCursorPos[0] + radius, currentCursorPos[1] - radius };

					FinalInterval interval = FinalInterval.createMinMax(coords);
					IntervalView<T> subView = Views.interval(extendedInput, interval);
					histogram.addData(subView);

					double totalCount = 0;
					for (int i = 0; i < histogram.getBinCount(); i++) {
						totalCount += histogram.frequency(i);
					}

					double entropy = 0;
					for (int i = 0; i < histogram.getBinCount(); i++) {
						if (histogram.frequency(i) > 0) {
							double probability = histogram.frequency(i) / totalCount;
							entropy += -probability * Math.log(probability) / Math.log(2.0);
						}
					}

					outRA.setPosition(cursor);
					outRA.get().setReal(entropy);
				}
				results.add(output);
			}
		}
		return Views.stack(results);
	}
}
