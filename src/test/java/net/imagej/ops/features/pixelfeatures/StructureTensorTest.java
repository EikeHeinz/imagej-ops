package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

import org.junit.Test;

public class StructureTensorTest extends AbstractOpTest {

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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().structureTensor(img, 1.0d, 1.0d);
		RandomAccess<FloatType> outRA = out.randomAccess();
		
		float[] largeEigenvalues = { 6.274681E-4f, 0.10563567f, 2.1829276f, 4.98041f, 0.755025f, 0.7613907f, 5.074237f, 2.19776f, 0.095376365f, 4.67303E-4f };
		float[] smallEigenvalues = { 0.14059982f, 0.8813345f, 3.4334455f, 4.228283f, 3.52035f, 3.4952223f, 3.974162f, 2.7890806f, 0.49972314f, 0.05507776f };
		for (int i = 0; i < largeEigenvalues.length; i++) {
			int[] pos = new int[3];
			pos[0] = i;
			pos[1] = i;
			pos[2] = 0;

			outRA.setPosition(pos);
			assertEquals(largeEigenvalues[i], outRA.get().getRealFloat(), 0.0000f);
			pos[2]=1;
			outRA.setPosition(pos);
//			assertEquals(smallEigenvalues[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}
}
