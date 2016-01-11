package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Mean;
import net.imagej.ops.Ops.Image.MeanPxFeature;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.MeanPxFeature.class, name = Ops.Image.MeanPxFeature.NAME)
public class MeanPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T>
		implements MeanPxFeature {

//	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> mapOp;
	private RandomAccessibleInterval<T> output;

	@Override
	public void initialize() {
		output = ops().create().img(in());
		mapOp = Computers.unary(ops(), Mean.class, output, in(),
				new RectangleShape(span, false));
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> in) {
//		final RandomAccessibleInterval<T> out = createOp.compute1(in);
		mapOp.compute1(Views.interval(Views.extendMirrorDouble(in), in), output);
		return output;
	}

}
