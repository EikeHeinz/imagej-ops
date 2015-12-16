package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.StdDevPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.StdDevPxFeature.class, name = Ops.Image.StdDevPxFeature.NAME)
public class StdDevPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements StdDevPxFeature {

	
	// TODO implement
	@Override
	public void initialize() {
	
	}
	
	
	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> in) {

		return null;
	}

}
