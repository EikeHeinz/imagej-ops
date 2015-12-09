package net.imagej.ops.image.pixelfeature;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.ComputerOp;
import net.imagej.ops.FunctionOp;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Create;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.Ops.Image.LoGPxFeature;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Image.LoGPxFeature.class, name = Ops.Image.LoGPxFeature.NAME)
public class LoGPixelFeature<T extends RealType<T>> extends AbstractPixelFeatureOp<T> implements LoGPxFeature {
	
	@Parameter(type=ItemIO.INPUT)
	private double sigma;
	
	private FunctionOp<RandomAccessibleInterval, RandomAccessibleInterval> createRAIOp;

	private ComputerOp<Img, Img> loGOp;
	
	@Override
	public void initialize() {
		createRAIOp = ops().function(Create.Img.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class);
		RandomAccessibleInterval<T> kernel = ops().create().kernelLog(in().numDimensions(), sigma);
		loGOp = ops().computer(Convolve.class, Img.class, Img.class, kernel);
	}

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = createRAIOp.compute(input);
		// TODO fix casts
		loGOp.compute((Img<T>)input, (Img<T>)output);
		
		return output;
	}

}
