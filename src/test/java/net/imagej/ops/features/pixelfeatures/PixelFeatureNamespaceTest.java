package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.AbstractNamespaceTest;

import org.junit.Test;

public class PixelFeatureNamespaceTest extends AbstractNamespaceTest {

	@Test
	public void testCompleteness() {
		assertComplete("pixelfeatures", PixelFeatureNamespace.class);
	}

}
