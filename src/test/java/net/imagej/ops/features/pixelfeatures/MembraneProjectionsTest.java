package net.imagej.ops.features.pixelfeatures;

import static org.junit.Assert.fail;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

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
		
		ops.pixelfeature().membraneProjections(img);
				
		fail("Not yet implemented");
	}

}
