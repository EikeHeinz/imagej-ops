
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.MaxPixelFeature;
import net.imagej.ops.special.computer.Computers;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.MaxPixelFeature.class,
	name = Ops.Pixelfeatures.MaxPixelFeature.NAME)
public class RectangleMaxPixelFeature<T extends RealType<T>> extends
	AbstractNeighborhoodBasedPixelFeature<T> implements MaxPixelFeature
{

	@Override
	public void initialize() {
		super.initialize();
		filterOp = Computers.unary(ops(), Ops.Filter.Max.class, in(), in(),
			new RectangleShape(span, false));
	}
}
