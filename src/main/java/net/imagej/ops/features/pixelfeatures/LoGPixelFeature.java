package net.imagej.ops.features.pixelfeatures;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

@Plugin(type = Ops.Pixelfeatures.LoGPixelFeature.class,
name = Ops.Pixelfeatures.LoGPixelFeature.NAME)
public class LoGPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Ops.Pixelfeatures.LoGPixelFeature
{

	@Parameter
	private double sigma;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> loGOp;

	// TODO is this all?
	
	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernel = ops().create().kernelLog(sigma, in()
				.numDimensions(), Util.getTypeFromInterval(in()));
		loGOp = RAIs.function(ops(), Convolve.class, in(), kernel);
	}

	@Override
	public RandomAccessibleInterval<T> compute1(
		RandomAccessibleInterval<T> input)
	{
		return loGOp.compute1(input);
	}

}