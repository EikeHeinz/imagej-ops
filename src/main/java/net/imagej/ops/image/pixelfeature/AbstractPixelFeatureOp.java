package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Parameter;

import net.imagej.ops.AbstractFunctionOp;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public abstract class AbstractPixelFeatureOp<T extends RealType<T>>
		extends AbstractFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {
	
	// TODO add labeling as optional parameter?
	// TODO is this class necessary?

	@Parameter
	protected OpService ops;

}
