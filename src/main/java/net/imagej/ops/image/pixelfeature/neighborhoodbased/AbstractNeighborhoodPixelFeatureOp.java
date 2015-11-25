package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import net.imagej.ops.image.pixelfeature.AbstractPixelFeatureOp;
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

	// skipcenter always false? -> not necessary?
	@Parameter(type = ItemIO.INPUT, required = false)
	protected boolean skipCenter = false;
}
