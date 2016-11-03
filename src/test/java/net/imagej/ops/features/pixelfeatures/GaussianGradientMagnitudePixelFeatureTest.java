package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

import org.junit.Test;

public class GaussianGradientMagnitudePixelFeatureTest extends AbstractOpTest {

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

		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature()
				.gaussianGradientMagnitude(img, 1.0d, 4.0d);

		CompositeView<FloatType, RealComposite<FloatType>>.CompositeRandomAccess outRA = out.randomAccess();
		float[] values = { 0.025049314f, 0.16272473f, 0.011789966f, 0.3250164f, 0.5826428f, 0.037357558f, 1.4774734f, 0.88723177f, 0.05076311f, 2.2316833f, 0.7549961f, 0.04029004f };
		int j = 0;
		for(int i = 0; i < 4; i++) {
			int[] position = new int[2];
			position[0] = i;
			position[1] = i;
			outRA.setPosition(position);
			assertEquals(values[j], outRA.get().get(0).getRealFloat(), 0.000000001f);
			assertEquals(values[j+1], outRA.get().get(1).getRealFloat(), 0.000000001f);
			assertEquals(values[j+2], outRA.get().get(2).getRealFloat(), 0.000000001f);
			j+=3;
		}
	}
}
