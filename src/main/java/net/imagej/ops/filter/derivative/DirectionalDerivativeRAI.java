package net.imagej.ops.filter.derivative;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Filter.DirectionalDerivative.class, name = Ops.Filter.DirectionalDerivative.NAME)
public class DirectionalDerivativeRAI<T extends RealType<T>>
		extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {

	@Parameter
	private int dimension;
	private IntervalView<T> kernelX;
	private IntervalView<T> kernelY;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> addOp;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> yConvolver;
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> copyOp;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> xConvolver;

	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernel = ops().create().kernelSobel(2);
		kernelX = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);
		kernelY = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 1);
		addOp = Computers.binary(ops(), Ops.Math.Add.class, RandomAccessibleInterval.class, in(), in());
		yConvolver = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(),
				Views.interval(kernelY, kernelY));
		xConvolver = Functions.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(),
				Views.interval(kernelX, kernelX));
		copyOp = Computers.unary(ops(), Ops.Copy.RAI.class, RandomAccessibleInterval.class, in());
		System.out.println("breakpoint");
	}

	@Override
	public void compute1(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {

		RandomAccessibleInterval<T> aux = ops().create().img(input);
		ops().copy().rai(aux, input);

		// calculate derivative on that direction with 1-d filter
		for (int j = input.numDimensions() - 1; j >= 0; j--) {
			RandomAccessibleInterval<T> derivative = ops().create().img(aux);
			// TODO extend aux
			if (j != 0) {
				if (dimension == j) {
					IntervalView<T> filter = Views.rotate(kernelY, 0, j);
					ops().filter().convolve(derivative, Views.extendMirrorSingle(aux), filter);
				} else {
					IntervalView<T> filter = Views.rotate(kernelX, 0, j);
					ops().filter().convolve(derivative, Views.extendMirrorSingle(aux), filter);
				}
			} else {
				if (dimension == j) {
					RandomAccessibleInterval<T> temp = yConvolver.compute1(aux);
					copyOp.compute1(temp, derivative);
				} else {
					RandomAccessibleInterval<T> temp = xConvolver.compute1(aux);
					copyOp.compute1(temp, derivative);
				}
			}
			aux = derivative;

		}
		addOp.compute2(output, aux, output);
	}

	@Override
	public void compute0(RandomAccessibleInterval<T> output) {
		compute1(in(), output);

	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = createOutput(input);
		compute1(input, output);
		return output;
	}

	@Override
	public RandomAccessibleInterval<T> compute0() {
		RandomAccessibleInterval<T> output = createOutput();
		compute1(in(), output);
		return output;
	}

	@Override
	public RandomAccessibleInterval<T> createOutput(RandomAccessibleInterval<T> input) {
		return ops().create().img(input);
	}

	@Override
	public RandomAccessibleInterval<T> createOutput() {
		return ops().create().img(in());
	}

}
