package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.MinPxFeature;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Image.MinPxFeature.class, name = Ops.Image.MinPxFeature.NAME)
public class MinPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements MinPxFeature {

	@Override
	protected T getValue(Neighborhood<T> neighborhood) {
		return ops.stats().min(neighborhood);
	}

}
