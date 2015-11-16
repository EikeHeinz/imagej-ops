package net.imagej.ops.image.pixelfeatures;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

public class StatPixelFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[]{10,10});
		
		Cursor<FloatType> cursor = img1.cursor();
		boolean isOdd = true;
		while(cursor.hasNext()) {
			if(isOdd) {
				cursor.next().set(200);
				isOdd = false;
			} else {
				cursor.next().setZero();
				isOdd = true;
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
//		RandomAccessibleInterval<FloatType> stdDevOut = ops.image().stdDevPxFeature(img1, span);
//		ImageJFunctions.show(stdDevOut, "stddev");
		System.out.println("breakpoint");
	}

}
