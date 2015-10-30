package net.imagej.ops.image.pixelfeature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

import net.imagej.ops.FunctionOp;
import net.imagej.ops.cached.CachedOpEnvironment;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public abstract class AbstractStatPixelFeatureOp<T extends RealType<T>> extends AbstractPixelFeatureOp<T>{
	
	
	@Parameter(type=ItemIO.INPUT)
	protected int delta;
	
	@Parameter(type=ItemIO.INPUT, required = false)
	protected boolean skipCenter;
	
	protected CachedOpEnvironment env = new CachedOpEnvironment(ops);
	
	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
				
		// FIXME outofbounds strategy?
		
		RandomAccessibleInterval<T> output = ops.create().img(input);
		RandomAccess<T> outLocation = output.randomAccess();
		// iterate over each pixel and create the corresponding neighborhood
		Cursor<T> center = Views.iterable(input).localizingCursor();
		center.next();
		long[] position = new long[input.numDimensions()];
		// consider each pixel with delta distance to center point
		RectangleShape shape = new RectangleShape(2*(delta-1)+1, skipCenter);
		for(Neighborhood<T> localNeighborhood : shape.neighborhoods(input)) {
			
			// TODO create Function and calculate output
			
			center.localize(position);
			outLocation.setPosition(position);
			//outLocation.get().set(value);	
			
			// take next pixel as center
			center.next();
		}
		return output;
	}
	
	protected abstract FunctionOp<Neighborhood, T> composeFunction();

}
