package net.imagej.ops.image.pixelfeatures;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

public class StatPixelFeatureTest extends AbstractOpTest {

	/*
	 * uses existing ops, not tested intensively
	 */
	
	@Test
	public void test() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[]{500,500});
		
//		Cursor<FloatType> cursor = img1.cursor();
//		boolean isOdd = true;
//		while(cursor.hasNext()) {
//			if(isOdd) {
//				cursor.next().setOne();
//				isOdd = false;
//			} else {
//				cursor.next().setZero();
//				isOdd = true;
//			}
//		}
		
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
		
		int span = 2;
		
		ImageJFunctions.show(img1, "input");
		RandomAccessibleInterval<FloatType> minOut = ops.image().minPxFeature(img1, span);
		ImageJFunctions.show(minOut, "min");
		RandomAccessibleInterval<FloatType> maxOut = ops.image().maxPxFeature(img1, span);
		ImageJFunctions.show(maxOut, "max");
		RandomAccessibleInterval<FloatType> meanOut = ops.image().meanPxFeature(img1, span);
		ImageJFunctions.show(meanOut, "mean");
		System.out.println("breakpoint");
	}

}
