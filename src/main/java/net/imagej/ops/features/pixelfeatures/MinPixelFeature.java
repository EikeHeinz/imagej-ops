package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Min;
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

@Plugin(type = Ops.Pixelfeatures.MinPixelFeature.class, name = Ops.Pixelfeatures.MinPixelFeature.NAME)
public class MinPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements Ops.Pixelfeatures.MinPixelFeature {

	@Parameter
	private int span;

	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> mapOp;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createRAIFromRAI;

	@Override
	public void initialize() {
		createRAIFromRAI = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class, in());
		mapOp = Computers.unary(ops(), Min.class, RandomAccessibleInterval.class, in(),
				new RectangleShape(span, false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> in) {
		RandomAccessibleInterval<T> output = createRAIFromRAI.calculate(in);
		mapOp.compute(Views.interval(Views.extendMirrorDouble(in), in), output);
		return output;
	}

}