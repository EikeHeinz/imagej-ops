package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.HessianPxFeature;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.HessianPxFeature.class, name = Ops.Image.HessianPxFeature.NAME)
public class HessianPixelFeatureOp<T extends RealType<T>> extends AbstractPixelFeatureOp<T>
		implements HessianPxFeature {

	@Override
	public RandomAccessibleInterval<T> compute(RandomAccessibleInterval<T> input) {
		// RandomAccessibleInterval<DoubleType> doubleInput =
		// Converters.convert(input, new ConvertToDoubleType(),
		// new DoubleType());
		// TODO init()
		RandomAccessibleInterval<T> kernelX = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { 1, 2, 1, 0, 0, 0, -1, -2, -1 }, 3L, 3L);
		RandomAccessibleInterval<T> kernelY = (RandomAccessibleInterval<T>) ArrayImgs
				.doubles(new double[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 }, 3L, 3L);

		RandomAccessibleInterval<T> convolveX = (RandomAccessibleInterval<T>) ops().filter().convolve((Img<T>) input,
				kernelX);
		RandomAccessibleInterval<T> convolveXX = (RandomAccessibleInterval<T>) ops().filter()
				.convolve((Img<T>) convolveX, kernelX);
		RandomAccessibleInterval<T> convolveXY = (RandomAccessibleInterval<T>) ops().filter()
				.convolve((Img<T>) convolveX, kernelY);

//		ImageJFunctions.show(convolveXX, "XX");
//		ImageJFunctions.show(convolveXY, "XY");

		RandomAccessibleInterval<T> convolveY = (RandomAccessibleInterval<T>) ops().filter().convolve((Img<T>) input,
				kernelY);
		RandomAccessibleInterval<T> convolveYX = (RandomAccessibleInterval<T>) ops().filter()
				.convolve((Img<T>) convolveY, kernelX);
		RandomAccessibleInterval<T> convolveYY = (RandomAccessibleInterval<T>) ops().filter()
				.convolve((Img<T>) convolveY, kernelY);

//		ImageJFunctions.show(convolveYX, "YX");
//		ImageJFunctions.show(convolveYY, "YY");

		// output px value is: [[XX,XY],[YX,YY]] = [[a,b],[c,d]]
		// Trace T=a+d
		// Determinant D=ad-bc
		// Eigenvalues: L1 = (1/2)*(T + sqrt(T^2 - 4D))
		// L2 = T/2 - sqrt(T^2/4-D)

		

		// - END TODO

		return null;
	}

}
