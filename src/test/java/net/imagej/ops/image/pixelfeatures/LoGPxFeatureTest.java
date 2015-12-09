package net.imagej.ops.image.pixelfeatures;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

public class LoGPxFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[] { 500, 500 });

		Cursor<FloatType> cursor = img1.cursor();
		while (cursor.hasNext()) {
			cursor.next().setZero();
		}
		RandomAccessibleInterval<FloatType> out = ops.image().loGPxFeature(img1, 1.0d);
		ImageJFunctions.show(img1);
		System.out.println("breakpoint");
	}

}
