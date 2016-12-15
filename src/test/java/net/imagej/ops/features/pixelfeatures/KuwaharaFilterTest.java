
package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.*;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class KuwaharaFilterTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 20,
			20 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 7 && counterX < 13 || counterY > 7 && counterY < 13) {
				cursorImg.next().setOne();
			}
			else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 20 == 9) {
				counterY++;
			}
			if (counterX == 20) {
				counterX = 0;
			}
			if (counterY == 20) {
				counterY = 0;
			}
		}

		RandomAccessibleInterval<FloatType> output = ops.pixelfeature()
			.kuwaharaFilter(img, 5, 30);
		System.out.println(output.numDimensions() + "|" + output.dimension(0) +
			"|" + output.dimension(1));
		Cursor<FloatType> outCursor = Views.iterable(output).cursor();
		while (outCursor.hasNext()) {
			System.out.println(outCursor.next().get());
		}

		fail("Not yet implemented");
	}

}
