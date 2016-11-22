package net.imagej.ops.features.haralick;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.features.AbstractFeatureTest;
import net.imagej.ops.image.cooccurrenceMatrix.MatrixOrientation2D;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Before;
import org.junit.Test;

/**
 * Haralick features tested against matlab implementations and all formulas
 * verified.
 * 
 * References:
 * http://murphylab.web.cmu.edu/publications/boland/boland_node26.html
 * http://www.uio.no/studier/emner/matnat/ifi/INF4300/h08/undervisningsmateriale
 * /glcm.pdf
 * 
 * @author Tim-Oliver Buchholz, University of Konstanz
 *
 */
public class HaralickFeatureTest extends AbstractOpTest {

	private final static double EPSILON = 10e-10;

	private Img<UnsignedByteType> img;

	@Before
	public void loadImage() {
		img = openUnsignedByteType(AbstractFeatureTest.class, "haralick_test_img.tif");
	}

	@Test
	public void asm() {
		assertEquals(0.002728531855956, ops.haralick().asm(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void clusterPromenence() {
		assertEquals(1.913756322645621e+07,
				ops.haralick().clusterPromenence(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void clusterShade() {
		assertEquals(7.909331564367658e+03,
				ops.haralick().clusterShade(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void contrast() {
		assertEquals(2.829684210526314e+03, ops.haralick().contrast(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}

	@Test
	public void correlation() {
		assertEquals(-6.957913328178969e-03,
				ops.haralick().correlation(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void differenceEntropy() {
		assertEquals(4.517886362509830,
				ops.haralick().differenceEntropy(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void differenceVariance() {
		assertEquals(2.829684210526315e+03,
				ops.haralick().differenceVariance(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void entropy() {
		assertEquals(5.914634251331289, ops.haralick().entropy(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}

	@Test
	public void ICM1() {
		assertEquals(-1.138457766487823, ops.haralick().icm1(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}

	@Test
	public void ICM2() {
		assertEquals(0.9995136931858095, ops.haralick().icm2(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}

	@Test
	public void IFDM() {
		assertEquals(2.630092221760193e-02, ops.haralick().ifdm(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}

	@Test
	public void maxProbability() {
		assertEquals(0.005263157894737,
				ops.haralick().maxProbability(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void sumAverage() {
		assertEquals(1.244210526315790e+02,
				ops.haralick().sumAverage(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void sumEntropy() {
		assertEquals(5.007751063919794, ops.haralick().sumEntropy(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}

	@Test
	public void sumVariance() {
		assertEquals(1.705010667439121e+04,
				ops.haralick().sumVariance(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void textureHomogeneity() {
		assertEquals(0.061929185106950,
				ops.haralick().textureHomogeneity(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(), EPSILON);
	}

	@Test
	public void variance() {
		// no reference found in matlab, formula verified
		// (http://murphylab.web.cmu.edu/publications/boland/boland_node26.html)
		assertEquals(5176.653047585449, ops.haralick().variance(img, 128, 1, MatrixOrientation2D.HORIZONTAL).get(),
				EPSILON);
	}
}
