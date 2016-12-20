package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.fail;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.junit.Test;

public class MembraneProjectionsTest extends AbstractOpTest {

	@Test
	public <T extends RealType<T>> void test() {

		Img<FloatType> img = generateFloatArrayTestImg(false, new long[] { 50, 50 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 27 && counterX < 33 || counterY > 27 && counterY < 33) {
				cursorImg.next().setOne();
			} else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 50 == 9) {
				counterY++;
			}
			if (counterX == 50) {
				counterX = 0;
			}
			if (counterY == 50) {
				counterY = 0;
			}
		}
		
		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.pixelfeature().membraneProjections(img);
		Cursor<RealComposite<FloatType>> outCursor = Views.iterable(out).cursor();
		while(outCursor.hasNext()) {
			RealComposite<FloatType> composite = outCursor.next();
			String values = "";
			for(FloatType value: composite) {
				values += value +"|";
			}
//			System.out.println(values);
		}
				
		fail("Not yet implemented");
	}

}
