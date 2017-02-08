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

@Plugin(type=Ops.Filter.NaivePartialDerivative.class)
public class DerivativeNaive<T extends RealType<T>> extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements Ops.Filter.NaivePartialDerivative{
	
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
//		long[] dims = new long[in().numDimensions()];
//		if (dimension == 0) {
//			// FIXME hack
//			kernelBConvolveOp = RAIs.computer(ops(), Ops.Filter.Convolve.class, in(), new Object[] { kernelB });
//		} else {
//			// rotate kernel B to dimension
//			for (int j = 0; j < in().numDimensions(); j++) {
//				if (j == dimension) {
//					dims[j] = 3;
//				} else {
//					dims[j] = 1;
//				}
//			}
//
//			Img<DoubleType> kernelInterval = ops().create().img(dims);
//
//			RandomAccessibleInterval<T> rotatedKernelB = kernelB;
//			for (int i = 0; i < dimension; i++) {
//				rotatedKernelB = Views.rotate(rotatedKernelB, i, i + 1);
//			}
//
//			rotatedKernelB = Views.interval(rotatedKernelB, kernelInterval);
//			kernelBConvolveOp = RAIs.computer(ops(), Ops.Filter.Convolve.class, in(), new Object[] { rotatedKernelB });
//		}
//
//		dims = null;
//
//		// rotate kernel A to all other dimensions
//		kernelAConvolveOps = new UnaryComputerOp[in().numDimensions()];
//		if (dimension != 0) {
//			kernelAConvolveOps[0] = RAIs.computer(ops(), Ops.Filter.Convolve.class, in(), new Object[] { kernelA });
//		}
//		RandomAccessibleInterval<T> rotatedKernelA = kernelA;
//		for (int i = 1; i < in().numDimensions(); i++) {
//			if (i != dimension) {
//				dims = new long[in().numDimensions()];
//				for (int j = 0; j < in().numDimensions(); j++) {
//					if (i == j) {
//						dims[j] = 3;
//					} else {
//						dims[j] = 1;
//					}
//				}
//				Img<DoubleType> kernelInterval = ops().create().img(dims);
//				for (int j = 0; j < i; j++) {
//					rotatedKernelA = Views.rotate(rotatedKernelA, j, j + 1);
//				}
//
//				kernelAConvolveOps[i] = RAIs.computer(ops(), Ops.Filter.Convolve.class, in(),
//						new Object[] { Views.interval(rotatedKernelA, kernelInterval) });
//				rotatedKernelA = kernelA;
//			}
//		}

		BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> addOp = RAIs.binaryComputer(ops(), Ops.Math.Add.class, in(), in());
		
		
		RandomAccessibleInterval<T> in = Views.interval(Views.extendMirrorDouble(input), input);
		for (int i = input.numDimensions() - 1; i >= 0; i--) {
			RandomAccessibleInterval<T> derivative = createRAI.calculate(input);
			if( i != 0 )
			{
				IntervalView<T> filter = dimension == i ? 
					Views.rotate( kernelB, 0, i ) : Views.rotate( kernelA, 0, i );
	
				ops().run( ConvolveNaiveC.class, derivative, Views.extendMirrorSingle( in ), filter );
			}
			else
			{
				if( dimension == i )
					ops().run( ConvolveNaiveC.class, derivative, Views.extendMirrorSingle( in ), kernelB );
				else
					ops().run( ConvolveNaiveC.class, derivative, Views.extendMirrorSingle( in ), kernelA );
}
			in = derivative;
//			in = Views.interval(Views.extend(derivative, fac), derivative);
		}
//		RandomAccessibleInterval<T> output = createRAI.calculate(input);
		addOp.compute(output, in, output);
		
	}
	
	@Override
	public RandomAccessibleInterval<T> createOutput(RandomAccessibleInterval<T> input) {
		return createRAI.calculate(input);
	}
}
