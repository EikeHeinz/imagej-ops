
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.MedianFeature;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.MedianFeature.class)
public class RectangleMedianPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodStatBasedPixelFeature<T>
		implements MedianFeature {

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		super.initialize();
		int currentRectangleSize = 3;
		int maxRectangleSize = (span % 2 == 0) ? span - 1 : span;
		int arraySize = (maxRectangleSize - currentRectangleSize) / 2 + 1;
		filterOp = new UnaryComputerOp[arraySize];
		for (int i = 0; i < filterOp.length; i++) {
			filterOp[i] = Computers.unary(ops(), Ops.Filter.Median.class, in(), in(),
					new RectangleShape(currentRectangleSize, false));
			currentRectangleSize += 2;
		}
	}

}