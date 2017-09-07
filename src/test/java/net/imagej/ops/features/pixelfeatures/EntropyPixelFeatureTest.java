package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class EntropyPixelFeatureTest extends AbstractOpTest {

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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().entropy(img, 1.0d, 1.0d, 3);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.5916728f, 0.86312056f, 0.86312056f, 0.86312056f, 0.86312056f, 0.9852281f,
				0.0f, 0.86312056f, 0.0f };
		float[] values2 = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			outRA.setPosition(new long[] { i, i, 0 });
			assertEquals(values2[i], outRA.get().getRealFloat(), 0.0000f);

		}
	}
}
