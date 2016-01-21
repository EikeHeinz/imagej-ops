package net.imagej.ops.create.kernelSobel;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.create.AbstractCreateSobelKernel;
import net.imglib2.Cursor;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.real.DoubleType;

@Plugin(type = Ops.Create.KernelSobel.class, name = Ops.Create.KernelSobel.NAME)
public class CreateKernelSobel<T extends ComplexType<T> & NativeType<T>> extends AbstractCreateSobelKernel<T> {


	@Override
	protected void createKernel() {

		long[] dim = new long[numDimensions + 2];

		dim[0] = 3;
		dim[1] = 1;

		for (int k = 2; k < dim.length; k++) {
			dim[k] = 1;
		}

		dim[dim.length - 1] = 2;

		createOutputImg(dim, getFac(), getOutType(), new ArrayImgFactory<DoubleType>(), new DoubleType());
		final Cursor<T> cursor = getOutput().cursor();
		int i = 0;
		float[] values = { 1.0f, 2.0f, 1.0f, -1.0f, 0.0f, 1.0f };
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().setReal(values[i]);
			i++;
		}
	}

}
