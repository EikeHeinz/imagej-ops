package net.imagej.ops.create.kernelSobel;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.create.AbstractCreateKernel;
import net.imglib2.Cursor;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.real.DoubleType;

@Plugin(type = Ops.Create.KernelSobel.class, name = Ops.Create.KernelSobel.NAME)
public class CreateKernelSobel<T extends ComplexType<T> & NativeType<T>> extends AbstractCreateKernel<T> {

	// TODO fix inputs maybe create new abstract kernel class for sobel
	// TODO fix type

	@Override
	protected void createKernel() {

		// case: 2d
		if (numDimensions == 2) {

			int[] valuesX = new int[] { 1, 2, 1, 0, 0, 0, -1, -2, -1 };
			// TODO remove y values and rotate x to obtain y-direction
			int[] valuesY = new int[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 };

			createOutputImg(new long[] { 3, 3, 1, 2 }, getFac(), getOutType(), new ArrayImgFactory<DoubleType>(),
					new DoubleType());

			final Cursor<T> cursor = getOutput().cursor();
			int i = 0;
			while (cursor.hasNext()) {
				cursor.fwd();
				if (i < 9) {
					cursor.get().setReal(valuesX[i]);
				} else {
					int pos = i - 9;
					cursor.get().setReal(valuesY[pos]);
				}
				i++;
			}
			// RandomAccessibleInterval<T> test = Views.rotate(getOutput(), 0,
			// 1);
			// ImageJFunctions.show(test);
			// System.out.println("breakpoint");

		} else if (numDimensions == 3) {
			// implement
			createOutputImg(new long[] { 3, 3, 3, 1, 3 }, getFac(), getOutType(), new ArrayImgFactory<DoubleType>(),
					new DoubleType());
		}

	}

}
