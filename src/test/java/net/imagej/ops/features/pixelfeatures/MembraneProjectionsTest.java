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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().membraneProjections(img);
		RandomAccess<FloatType> outRA = out.randomAccess();
		float[] valuesSum = new float[] { 5.6977525f, 4.042903f, 0.092166655f, -9.181864E-8f, -5.2038303E-9f,
				-3.4967462E-7f, -1.2869846E-7f, 1.7815857E-8f, 1.7521361f, 2.426858f };
		float[] valuesMean = new float[] { 0.18992509f, 0.13476342f, 0.003072222f, -3.0606213E-9f, -1.7346101E-10f,
				-1.165582E-8f, -4.2899484E-9f, 5.938619E-10f, 0.058404535f, 0.08089527f };
		float[] valuesStdDev = new float[] { 0.38703936f, 0.3064844f, 0.012186458f, 4.0971667E-8f, 3.8031022E-8f,
				1.9334514E-8f, 3.209443E-8f, 3.1700917E-8f, 0.19785716f, 0.27678636f };
		float[] valuesMedian = new float[] { -3.4134402E-9f, 1.8946128E-8f, 9.934108E-10f, -4.9594435E-9f, 0.0f,
				-8.940697E-9f, -5.3236726E-10f, -5.804542E-9f, 1.4901161E-8f, 1.1904074E-8f };
		float[] valuesMax = new float[] { 1.0191867f, 0.95304805f, 0.059171356f, 9.536743E-8f, 7.251386E-8f,
				3.264064E-8f, 6.357829E-8f, 8.684956E-8f, 1.0126531f, 1.1701688f };
		float[] valuesMin = new float[] { -5.268387E-8f, -4.86494E-8f, -4.7820652E-8f, -9.536743E-8f, -1.2715658E-7f,
				-4.4774442E-8f, -1.2715658E-7f, -3.7606963E-8f, -4.0978193E-8f, -4.7683717E-8f };
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
