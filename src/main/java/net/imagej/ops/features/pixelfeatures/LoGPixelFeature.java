
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;

public class LoGPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
{

	@Parameter
	private double sigma;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> loGOp;

	private RandomAccessibleInterval<T> output;

	@Override
	public void initialize() {
		output = ops().create().img(in());
		RandomAccessibleInterval<T> kernel = ops().create().kernelLog(in()
			.numDimensions(), sigma);
		loGOp = Computers.unary(ops(), Convolve.class, in(), in(), kernel);
	}

	@Override
	public RandomAccessibleInterval<T> compute1(
		RandomAccessibleInterval<T> input)
	{
		loGOp.compute1(input, output);

		return output;
	}

}
