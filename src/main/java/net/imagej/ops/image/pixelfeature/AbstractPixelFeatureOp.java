package net.imagej.ops.image.pixelfeature;

import net.imagej.ops.AbstractFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public abstract class AbstractPixelFeatureOp<T extends RealType<T>>
		extends AbstractFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {
	
	// TODO add labeling as optional parameter?
	// TODO is this class necessary?

}
