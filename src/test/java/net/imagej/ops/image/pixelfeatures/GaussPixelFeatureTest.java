package net.imagej.ops.image.pixelfeatures;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

public class GaussPixelFeatureTest extends AbstractOpTest {

//	@Test
	public void test() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[] { 500, 500 });

		Cursor<FloatType> cursor = img1.cursor();
		while (cursor.hasNext()) {
			cursor.next().setZero();
		}
		RandomAccess<FloatType> img1RA = img1.randomAccess();
		img1RA.setPosition(new long[] { 250, 250 });
		img1RA.get().set(200);
		ImageJFunctions.show(img1, "input");
		RandomAccessibleInterval<FloatType> out = ops.image().gaussPxFeature(img1, 1.0d, 16.0d);
		ImageJFunctions.show(out, "output");
		System.out.println("breakpoint");
	}
	
	@Test
	public void testDoG() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[] { 500, 500 });

		Cursor<FloatType> cursor = img1.cursor();
		while (cursor.hasNext()) {
			cursor.next().setZero();
		}
		RandomAccess<FloatType> img1RA = img1.randomAccess();
		img1RA.setPosition(new long[] { 250, 250 });
		img1RA.get().set(200);
		ImageJFunctions.show(img1, "input");
		RandomAccessibleInterval<FloatType> out = ops.image().dogPxFeature(img1, 1.0d, 16.0d);
		ImageJFunctions.show(out, "output");
		System.out.println("breakpoint");
	}

}
