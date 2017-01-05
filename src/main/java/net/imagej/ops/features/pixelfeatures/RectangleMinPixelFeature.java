
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.MinPixelFeature;
import net.imagej.ops.special.computer.Computers;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.MinPixelFeature.class,
	name = Ops.Pixelfeatures.MinPixelFeature.NAME)
public class RectangleMinPixelFeature<T extends RealType<T>> extends
	AbstractNeighborhoodBasedPixelFeature<T> implements MinPixelFeature
{

	@Override
	public void initialize() {
		super.initialize();
		filterOp = Computers.unary(ops(), Ops.Filter.Min.class, in(), in(),
			new RectangleShape(span, false));
	}
}