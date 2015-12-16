package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Create;
import net.imagej.ops.Ops.Filter.Mean;
import net.imagej.ops.Ops.Image.MeanPxFeature;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryComputerOp;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.MeanPxFeature.class, name = Ops.Image.MeanPxFeature.NAME)
public class MeanPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T>
		implements MeanPxFeature {

	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> mapOp;

	@Override
	public void initialize() {
		createOp = Functions.unary(ops(), Create.Img.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class);
		mapOp = Computers.unary(ops(), Mean.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class,
				new RectangleShape(span, false));
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> in) {
		final RandomAccessibleInterval<T> out = createOp.compute1(in);
		mapOp.compute1(Views.interval(Views.extendMirrorDouble(in), in), out);
		return out;
	}

}
