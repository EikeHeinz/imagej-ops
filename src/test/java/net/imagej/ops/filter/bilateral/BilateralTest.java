package net.imagej.ops.filter.bilateral;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.Test;

public class BilateralTest extends AbstractOpTest {

	@Test
	public void test() {
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
		RandomAccess<FloatType> imgRA = img.randomAccess();
		imgRA.setPosition(new int[] {0,0});
		imgRA.get().setReal(2);

		RandomAccessibleInterval<FloatType> out = ops.filter().bilateral(img, 5.0d, 5.0d, 2);
		
//		RealType outType = ops.create().nativeType();
		
//		ops.filter().bilateralRegion(outType, img, 5.0d, 5.0d);
		
		RandomAccessibleInterval<FloatType> test = ops.filter().optimizedBilateral(img, 5.0d, 5.0d, 2);
		RandomAccess<FloatType> outRA = out.randomAccess();
		Cursor<FloatType> testCursor = Views.iterable(test).cursor();
		String valuesString = "";
		int j = 0;
		while(testCursor.hasNext()) {
			testCursor.fwd();
			outRA.setPosition(testCursor);
			valuesString += outRA.get() +"/" + testCursor.get() +" | ";
			j++;
			if(j == 10) {
//				System.out.println(valuesString);
				valuesString = "";
				j = 0;
			}

//			assertEquals(outRA.get(), testCursor.get());
		}

		float[] values = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
		for (int i = 0; i < values.length; i++) {
			long[] pos = new long[] { i, i };
			outRA.setPosition(pos);
//			assertEquals(values[i], outRA.get().getRealFloat(), 0.0000f);
		}
	}
}
