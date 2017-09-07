package net.imagej.ops.filter.derivative;

import net.imagej.ops.Ops;
import net.imagej.ops.filter.convolve.ConvolveNaiveC;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Filter.NaivePartialDerivative.class)
public class DerivativeNaive<T extends RealType<T>>
		extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements Ops.Filter.NaivePartialDerivative {

	@Parameter
	private int dimension;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	@Override
	public void initialize() {
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
	}

	@Override
	public void compute(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {
		RandomAccessibleInterval<T> kernel = ops().create().kernelSobelSeparated(Util.getTypeFromInterval(in()));

		RandomAccessibleInterval<T> kernelA = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);

		RandomAccessibleInterval<T> kernelB = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 1);
		if (in().numDimensions() > 2) {
			RandomAccessible<T> expandedKernelA = Views.addDimension(kernelA);
			RandomAccessible<T> expandedKernelB = Views.addDimension(kernelB);
			for (int i = 0; i < in().numDimensions() - 3; i++) {
				expandedKernelA = Views.addDimension(expandedKernelA);
				expandedKernelB = Views.addDimension(expandedKernelB);
			}
			long[] dims = new long[in().numDimensions()];
			for (int j = 0; j < in().numDimensions(); j++) {
				dims[j] = 1;
			}
			dims[0] = 3;
			Interval kernelInterval = new FinalInterval(dims);
			kernelA = Views.interval(expandedKernelA, kernelInterval);
			kernelB = Views.interval(expandedKernelB, kernelInterval);
		}

		BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> addOp = RAIs
				.binaryComputer(ops(), Ops.Math.Add.class, in(), in());

		RandomAccessibleInterval<T> in = Views.interval(Views.extendMirrorDouble(input), input);
		for (int i = input.numDimensions() - 1; i >= 0; i--) {
			RandomAccessibleInterval<T> derivative = createRAI.calculate(input);
			if (i != 0) {
				IntervalView<T> filter = dimension == i ? Views.rotate(kernelB, 0, i) : Views.rotate(kernelA, 0, i);

				ops().run(ConvolveNaiveC.class, derivative, Views.extendMirrorSingle(in), filter);
			} else {
				if (dimension == i)
					ops().run(ConvolveNaiveC.class, derivative, Views.extendMirrorSingle(in), kernelB);
				else
					ops().run(ConvolveNaiveC.class, derivative, Views.extendMirrorSingle(in), kernelA);
			}
			in = derivative;
		}

		addOp.compute(output, in, output);

	}

	@Override
	public RandomAccessibleInterval<T> createOutput(RandomAccessibleInterval<T> input) {
		return createRAI.calculate(input);
	}
}
