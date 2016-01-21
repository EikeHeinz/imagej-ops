package net.imagej.ops.create;

import org.scijava.plugin.Parameter;

import net.imagej.ops.Contingent;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

public abstract class AbstractCreateSobelKernel<T extends ComplexType<T> & NativeType<T>>
		extends AbstractCreateKernelImg<T, DoubleType, ArrayImgFactory<DoubleType>> implements Contingent {
	
	@Parameter
	protected int numDimensions;
	
	@Override
	public void run() {

		createKernel();
	}
	
	@Override
	public boolean conforms() {

		// if outType is not null make sure it is a supported type
		if (getOutType() != null) {
			final Object tmp = getOutType();
			if ((tmp instanceof FloatType) || (tmp instanceof DoubleType) ||
				(tmp instanceof ComplexFloatType) || (tmp instanceof ComplexDoubleType)) return true;
			return false;
		}

		return true;
	}
	
	protected abstract void createKernel();

}
