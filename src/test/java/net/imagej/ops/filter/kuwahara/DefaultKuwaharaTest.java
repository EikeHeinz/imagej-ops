package net.imagej.ops.filter.kuwahara;

import static org.junit.Assert.*;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.Test;

public class DefaultKuwaharaTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 20, 20 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 7 && counterX < 13 || counterY > 7 && counterY < 13) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}

			counterX++;
			if (counterX % 20 == 9) {
				counterY++;
			}
			if (counterX == 20) {
				counterX = 0;
			}
			if (counterY == 20) {
				counterY = 0;
			}
		}

		RandomAccessibleInterval<FloatType> output = ops.filter().kuwahara(img, 5);
		Cursor<FloatType> outCursor = Views.iterable(output).cursor();
		System.out.println("outputimage-----------------------");
		String values = "";
		int counter = 0;
		while (outCursor.hasNext()) {
			FloatType value = outCursor.next();
			values += value + "|";
			counter++;
			if (counter == 20) {
				counter = 0;
				System.out.println(values);
				values = "";
			}
		}
		fail("Not yet implemented");
	}

}
