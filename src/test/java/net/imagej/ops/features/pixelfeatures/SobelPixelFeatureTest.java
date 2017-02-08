package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class SobelPixelFeatureTest extends AbstractOpTest {

	@Test
	public <T extends RealType<T>> void test() {
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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().sobel(img, 1.0d, 4.0d);

		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = { 0.025049314f, 0.16272473f, 0.011789966f, 0.3250164f, 0.5826428f, 0.037357558f, 1.4774734f,
				0.88723177f, 0.05076311f, 2.2316833f, 0.7549961f, 0.04029004f, 0.86892176f, 0.28092527f, 0.013144224f,
				0.872577f, 0.30680498f, 0.02432682f, 2.2526066f, 0.7846407f, 0.05082421f, 1.4824845f, 0.9008621f,
				0.05873185f, 0.30883065f, 0.57694614f, 0.04158816f, 0.021617193f, 0.15837532f, 0.012854086f };
		int j = 0;
		for (int i = 0; i < 10; i++) {
			int[] position = new int[3];
			position[0] = i;
			position[1] = i;
			position[2] = 0;
			outRA.setPosition(position);
//			assertEquals(values[j], outRA.get().getRealFloat(), 0.000000001f);
			position[2] = 1;
			outRA.setPosition(position);
//			assertEquals(values[j + 1], outRA.get().getRealFloat(), 0.000000001f);
			position[2] = 2;
			outRA.setPosition(position);
//			assertEquals(values[j + 2], outRA.get().getRealFloat(), 0.000000001f);
			j += 3;
		}
	}
}
