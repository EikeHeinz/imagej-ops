package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

import net.imagej.ops.image.pixelfeature.AbstractPixelFeatureOp;
import net.imglib2.type.numeric.RealType;

public abstract class AbstractNeighborhoodPixelFeatureOp<T extends RealType<T>> extends
		AbstractPixelFeatureOp<T> {

	@Parameter(type = ItemIO.INPUT)
	protected int span;

	// TODO skipcenter necessary?
	@Parameter(type = ItemIO.INPUT, required = false)
	protected boolean skipCenter = false;
}
