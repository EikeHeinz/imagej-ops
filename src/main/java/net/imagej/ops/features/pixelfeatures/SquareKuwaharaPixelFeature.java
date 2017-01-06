
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.SquareKuwahara;
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

@Plugin(type = SquareKuwahara.class)
public class SquareKuwaharaPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements SquareKuwahara
{

	@Parameter
	private int kernelSize = 5;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	@Override
	public void initialize() {
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());
	}

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
			T mean1 = ops().stats().mean(region1);
			T stddev1 = ops().stats().stdDev(region1);

			// REGION 2
			FinalInterval region2Interval = FinalInterval.createMinMax(position[0],
				position[1] - halfsize, position[0] + halfsize, position[1]);
			IntervalView<T> region2 = Views.interval(extendedInput, region2Interval);
			T mean2 = ops().stats().mean(region2);
			T stddev2 = ops().stats().stdDev(region2);

			// REGION 3
			FinalInterval region3Interval = FinalInterval.createMinMax(position[0] -
				halfsize, position[1], position[0], position[1]+halfsize);
			IntervalView<T> region3 = Views.interval(extendedInput, region3Interval);
			T mean3 = ops().stats().mean(region3);
			T stddev3 = ops().stats().stdDev(region3);

			// REGION 4
			FinalInterval region4Interval = FinalInterval.createMinMax(position[0],
				position[1], position[0] + halfsize, position[1] + halfsize);
			IntervalView<T> region4 = Views.interval(extendedInput, region4Interval);
			T mean4 = ops().stats().mean(region4);
			T stddev4 = ops().stats().stdDev(region4);

			T minCandidate1 = (stddev1.getRealDouble() < stddev2.getRealDouble())
				? stddev1 : stddev2;
			T minCandidate2 = (stddev3.getRealDouble() < stddev4.getRealDouble())
				? stddev3 : stddev4;
			T minStdDev = (minCandidate1.getRealDouble() < minCandidate2
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
