package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.FunctionOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Create;
import net.imagej.ops.Ops.Filter.Max;
import net.imagej.ops.Ops.Image.MaxPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.MaxPxFeature.class, name = Ops.Image.MaxPxFeature.NAME)
public class MaxPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements MaxPxFeature {



	private FunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createOp;

	private ComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> mapOp;

	@Override
	public void initialize() {
		createOp = ops().function(Create.Img.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class);
		mapOp = ops().computer(Max.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class,
				new RectangleShape(span, skipCenter));
	}

	@Override
	public RandomAccessibleInterval<T> compute(final RandomAccessibleInterval<T> in) {
		final RandomAccessibleInterval<T> out = createOp.compute(in);
		// TODO check extension
		mapOp.compute(Views.interval(Views.extendMirrorDouble(in), in), out);
		return out;
	}

}
