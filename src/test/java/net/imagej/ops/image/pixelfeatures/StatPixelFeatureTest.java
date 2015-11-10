package net.imagej.ops.image.pixelfeatures;

import static org.junit.Assert.*;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class StatPixelFeatureTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img1 = generateFloatArrayTestImg(false, new long[]{10,10});
		
		Cursor<FloatType> cursor = img1.cursor();
		cursor.next().set(0);
		while(cursor.hasNext()) {
			cursor.next().set(200);
		}
		
		RandomAccessibleInterval<FloatType> out = ops.image().minPxFeature(img1, 2);
		
	}

}
