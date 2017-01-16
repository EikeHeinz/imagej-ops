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

public class MembraneProjectionsTest extends AbstractOpTest {

	@Test
	public <T extends RealType<T>> void test() {

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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().membraneProjections(img, 1, 19);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] valuesSum = new float[] { 75.71942f, 100.44581f, 126.2946f, 165.18263f, 224.89537f, 236.38914f,
				220.10951f, 158.82895f, 119.70989f, 86.33595f };
		float[] valuesMean = new float[] { 2.5239806f, 3.3481934f, 4.20982f, 5.506088f, 7.4965124f, 7.879638f,
				7.3369837f, 5.2942986f, 3.9903297f, 2.877865f };
		float[] valuesStdDev = new float[] { 2.0307617f, 2.1009798f, 1.3697824f, 1.1668729f, 1.371079f, 2.0070376f,
				1.3115847f, 1.5655895f, 2.3294756f, 2.4602454f };
		float[] valuesMedian = new float[] { 3.035905f, 3.0790527f, 4.0478735f, 5.765988f, 7.602315f, 8.806245f,
				7.347774f, 5.2497377f, 3.3975503f, 3.0000002f };
		float[] valuesMax = new float[] { 5.4939485f, 6.7205176f, 6.791072f, 7.4696307f, 10.0f, 10.096897f, 10.0f,
				7.48015f, 8.029925f, 6.8780336f };
		float[] valuesMin = new float[] { -5.0862633E-7f, -5.0862633E-7f, 2.6952355f, 2.9999998f, 5.0827727f, 5.110183f,
				5.0827723f, 2.9999995f, 0.07234764f, -5.0862633E-7f };
		for (int i = 0; i < valuesSum.length; i++) {
			long[] pos = new long[] { i, i, 0 };
			outRA.setPosition(pos);
			assertEquals(valuesSum[i], outRA.get().getRealFloat(), 0.0000f);
			pos[2] = 1;
			outRA.setPosition(pos);
			assertEquals(valuesMean[i], outRA.get().getRealFloat(), 0.0000f);
			pos[2] = 2;
			outRA.setPosition(pos);
			assertEquals(valuesStdDev[i], outRA.get().getRealFloat(), 0.0000f);
			pos[2] = 3;
			outRA.setPosition(pos);
			assertEquals(valuesMedian[i], outRA.get().getRealFloat(), 0.0000f);
			pos[2] = 4;
			outRA.setPosition(pos);
			assertEquals(valuesMax[i], outRA.get().getRealFloat(), 0.0000f);
			pos[2] = 5;
			outRA.setPosition(pos);
			assertEquals(valuesMin[i], outRA.get().getRealFloat(), 0.0000f);

		}
	}
}
