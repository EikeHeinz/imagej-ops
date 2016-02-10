
package net.imagej.ops.image.pixelfeatures;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

public class LoGPxFeatureTest extends AbstractOpTest {
	
	/*
	 * Originally this was tested with an image that was loaded from harddrive.
	 * The image was then compared to the image that was already filtered.
	 */

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 500,
			500 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 240 && counterX < 260 || counterY > 120000 &&
				counterY < 130000)
			{
				cursorImg.next().setOne();
			}
			else {
				cursorImg.next().setZero();
			}
			counterX++;
			counterY++;
			if (counterX == 500) {
				counterX = 0;
			}
		}
		RandomAccessibleInterval<FloatType> out = ops.image().loGPxFeature(img,
			1.4d);
		ImageJFunctions.show(out);
		System.out.println("breakpoint");
	}

}
