package net.imagej.ops.filter.bilateral;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.BilateralFilter;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Filter.BilateralFilter.class)
public class DefaultBilateral<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements BilateralFilter {

	@Parameter
	private double domain;

	@Parameter
	private double range;

	@Parameter
	private int radius;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	@Override
	public void initialize() {
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());

	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		IntervalView<T> extendedInput = Views.interval(Views.extendMirrorDouble(input), input);
		Cursor<T> cursor = Views.iterable(extendedInput).cursor();
		RandomAccessibleInterval<T> output = createOp.calculate(input);
		RandomAccess<T> outputRA = output.randomAccess();

		while (cursor.hasNext()) {
			double sum = 0;
			double normalization = 0;
			double x = cursor.next().getRealDouble(); // center value

			long[] cursorPos = new long[2];
			cursor.localize(cursorPos);

			long[] min = new long[] { cursorPos[0] - radius, cursorPos[1] - radius };
			long[] max = new long[] { cursorPos[0] + radius, cursorPos[1] + radius };
			FinalInterval interval = new FinalInterval(min, max);

			IntervalView<T> subsetView = Views.interval(extendedInput, interval);
			Cursor<T> subsetCursor = subsetView.cursor();
			while (subsetCursor.hasNext()) {
				long[] subsetCursorPos = new long[2];
				subsetCursor.localize(subsetCursorPos);
				double y = subsetCursor.next().getRealDouble();
				double currentRange = gaussRange(x, y);
				double currentDomain = gaussDomain(cursorPos, subsetCursorPos);
				sum += (y * currentDomain * currentRange);
				normalization += currentDomain * currentRange;
			}

			outputRA.setPosition(cursor);
			double out = 1 / normalization * sum;
			outputRA.get().setReal(out);
		}
		return output;
	}

	private double gaussDomain(long[] coord1, long[] coord2) {
		double distance = Math.sqrt(Math.pow(coord1[0] - coord2[0], 2) + Math.pow(coord1[1] - coord2[1], 2));
		return (1 / (2 * Math.PI * Math.pow(domain, 2)) * Math.exp((distance) / 2 * Math.pow(domain, 2)));
	}

	private double gaussRange(double x, double y) {
		return (1 / (2 * Math.PI * Math.pow(range, 2)) * Math.exp(Math.pow(x - y, 2) / 2 * Math.pow(range, 2)));
	}

}
