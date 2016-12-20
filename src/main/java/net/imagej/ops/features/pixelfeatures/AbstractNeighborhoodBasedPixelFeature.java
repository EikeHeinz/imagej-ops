
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;

public abstract class AbstractNeighborhoodBasedPixelFeature<T extends RealType<T>>
	extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
{

	@Parameter
	protected int span;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	protected UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> filterOp;

	@Override
	public void initialize() {
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
	}

	@Override
	public RandomAccessibleInterval<T> calculate(
		final RandomAccessibleInterval<T> in)
	{
		RandomAccessibleInterval<T> out = createRAI.calculate(in);
		filterOp.compute(Views.interval(Views.extendMirrorDouble(in), in), out);
		return out;
	}
}
