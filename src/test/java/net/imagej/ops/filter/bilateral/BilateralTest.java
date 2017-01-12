package net.imagej.ops.filter.bilateral;

import static org.junit.Assert.*;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.Test;

public class BilateralTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 10, 10 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 3 && counterX < 6 || counterY > 3 && counterY < 6) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 10 == 9) {
				counterY++;
			}
			if (counterX == 10) {
				counterX = 0;
			}
			if (counterY == 10) {
				counterY = 0;
			}
		}
		
		RandomAccessibleInterval<FloatType> out = ops.filter().bilateralFilter(img, 1.0d, 1.0d, 0);
		Cursor<FloatType> outCursor = Views.iterable(out).cursor();
		int counter = 0;
		String values = "";
		while(outCursor.hasNext()) {
			FloatType value = outCursor.next();
			values += value + "|";
			counter++;
			if(counter == 10) {
				System.out.println(values);
				values = "";
				counter = 0;
			}
		}
		fail("Not yet implemented");
	}

}
