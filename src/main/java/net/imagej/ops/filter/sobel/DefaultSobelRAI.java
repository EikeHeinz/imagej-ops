package net.imagej.ops.filter.sobel;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.special.AbstractUnaryHybridOp;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.Functions;
import net.imagej.ops.special.UnaryComputerOp;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Ops.Filter.Sobel.class, name = Ops.Filter.Sobel.NAME)
public class DefaultSobelRAI<T extends RealType<T>>
		extends AbstractUnaryHybridOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> convolverX;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createOutputOp;
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> convolverY;

	// TODO how to return convolved image? combine into one? create parameter
	// for kernel selection and only return one derivative?
	// TODO fix sobel kernel creation op

	@Override
	public void initialize() {
		double[] dims = new double[in().numDimensions()];
		for (int i = 0; i < in().numDimensions(); i++) {
			dims[i] = 3;
		}
		createOutputOp = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class, in());
		RandomAccessibleInterval<T> kernel = (RandomAccessibleInterval<T>) ops().create().kernelSobel(dims);
		RandomAccessibleInterval<T> kernelX = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);
		RandomAccessibleInterval<T> kernelY = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);
		convolverX = Computers.unary(ops(), Convolve.class, RandomAccessibleInterval.class, in(), kernelX);
		convolverY = Computers.unary(ops(), Convolve.class, RandomAccessibleInterval.class, in(), kernelY);

	}

	@Override
	public void compute1(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {
		// TODO Auto-generated method stub
		convolverX.compute1(input, output);
		convolverY.compute1(input, output);
	}

	@Override
	public void compute0(RandomAccessibleInterval<T> output) {
		// TODO Auto-generated method stub
		convolverX.compute1(in(), output);

	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		// TODO Auto-generated method stub
		RandomAccessibleInterval<T> output = createOutputOp.compute1(input);
		convolverX.compute1(input, output);
		return output;
	}

	@Override
	public RandomAccessibleInterval<T> compute0() {
		// TODO Auto-generated method stub
		return compute1(in());
	}

	@Override
	public RandomAccessibleInterval<T> createOutput(RandomAccessibleInterval<T> input) {
		return createOutputOp.compute1(input);
	}

	@Override
	public RandomAccessibleInterval<T> createOutput() {
		// TODO Auto-generated method stub
		return createOutputOp.compute1(in());
	}

}
