package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.*;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.Test;

public class NeighborsPixelFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
		ArrayImg<FloatType, FloatArray> img = generateFloatArrayTestImg(false, new long[] { 10, 10 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 4 && counterX < 6 || counterY > 4 && counterY < 6) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 10 == 0) {
				counterY++;
			}
			if (counterX == 10) {
				counterX = 0;
			}
			if (counterY == 10) {
				counterY = 0;
			}
		}
		
//		String values = "";
//		int counter = 0;
//		Cursor<FloatType> cursor = Views.iterable(img).cursor();
//		System.out.println("original-------------");
//		while(cursor.hasNext()) {
//			values += cursor.next().getRealDouble()+"|";
//			counter++;
//			if(counter == 10) {
//				System.out.println(values);
//				counter = 0;
//				values = "";
//			}
//			
//		}
//		
//		RandomAccessibleInterval<FloatType> out = ops.pixelfeature().neighbors(img, 1, 1);
//		
//		fail("Not yet implemented");
	}

}
