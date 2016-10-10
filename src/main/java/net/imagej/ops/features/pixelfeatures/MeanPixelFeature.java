package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Mean;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.MeanPixelFeature.class,
	name = Ops.Pixelfeatures.MeanPixelFeature.NAME)
public class MeanPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Ops.Pixelfeatures.MeanPixelFeature
{

	@Parameter
	private int span;

	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> mapOp;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createRAIFromRAI;

	@Override
	public void initialize() {
		createRAIFromRAI = Functions.unary(ops(), Ops.Create.Img.class,
			RandomAccessibleInterval.class, in());
		mapOp = Computers.unary(ops(), Mean.class, RandomAccessibleInterval.class,
			in(), new RectangleShape(span, false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> in) {
		final RandomAccessibleInterval<T> out = createRAIFromRAI.compute1(in);
		mapOp.compute1(Views.interval(Views.extendMirrorDouble(in), in), out);
		return out;
	}

}