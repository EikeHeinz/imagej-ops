package net.imagej.ops.image.pixelfeature.neighborhoodbased;

import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.FunctionOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Create;
import net.imagej.ops.Ops.Filter.Max;
import net.imagej.ops.Ops.Image.StdDevPxFeature;
import net.imagej.ops.Ops.Stats.StdDev;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.StdDevPxFeature.class, name = Ops.Image.StdDevPxFeature.NAME)
public class StdDevPixelFeature<T extends RealType<T>> extends AbstractNeighborhoodPixelFeatureOp<T> implements StdDevPxFeature {

	
	// TODO implement
	@Override
	public void initialize() {
	
	}
	
	
	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> in) {

		return null;
	}

}
