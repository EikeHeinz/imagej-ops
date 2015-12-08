package net.imagej.ops.image.pixelfeatures;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class GaussianGradientMagnitudePxFeatureTest extends AbstractOpTest {

	@Test
	public <T extends RealType<T>> void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] {
				500, 500 });

		Cursor<FloatType> cursor = img.cursor();
		long counter = 0;
		while (cursor.hasNext()) {
			if (counter > 225000 || counter < 65000) {
				cursor.next().setZero();
			} else if (counter >= 65000) {
				cursor.next().set(300);
			}
			counter++;
		}
			
		ImageJFunctions.show(img, "input");
		RandomAccessibleInterval<T> out = ops.image().gaussianGradientMagnitude((RandomAccessibleInterval<T>) img, 1.0d);
				
		ImageJFunctions.show(out, "output");
		System.out.println("breakpoint");
	}

}
