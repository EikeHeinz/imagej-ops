package net.imagej.ops.features.pixelfeatures;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.IterableInterval;
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

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, IterableInterval> createInterval;

	@Override
	public void initialize() {
		createInterval = Functions.unary(ops(), Ops.Create.Img.class,
				IterableInterval.class, in());
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> calculate(final RandomAccessibleInterval<T> in) {
		IterableInterval<T> output = createInterval.calculate(in);

		// TODO use init method -- HACK
		ops().filter().max(output, Views.interval(Views.extendMirrorDouble(in),in), new RectangleShape(span, true));
		return (RandomAccessibleInterval<T>) output;
	}

}