package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Min;
import net.imagej.ops.Ops.Image.MinPxFeature;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.MinPxFeature.class, name = Ops.Image.MinPxFeature.NAME)
public class MinPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T>
		implements MinPxFeature {

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> mapOp;
	private RandomAccessibleInterval<T> output;

	@Override
	public void initialize() {
		output = ops().create().img(in());
		mapOp = Computers.unary(ops(), Min.class, output, in(),
				new RectangleShape(span, false));
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> in) {
		mapOp.compute1(Views.interval(Views.extendMirrorDouble(in), in), output);
		return output;
	}

}
