package net.imagej.ops.image.pixelfeature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Create;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.Ops.Image.LoGPxFeature;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryComputerOp;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.LoGPxFeature.class, name = Ops.Image.LoGPxFeature.NAME)
public class LoGPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements LoGPxFeature {

	@Parameter(type = ItemIO.INPUT)
	private double sigma;

	private UnaryFunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createRAIOp;

	private UnaryComputerOp<Img, Img> loGOp;

	@Override
	public void initialize() {
		createRAIOp = Functions.unary(ops(), Create.Img.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class);
		RandomAccessibleInterval<T> kernel = ops().create().kernelLog(in().numDimensions(), sigma);
		loGOp = Computers.unary(ops(), Convolve.class, Img.class, Img.class, kernel);
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = createRAIOp.compute1(input);
		// TODO fix casts
		loGOp.compute1((Img<T>) input, (Img<T>) output);

		return output;
	}

}
