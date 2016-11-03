package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.junit.Test;

public class GaussPixelFeatureTest extends AbstractOpTest {

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

		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature().gaussian(img, 1.0d, 8.0d);
		RandomAccessibleInterval<FloatType> manualFirstSigma = ops.filter()
				.gauss(Views.interval(Views.extendMirrorDouble(img), img), 1.0d);
		RandomAccess<FloatType> FirstSigmaRA = manualFirstSigma.randomAccess();
		RandomAccessibleInterval<FloatType> manualSecondSigma = ops.filter()
				.gauss(Views.interval(Views.extendMirrorDouble(img), img), 2.0d);
		RandomAccess<FloatType> SecondSigmaRA = manualSecondSigma.randomAccess();
		RandomAccessibleInterval<FloatType> manualThirdSigma = ops.filter()
				.gauss(Views.interval(Views.extendMirrorDouble(img), img), 4.0d);
		RandomAccess<FloatType> ThirdSigmaRA = manualThirdSigma.randomAccess();
		RandomAccessibleInterval<FloatType> manualFourthSigma = ops.filter()
				.gauss(Views.interval(Views.extendMirrorDouble(img), img), 8.0d);
		RandomAccess<FloatType> FourthSigmaRA = manualFourthSigma.randomAccess();
		Cursor<RealComposite<FloatType>> outCursor = Views.iterable(out).cursor();
		while (outCursor.hasNext()) {
			RealComposite<FloatType> composite = outCursor.next();
			long[] position = new long[2];
			outCursor.localize(position);
			FirstSigmaRA.setPosition(position);
			SecondSigmaRA.setPosition(position);
			ThirdSigmaRA.setPosition(position);
			FourthSigmaRA.setPosition(position);
			assertEquals(FirstSigmaRA.get(), composite.get(0));
			assertEquals(SecondSigmaRA.get(), composite.get(1));
			assertEquals(ThirdSigmaRA.get(), composite.get(2));
			assertEquals(FourthSigmaRA.get(), composite.get(3));
		}
	}
}