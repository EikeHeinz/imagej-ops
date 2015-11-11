package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.MeanPxFeature;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Image.MeanPxFeature.class, name = Ops.Image.MeanPxFeature.NAME)
public class MeanPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements MeanPxFeature {

	@Override
	protected T getValue(Neighborhood<T> neighborhood) {
		return ops.stats().mean(neighborhood);
	}

}
