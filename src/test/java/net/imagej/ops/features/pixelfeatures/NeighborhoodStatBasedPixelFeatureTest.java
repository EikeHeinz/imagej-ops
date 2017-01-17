package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Before;
import org.junit.Test;

public class NeighborhoodStatBasedPixelFeatureTest extends AbstractOpTest {

	private ArrayImg<FloatType, FloatArray> img;

	@Before
	public void init() {
		img = generateFloatArrayTestImg(false, new long[] { 10, 10 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 4 && counterX < 6 || counterY > 4 && counterY < 6) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 10 == 0) {
				counterY++;
			}
			if (counterX == 10) {
				counterX = 0;
			}
			if (counterY == 10) {
				counterY = 0;
			}
		}
	}

	@Test
	public void testMax() {
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().max(img, 3);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}

	@Test
	public void testMin() {
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().min(img, 3);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}

	@Test
	public void testMean() {
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().mean(img, 3);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.26530612f, 0.26530612f, 0.26530612f, 0.26530612f, 0.26530612f,
				0.26530612f, 0.26530612f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}

	@Test
	public void testMedian() {
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().median(img, 3);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}

	@Test
	public void testVariance() {
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().variance(img, 3);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] values = new float[] { 0.0f, 0.0f, 0.19897959f, 0.19897959f, 0.19897959f, 0.19897959f, 0.19897959f, 0.19897959f, 0.19897959f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}
}
