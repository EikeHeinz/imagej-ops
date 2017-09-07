package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.Ops;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.Test;

public class DoGPixelFeatureTest extends AbstractOpTest {

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

		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().doG(img, 1.0d, 4.0d);

		UnaryFunctionOp<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> function1 = RAIs
				.function(ops, Ops.Filter.DoG.class, img, 1.0d, 2.0d);
		RandomAccessibleInterval<FloatType> firstSigmaCombination = function1
				.calculate(Views.interval(Views.extendMirrorDouble(img), img));

		UnaryFunctionOp<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> function2 = RAIs
				.function(ops, Ops.Filter.DoG.class, img, 1.0d, 4.0d);
		RandomAccessibleInterval<FloatType> secondSigmaCombination = function2
				.calculate(Views.interval(Views.extendMirrorDouble(img), img));
		RandomAccess<FloatType> secondSigmaComboRA = secondSigmaCombination.randomAccess();

		UnaryFunctionOp<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> function3 = RAIs
				.function(ops, Ops.Filter.DoG.class, img, 2.0d, 4.0d);
		RandomAccessibleInterval<FloatType> thirdSigmaCombination = function3
				.calculate(Views.interval(Views.extendMirrorDouble(img), img));
		RandomAccess<FloatType> thirdSigmaComboRA = thirdSigmaCombination.randomAccess();

		RandomAccess<FloatType> outRA = out.randomAccess();
		Cursor<FloatType> firstSigmaComboCursor = Views.iterable(firstSigmaCombination).cursor();
		while(firstSigmaComboCursor.hasNext()) {
			firstSigmaComboCursor.next();
			long[] pos = new long[2];
			firstSigmaComboCursor.localize(pos);
			secondSigmaComboRA.setPosition(pos);
			thirdSigmaComboRA.setPosition(pos);
			long[] stackPos = new long[3];
			stackPos[0]=pos[0];
			stackPos[1]=pos[1];
			stackPos[2]=0;
			outRA.setPosition(stackPos);
			assertEquals(firstSigmaComboCursor.get(), outRA.get());
			stackPos[2]=1;
			outRA.setPosition(stackPos);
			assertEquals(secondSigmaComboRA.get(), outRA.get());
			stackPos[2]=2;
			outRA.setPosition(stackPos);
			assertEquals(thirdSigmaComboRA.get(), outRA.get());
		}
	}
}