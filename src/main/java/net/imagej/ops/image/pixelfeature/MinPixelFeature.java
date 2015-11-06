package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Min;
import net.imagej.ops.Ops.Image.MinPxFeature;
import net.imglib2.Cursor;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.MinPxFeature.class, name = Ops.Image.MinPxFeature.NAME)
public class MinPixelFeature<T extends RealType<T>> extends AbstractStatPixelFeatureOp<T> implements MinPxFeature {

	@Override
	protected ComputerOp<Neighborhood, T> createComputer(T value) {
		//return (ComputerOp<Neighborhood, T>) env.computer(Min.class, value.getClass(), Neighborhood.class);
		return null;
	}

	@Override
	protected T getValue(Neighborhood<T> neighborhood) {
		Cursor<T> cursor = neighborhood.cursor();
		T value = cursor.next();
		while(cursor.hasNext()) {
			T temp = cursor.next();
			if(value.compareTo(temp) < 0) {
				value = temp;
			}
		}
		return value;
	}

}
