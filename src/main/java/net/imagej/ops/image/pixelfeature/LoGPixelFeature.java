package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.LoGPxFeature.class, name = Ops.Image.LoGPxFeature.NAME)
public class LoGPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	@Parameter
	private double sigma;

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> loGOp;
	
	private RandomAccessibleInterval<T> output;

	@Override
	public void initialize() {
		output = ops().create().img(in());
		RandomAccessibleInterval<T> kernel = ops().create().kernelLog(in().numDimensions(), sigma);
		loGOp = Computers.unary(ops(), Convolve.class, in(), in(), kernel);
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		loGOp.compute1(input, output);

		return output;
	}

}
