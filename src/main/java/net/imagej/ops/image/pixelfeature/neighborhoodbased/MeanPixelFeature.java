package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.FunctionOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Create;
import net.imagej.ops.Ops.Filter.Mean;
import net.imagej.ops.Ops.Image.MeanPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.MeanPxFeature.class, name = Ops.Image.MeanPxFeature.NAME)
public class MeanPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements MeanPxFeature {


	private FunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;
	private ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> mapOp;

	@Override
	public void initialize() {
		createOp = ops().function(Create.Img.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class);
		mapOp = ops().computer(Mean.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class,
				new RectangleShape(span, false));
	}
	
	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> in) {
		final RandomAccessibleInterval<T> out = createOp.compute(in);
		mapOp.compute(Views.interval(Views.extendMirrorDouble(in), in), out);
		return out;
	}

}
