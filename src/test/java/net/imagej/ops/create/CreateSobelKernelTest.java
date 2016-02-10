package net.imagej.ops.create;

import static org.junit.Assert.*;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;


public class CreateSobelKernelTest extends AbstractOpTest {

	@Test
	public <T extends RealType<T>> void test() {
		Img<T> img = ops.create().kernelSobel();
		ImageJFunctions.show(img);
		System.out.println("breakpoint");
	}

}
