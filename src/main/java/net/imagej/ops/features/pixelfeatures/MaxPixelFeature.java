package net.imagej.ops.features.pixelfeatures;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Pixelfeatures.MaxPixelFeature.class, name = Ops.Pixelfeatures.MaxPixelFeature.NAME)
public class MaxPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements Ops.Pixelfeatures.MaxPixelFeature {

	// TODO span = sigma
	@Parameter
	private int span;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> filterOp;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	@Override
	public void initialize() {
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
		filterOp = Computers.unary(ops(), Ops.Filter.Max.class, in(), in(), new RectangleShape(span,false));
	}

	@Override
	public RandomAccessibleInterval<T> calculate(final RandomAccessibleInterval<T> in) {
		RandomAccessibleInterval<T> out = createRAI.calculate(in);
		filterOp.compute(Views.interval(Views.extendMirrorDouble(in),in), out);
		return (RandomAccessibleInterval<T>) out;
	}

}