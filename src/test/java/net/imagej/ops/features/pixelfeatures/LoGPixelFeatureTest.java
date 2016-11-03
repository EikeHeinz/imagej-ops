package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.fail;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.junit.Test;

public class LoGPixelFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 500, 500 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 240 && counterX < 260 || counterY > 120000 && counterY < 130000) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			counterY++;
			if (counterX == 500) {
				counterX = 0;
			}
		}
		// CompositeIntervalView<FloatType, RealComposite<FloatType>> out =
		// ops.pixelfeature().loG(img, 1.4d, 2.8d);
		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature().loG(img, 1.0d, 4.0d);
		System.out.println(out.numDimensions());
		// ImageJFunctions.show(out);
		System.out.println("breakpoint");
		fail("no proper test.");
	}

}
