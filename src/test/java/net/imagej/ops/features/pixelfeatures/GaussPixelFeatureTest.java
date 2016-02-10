package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class GaussPixelFeatureTest extends AbstractOpTest {
	
	/*
	 * These ops utilize already existing ops, so no intensive testing was done
	 */

	@Test
	public void test() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[] { 500, 500 });
		
		Cursor<FloatType> cursorImg = img1.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if(counterX > 240 && counterX < 260 || counterY > 120000 && counterY < 130000) {
				cursorImg.next().setOne();
			} else {
			cursorImg.next().setZero();
			}
			counterX++;
			counterY++;
			if(counterX == 500) {
				counterX =0;
			}
		}

		ImageJFunctions.show(img1, "input");
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().gaussian(img1, 1.0d, 16.0d);
		ImageJFunctions.show(out, "output");
		System.out.println("breakpoint");
	}
	
	@Test
	public void testDoG() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[] { 500, 500 });

		Cursor<FloatType> cursorImg = img1.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if(counterX > 240 && counterX < 260 || counterY > 120000 && counterY < 130000) {
				cursorImg.next().setOne();
			} else {
			cursorImg.next().setZero();
			}
			counterX++;
			counterY++;
			if(counterX == 500) {
				counterX =0;
			}
		}
		ImageJFunctions.show(img1, "input");
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().doG(img1, 1.0d, 16.0d);
		ImageJFunctions.show(out, "output");
		System.out.println("breakpoint");
	}

}
