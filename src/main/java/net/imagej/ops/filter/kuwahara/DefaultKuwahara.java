
package net.imagej.ops.filter.kuwahara;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Kuwahara;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
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

@Plugin(type = Ops.Filter.Kuwahara.class)
public class DefaultKuwahara<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Kuwahara
{

	@Parameter
	private int kernelSize = 5;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<IntervalView, RealType> meanOp;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<IntervalView, RealType> stdDevOp;


	@Override
	public void initialize() {
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());
		meanOp = Functions.unary(ops(), Ops.Stats.Mean.class, RealType.class, IntervalView.class);
		stdDevOp = Functions.unary(ops(), Ops.Stats.StdDev.class, RealType.class, IntervalView.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> calculate(
		RandomAccessibleInterval<T> input)
	{
		Cursor<T> inputCursor = Views.iterable(input).cursor();
		RandomAccessibleInterval<T> output = createOp.calculate(input);
		RandomAccess<T> outputRA = output.randomAccess();

		while (inputCursor.hasNext()) {
			inputCursor.next();
			long[] position = new long[2];
			inputCursor.localize(position);
			int halfsize = kernelSize / 2;
			IntervalView<T> extendedInput = Views.interval(Views.extendMirrorDouble(
				input), input);

			// REGION 1
			FinalInterval region1Interval = FinalInterval.createMinMax(position[0] -
				halfsize, position[1] - halfsize, position[0], position[1]);
			IntervalView<T> region1 = Views.interval(extendedInput, region1Interval);
			 RealType<T> mean1 = meanOp.calculate(region1);
			 RealType<T> stddev1 = stdDevOp.calculate(region1);

			// REGION 2
			FinalInterval region2Interval = FinalInterval.createMinMax(position[0],
				position[1] - halfsize, position[0] + halfsize, position[1]);
			IntervalView<T> region2 = Views.interval(extendedInput, region2Interval);
			RealType<T> mean2 = meanOp.calculate(region2);
			RealType<T> stddev2 = stdDevOp.calculate(region2);

			// REGION 3
			FinalInterval region3Interval = FinalInterval.createMinMax(position[0] -
				halfsize, position[1], position[0], position[1]+halfsize);
			IntervalView<T> region3 = Views.interval(extendedInput, region3Interval);
			RealType<T> mean3 = meanOp.calculate(region3);
			RealType<T> stddev3 = stdDevOp.calculate(region3);

			// REGION 4
			FinalInterval region4Interval = FinalInterval.createMinMax(position[0],
				position[1], position[0] + halfsize, position[1] + halfsize);
			IntervalView<T> region4 = Views.interval(extendedInput, region4Interval);
			RealType<T> mean4 = meanOp.calculate(region4);
			RealType<T> stddev4 = stdDevOp.calculate(region4);

			RealType<T> minCandidate1 = (stddev1.getRealDouble() < stddev2.getRealDouble())
				? stddev1 : stddev2;
			RealType<T> minCandidate2 = (stddev3.getRealDouble() < stddev4.getRealDouble())
				? stddev3 : stddev4;
			RealType<T> minStdDev = (minCandidate1.getRealDouble() < minCandidate2
				.getRealDouble()) ? minCandidate1 : minCandidate2;

			outputRA.setPosition(inputCursor);
			if (stddev1.getRealDouble() == minStdDev.getRealDouble()) {
				outputRA.get().setReal(mean1.getRealDouble());
				// mean1
			}
			else if (stddev2.getRealDouble() == minStdDev.getRealDouble()) {
				outputRA.get().setReal(mean2.getRealDouble());
				// mean 2
			}
			else if (stddev3.getRealDouble() == minStdDev.getRealDouble()) {
				outputRA.get().setReal(mean3.getRealDouble());
				// mean 3
			}
			else if (stddev4.getRealDouble() == minStdDev.getRealDouble()) {
				outputRA.get().setReal(mean4.getRealDouble());
				// mean 4
			}
			else {
				System.err.println("Whoops, something went wrong");
			}

		}
		return output;
	}

}
