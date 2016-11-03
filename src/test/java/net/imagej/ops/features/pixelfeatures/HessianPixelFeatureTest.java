package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

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

		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature().hessian(img, 1.0d, 8.0d);
		CompositeView<FloatType, RealComposite<FloatType>>.CompositeRandomAccess outRA = out.randomAccess();

		float[] valuesTrace = new float[] { 1.7194724f, 8.870056f, 14.474865f, 1.1520919f, -14.936752f, -15.013784f,
				0.6746214f, 13.593287f, 8.069767f, 1.4733065f };
		float[] valuesDeterminant = new float[] { 1.5465988f, 5.1687274f, 0.6105761f, 1.5592347f, 7.891646f, 7.83646f,
				1.4087182f, 0.7815286f, 5.209663f, 1.4877121f };
		float[] valuesFirstEigenvalue = new float[] { 0.07909532f, 0.12703504f, 0.032215446f, -0.07684502f,
				-0.14143874f, -0.13607089f, -0.06250863f, 0.052130155f, 0.14481187f, 0.086188324f };
		float[] valuesSecondEigenvalue = new float[] { -0.00525427f, -0.009793053f, 6.610853E-4f, 0.009056217f,
				9.237971E-4f, 9.734597E-4f, 0.010357526f, 0.0024873405f, -0.009231832f, -0.005206969f };

		// testing trace values with sigma of 1.0d
		for (int i = 0; i < valuesTrace.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(valuesTrace[i], outRA.get().get(0).getRealFloat(), 0.0000f);
		}
		
		// testing determinant values with sigma of 2.0d
		for (int i = 0; i < valuesDeterminant.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 1;

			outRA.setPosition(pos);
			assertEquals(valuesDeterminant[i], outRA.get().get(1).getRealFloat(), 0.0000f);
		}
		
		// testing first eigenvalue values with sigma of 4.0d
		for (int i = 0; i < valuesFirstEigenvalue.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 2;

			outRA.setPosition(pos);
			assertEquals(valuesFirstEigenvalue[i], outRA.get().get(2).getRealFloat(), 0.0000f);
		}
		
		// testing second eigenvalue values with sigma of 8.0d
		for (int i = 0; i < valuesSecondEigenvalue.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 3;

			outRA.setPosition(pos);
			assertEquals(valuesSecondEigenvalue[i], outRA.get().get(3).getRealFloat(), 0.0000f);
		}
	}
}