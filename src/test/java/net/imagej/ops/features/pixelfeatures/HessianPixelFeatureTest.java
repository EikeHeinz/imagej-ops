package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class HessianPixelFeatureTest extends AbstractOpTest {

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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().hessian(img, 1.0d, 8.0d);
		RandomAccess<FloatType> outRA = out.randomAccess();

		float[] valuesModule = new float[] { 1.215859f, 6.274394f, 10.344449f, 3.3476565f, 10.597554f, 10.656869f,
				3.1518977f, 9.644412f, 5.7434764f, 1.0603495f };
		float[] valuesTrace = new float[] { 1.7194724f, 8.870056f, 14.474865f, 1.1520919f, -14.936752f, -15.013784f,
				0.6746214f, 13.593287f, 8.069767f, 1.4733065f };
		float[] valuesDeterminant = new float[] { 1.5465988f, 5.1687274f, 0.6105761f, 1.5592347f, 7.891646f, 7.83646f,
				1.4087182f, 0.7815286f, 5.209663f, 1.4877121f };
		float[] valuesFirstEigenvalue = new float[] { 0.07731938f, 0.12302194f, 0.027343674f, -0.08272274f,
				-0.14813614f, -0.1431784f, -0.06911206f, 0.04690789f, 0.14140923f, 0.08497451f };
		float[] valuesSecondEigenvalue = new float[] { -0.005227004f, -0.009627844f, 0.0010324137f, 0.00929681f,
				0.0010209724f, 0.0010227921f, 0.010379163f, 0.0028024118f, -0.008842271f, -0.0050890446f };
		float[] valuesOrientation = new float[] { 0.7853981f, 0.7853978f, 0.7853966f, 0.78539747f, 0.78539807f,
				0.7853981f, 0.7853982f, 0.785397f, 0.7853964f, 0.785398f };
		float[] valuesgnsed = new float[] { 3.0031693E-16f, 4.0481537E-13f, 1.0326011E-11f, 1.8067948E-12f,
				4.5590783E-14f, 2.7069462E-15f, 3.8444407E-17f, 5.3487466E-12f, 1.25166015E-11f, 1.05098425E-13f };
		float[] valuessgned = new float[] { 1.7331047E-8f, 6.36337E-7f, 3.214762E-6f, 1.3496093E-6f, 2.2016874E-7f,
				5.6848624E-8f, 1.0864554E-8f, 2.3144146E-6f, 3.5380365E-6f, 3.2419172E-7f };

		// testing module values with sigma of 1.0d
		for (int i = 0; i < valuesModule.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;
			pos[3] = 0;

			outRA.setPosition(pos);
//			assertEquals(valuesModule[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing trace values with sigma of 1.0d
		for (int i = 0; i < valuesTrace.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 1;
			pos[3] = 0;

			outRA.setPosition(pos);
//			assertEquals(valuesTrace[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing determinant values with sigma of 2.0d
		for (int i = 0; i < valuesDeterminant.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 2;
			pos[3] = 1;

			outRA.setPosition(pos);
//			assertEquals(valuesDeterminant[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing first eigenvalue values with sigma of 4.0d
		for (int i = 0; i < valuesFirstEigenvalue.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 3;
			pos[3] = 2;

			outRA.setPosition(pos);
//			assertEquals(valuesFirstEigenvalue[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing second eigenvalue values with sigma of 8.0d
		for (int i = 0; i < valuesSecondEigenvalue.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 4;
			pos[3] = 3;

			outRA.setPosition(pos);
//			assertEquals(valuesSecondEigenvalue[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing orientatinon values with sigma of 1.0d
		for (int i = 0; i < valuesOrientation.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 5;
			pos[3] = 4;

			outRA.setPosition(pos);
//			assertEquals(valuesOrientation[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing gamma-normalized square eigenvalue difference values with
		// sigma of 1.0d
		for (int i = 0; i < valuesgnsed.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 6;
			pos[3] = 5;

			outRA.setPosition(pos);
//			assertEquals(valuesgnsed[i], outRA.get().getRealFloat(), 0.0000f);
		}

		// testing square gamma-normalized eigenvalue difference values with
		// sigma of 1.0d
		for (int i = 0; i < valuessgned.length; i++) {
			int[] pos = new int[4];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 7;
			pos[3] = 6;

			outRA.setPosition(pos);
//			assertEquals(valuessgned[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}
}