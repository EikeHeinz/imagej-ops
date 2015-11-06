package net.imagej.ops.image.pixelfeature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.Ops.Filter.Min;
import net.imagej.ops.cached.CachedOpEnvironment;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

public abstract class AbstractStatPixelFeatureOp<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	@Parameter(type = ItemIO.INPUT)
	protected int delta;

	@Parameter(type = ItemIO.INPUT, required = false)
	protected boolean skipCenter = false;

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {

		// CachedOpEnvironment env = new CachedOpEnvironment(ops);
		// FIXME outofbounds strategy? - (Mirroring) necessary?

		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedInput = Views
				.extendMirrorDouble(input);
		RandomAccess<T> inRA = extendedInput.randomAccess();

		RandomAccessibleInterval<T> output = ops.create().img(input);
//		RandomAccess<T> outLocation = output.randomAccess();
		Cursor<T> outCursor = Views.iterable(output).cursor();
		// iterate over each pixel and create the corresponding neighborhood
//		Cursor<T> center = Views.iterable(input).localizingCursor();
		long[] position = new long[input.numDimensions()];

		// consider each pixel with delta distance to center point - check
		// whether values are correct FIXME
		RectangleShape shape = new RectangleShape(2 * (delta - 1) + 1, skipCenter);
		// T value = (T) ops.create().nativeType(outLocation.get());
		for (Neighborhood<T> localNeighborhood : shape.neighborhoods(input)) {
			//Neighborhood<T> temp = (Neighborhood<T>) ops.create().img(localNeighborhood);
			outCursor.next();
//			ops.filter().min(temp, localNeighborhood, shape);
			T value = getValue(localNeighborhood);
			outCursor.get().set(value);

			// ComputerOp<Neighborhood, T> comp = (ComputerOp<Neighborhood, T>)
			// env.computer(Min.class, value.getClass(),
			// Neighborhood.class);//createComputer(value);
			// comp.compute(localNeighborhood, value);

			// center.localize(position);
			// outLocation.setPosition(position);
			// outLocation.get().set(value);
		}
		return output;
	}

	protected abstract ComputerOp<Neighborhood, T> createComputer(T value);

	protected abstract T getValue(Neighborhood<T> neighborhood);

}
