package net.imagej.ops.filter.kuwahara;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class DefaultKuwaharaTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 10, 10 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 3 && counterX < 7 || counterY > 3 && counterY < 3) {
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

		RandomAccessibleInterval<FloatType> output = ops.filter().kuwahara(img, 5);
		RandomAccess<FloatType> outputRA = output.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.6666667f, 1.0f, 0.0f, 0.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			long[] pos = new long[] { i, i };
			outputRA.setPosition(pos);
			assertEquals(values[i], outputRA.get().getRealFloat(), 0.0000f);
		}
	}
}
