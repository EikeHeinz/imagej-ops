package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.*;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.Test;

public class BilateralFilterTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 50, 50 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 27 && counterX < 33 || counterY > 27 && counterY < 33) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 50 == 9) {
				counterY++;
			}
			if (counterX == 50) {
				counterX = 0;
			}
			if (counterY == 50) {
				counterY = 0;
			}
		}
		
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().bilateralFilter(img, 3.0d, 3.0d, 5);
//		Cursor<FloatType> outCursor = Views.iterable(out).cursor();
//		int counter = 0;
//		String values = "";
//		while(outCursor.hasNext()) {
//			FloatType value = outCursor.next();
//			values += value + "|";
//			counter++;
//			if(counter == 50) {
//				System.out.println(values);
//				values = "";
//				counter = 0;
//			}
//		}
		fail("Not yet implemented");
	}

}
