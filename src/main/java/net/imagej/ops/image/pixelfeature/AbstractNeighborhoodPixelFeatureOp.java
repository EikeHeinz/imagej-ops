package net.imagej.ops.image.pixelfeature;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

public abstract class AbstractNeighborhoodPixelFeatureOp<T extends RealType<T>> extends
		AbstractPixelFeatureOp<T> {

	@Parameter(type = ItemIO.INPUT)
	protected int span;

	@Parameter(type = ItemIO.INPUT, required = false)
	protected boolean skipCenter = false;

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {

		RandomAccessibleInterval<T> output = ops.create().img(input);

		Interval interval = Intervals.expand(input, -span);


		RandomAccessibleInterval<T> computationInput = Views.interval(input,
				interval);
		RandomAccessibleInterval<T> computationOutput = Views.interval(output,
				interval);

		RandomAccess<T> outLocation = computationOutput.randomAccess();

		final RectangleShape shape = new RectangleShape(span, false);

		for (Neighborhood<T> localNeighborhood : shape
				.neighborhoods(computationInput)) {

			outLocation.setPosition(localNeighborhood);
			outLocation.get().set(getValue(localNeighborhood));
		}

		return output;
	}
	
	 protected abstract T getValue(Neighborhood<T> neighborhood);
}
