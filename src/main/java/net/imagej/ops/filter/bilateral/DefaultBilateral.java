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
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Filter.BilateralFilter.class)
public class DefaultBilateral<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements BilateralFilter {

	@Parameter
	private double spatial;

	@Parameter
	private double domain;

	@Parameter
	private int radius;

	@Parameter(required = false)
	private OutOfBoundsFactory<T, RandomAccessibleInterval<T>> fac;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	@Override
	public void initialize() {
		if (fac == null) {
			fac = new OutOfBoundsMirrorFactory<>(Boundary.DOUBLE);
		}
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());

	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		IntervalView<T> extendedInput = Views.interval(Views.extend(input, fac), input);
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
				double y = subsetCursor.next().getRealDouble();
				long[] subsetCursorPos = new long[2];
				subsetCursor.localize(subsetCursorPos);

				double distance = Math.sqrt(Math.pow(cursorPos[0] - subsetCursorPos[0], 2)
						+ Math.pow(cursorPos[1] - subsetCursorPos[1], 2));
				double currentSpatial = gauss(distance, spatial);
				double difference = Math.abs(x - y);
				double currentDomain = gauss(difference, domain);
				sum += (y * currentSpatial * currentDomain);
				normalization += currentSpatial * currentDomain;
			}

			outputRA.setPosition(cursor);
			double out = 1 / normalization * sum;
			outputRA.get().setReal(out);
		}
		return output;
	}

	private double gauss(final double x, final double sigma) {
		return (1 / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp((-0.5 * x * x) / (sigma * sigma));
	}

}
