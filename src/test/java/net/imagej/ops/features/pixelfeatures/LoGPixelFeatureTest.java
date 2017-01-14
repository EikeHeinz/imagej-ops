package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.junit.Test;

public class LoGPixelFeatureTest extends AbstractOpTest {

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
		
		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().loG(img, 1.0d, 4.0d);
		RandomAccess<FloatType> outRA = out.randomAccess();

		RandomAccessibleInterval<FloatType> kernel = ops.create().kernelLog(1.0d, img.numDimensions(),
				Util.getTypeFromInterval(img));
		RandomAccessibleInterval<FloatType> manualFirstSigma = ops.filter()
				.convolve(Views.interval(Views.extendMirrorDouble(img), img), kernel);
		Cursor<FloatType> firstSigmaCursor = Views.iterable(manualFirstSigma).cursor();

		kernel = ops.create().kernelLog(2.0d, img.numDimensions(), Util.getTypeFromInterval(img));
		RandomAccessibleInterval<FloatType> manualSecondSigma = ops.filter()
				.convolve(Views.interval(Views.extendMirrorDouble(img), img), kernel);
		RandomAccess<FloatType> secondSigmaRA = manualSecondSigma.randomAccess();

		kernel = ops.create().kernelLog(4.0d, img.numDimensions(), Util.getTypeFromInterval(img));
		RandomAccessibleInterval<FloatType> manualThirdSigma = ops.filter()
				.convolve(Views.interval(Views.extendMirrorDouble(img), img), kernel);
		RandomAccess<FloatType> thirdSigmaRA = manualThirdSigma.randomAccess();
		
		while(firstSigmaCursor.hasNext()) {
			firstSigmaCursor.next();
			long[] pos = new long[2];
			firstSigmaCursor.localize(pos);
			secondSigmaRA.setPosition(firstSigmaCursor);
			thirdSigmaRA.setPosition(firstSigmaCursor);
			long[] stackPos = new long[3];
			stackPos[0]=pos[0];
			stackPos[1]=pos[1];
			stackPos[2]=0;
			outRA.setPosition(stackPos);
			assertEquals(firstSigmaCursor.get(), outRA.get());
			stackPos[2]=1;
			outRA.setPosition(stackPos);
			assertEquals(secondSigmaRA.get(), outRA.get());
			stackPos[2]=2;
			outRA.setPosition(stackPos);
			assertEquals(thirdSigmaRA.get(), outRA.get());
			
		}
	}
}
