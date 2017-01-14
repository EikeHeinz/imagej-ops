package net.imagej.ops.filter.bilateral;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class BilateralTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 10, 10 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 3 && counterX < 7 || counterY > 3 && counterY < 7) {
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

		RandomAccessibleInterval<FloatType> out = ops.filter().bilateral(img, 5.0d, 5.0d, 0);

		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			long[] pos = new long[] { i, i };
			outRA.setPosition(pos);
			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}
}
