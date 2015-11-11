package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.StdDevPxFeature;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.StdDevPxFeature.class, name = Ops.Image.StdDevPxFeature.NAME)
public class StdDevPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements StdDevPxFeature {

	@Override
	protected T getValue(Neighborhood<T> neighborhood) {
		return ops.stats().stdDev(neighborhood);
	}

}
