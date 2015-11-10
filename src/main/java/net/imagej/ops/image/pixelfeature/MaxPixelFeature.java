package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.MaxPxFeature;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.MaxPxFeature.class, name = Ops.Image.MaxPxFeature.NAME)
public class MaxPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements MaxPxFeature {

	@Override
	protected T getValue(Neighborhood<T> neighborhood) {
		
		return ops.stats().max(neighborhood);
	}

}
