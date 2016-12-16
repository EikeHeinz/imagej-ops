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

@Plugin(type = Ops.Pixelfeatures.MeanPixelFeature.class, name = Ops.Pixelfeatures.MeanPixelFeature.NAME)
public class MeanPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements Ops.Pixelfeatures.MeanPixelFeature {

	@Parameter
	private int span;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> filterOp;

	@Override
	public void initialize() {
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
		filterOp = Computers.unary(ops(), Ops.Filter.Mean.class, in(), in(), new RectangleShape(span, false));
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> in) {
		RandomAccessibleInterval<T> out = createRAI.calculate(in);
		filterOp.compute(Views.interval(Views.extendMirrorDouble(in), in), out);
		return (RandomAccessibleInterval<T>) out;
	}

}