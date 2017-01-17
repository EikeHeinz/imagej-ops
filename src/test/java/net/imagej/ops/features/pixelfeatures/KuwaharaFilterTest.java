
package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class KuwaharaFilterTest extends AbstractOpTest {

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

		RandomAccessibleInterval<FloatType> output = ops.pixelfeature().kuwahara(img,5);
		RandomAccess<FloatType> outputRA = output.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.8888889f, 1.0f, 0.0f, 0.0f, 0.0f };

		for (int i = 0; i < values.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outputRA.setPosition(pos);
			assertEquals(values[i], outputRA.get().getRealFloat(), 0.0000f);
		}
	}
}
