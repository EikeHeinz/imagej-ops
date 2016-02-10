
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.AbstractNamespaceTest;

import org.junit.Test;

/**
 * Tests {@link PixelfeatureNamespace}.
 * 
 * @author Eike Heinz
 */

public class PixelFeaturesNamespaceTest extends AbstractNamespaceTest {

	/**
	 * Tests that the ops of the {@code pixelfeatures} namespace have
	 * corresponding type-safe Java method signatures declared in the
	 * {@link PixelfeatureNamespace} class.
	 */
	@Test
	public void test() {
		assertComplete("pixelfeatures", PixelfeatureNamespace.class);
	}

}
