
package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Min;
import net.imagej.ops.Ops.Image.MinPxFeature;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.MinPxFeature.class, name = Ops.Image.MinPxFeature.NAME)
public class MinPixelFeature<T extends RealType<T>> extends
	AbstractNeighborhoodPixelFeatureOp<T> implements MinPxFeature
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
		mapOp = Computers.unary(ops(), Min.class, RandomAccessibleInterval.class,
			in(), new RectangleShape(span, false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> in) {
		RandomAccessibleInterval<T> output = createRAIFromRAI.compute1(in);
		mapOp.compute1(Views.interval(Views.extendMirrorDouble(in), in), output);
		return output;
	}

}
