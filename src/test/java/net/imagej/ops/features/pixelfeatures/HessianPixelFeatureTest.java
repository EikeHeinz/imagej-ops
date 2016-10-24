package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import static org.junit.Assert.*;

import org.junit.Test;

public class HessianPixelFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 10, 10});

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 3 && counterX < 6 || counterY > 3 && counterY < 6) {
				cursorImg.next().setOne();
			}
			else {
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

//		ImageJFunctions.show(img, "img");
		
//		CompositeIntervalView<FloatType, RealComposite<FloatType>> test = ops.filter().hessian(img);
//		
//		Cursor<RealComposite<FloatType>> cursor = Views.iterable(test).cursor();
//		int i = 0;
//		while(cursor.hasNext()) {
//			RealComposite<FloatType> composite = cursor.next();
//			String values = "";
//			for (FloatType value : composite) {
//				values += value + "|";
//			}
//			System.out.println(values);
//			i++;
//		}
//		
//		System.out.println("#composites: "+i);
		

		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature().hessian(img);
		System.out.println(out.numDimensions());
//		ImageJFunctions.show(out);
		System.out.println("breakpoint");
		fail("no proper test.");
	}

}