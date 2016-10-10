package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.fail;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

public class DoGPixelFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
//		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[] { 500, 500 });
//
//		Cursor<FloatType> cursorImg = img1.cursor();
//		int counterX = 0;
//		int counterY = 0;
//		while (cursorImg.hasNext()) {
//			if(counterX > 240 && counterX < 260 || counterY > 120000 && counterY < 130000) {
//				cursorImg.next().setOne();
//			} else {
//			cursorImg.next().setZero();
//			}
//			counterX++;
//			counterY++;
//			if(counterX == 500) {
//				counterX =0;
//			}
//		}
		
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 20, 20 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 8 && counterX < 12 || counterY > 8 && counterY < 12) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 20 == 0) {
				counterY++;
			}
			if (counterX == 20) {
				counterX = 0;
			}
			if (counterY == 20) {
				counterY = 0;
			}
		}
		

		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature().doG(img, 1.0d, 16.0d);
		Cursor<RealComposite<FloatType>> outCursor = Views.iterable(out).cursor();
		while(outCursor.hasNext()) {
			RealComposite<FloatType> composite = outCursor.next();
			String result = "";
			for(FloatType values : composite) {
				result += values + "|";
			}
			System.out.println(result);
		}

		System.out.println("breakpoint");
		fail("no proper test.");
	}

}
