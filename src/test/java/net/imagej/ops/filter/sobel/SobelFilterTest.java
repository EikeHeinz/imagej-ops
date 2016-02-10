package net.imagej.ops.filter.sobel;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class SobelFilterTest extends AbstractOpTest {

	@Test
	public void test() {
				
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] {
				500, 500 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if(counterX > 240 && counterX < 260 || counterY > 120000 && counterY < 130000) {
				cursorImg.next().set(1);
			} else {
			cursorImg.next().setZero();
			}
			counterX++;
			counterY++;
			if(counterX == 500) {
				counterX =0;
			}
		}
		ImageJFunctions.show(img, "input");
		RandomAccessibleInterval<FloatType> out = ops.filter().sobel(img);
		ImageJFunctions.show(out);
		System.out.println("breakpoint");
	}
		

}
