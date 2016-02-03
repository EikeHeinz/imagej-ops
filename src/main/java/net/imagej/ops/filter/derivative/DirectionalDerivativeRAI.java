package net.imagej.ops.filter.derivative;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.DirectionalDerivative;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Filter.DirectionalDerivative.class, name = Ops.Filter.DirectionalDerivative.NAME)
public class DirectionalDerivativeRAI<T extends RealType<T>>
		extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements DirectionalDerivative {

	@Parameter
	private int dimension;

	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> addOp;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> yConvolver;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> copyRAI;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> xConvolver;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createRAIFromRAI;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> yConvolverRotated;
	@SuppressWarnings("rawtypes")
	private List<UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval>> xConvolverRotatedList;

	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernel = ops().create().kernelSobel();
		IntervalView<T> kernelX = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);
		IntervalView<T> kernelY = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 1);
		addOp = Computers.binary(ops(), Ops.Math.Add.class, RandomAccessibleInterval.class, in(), in());
		yConvolver = Computers.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(), kernelY);
		xConvolver = Computers.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(), kernelX);
		copyRAI = Computers.unary(ops(), Ops.Copy.RAI.class, RandomAccessibleInterval.class, in());
		createRAIFromRAI = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class, in());
		// this list contains NULL at index dimension
		xConvolverRotatedList = new ArrayList<>();
		for (int i = 0; i < in().numDimensions(); i++) {
			@SuppressWarnings("rawtypes")
			UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> temp = null;
			if (i == dimension) {
				// rotate kernelY
				IntervalView<T> filter = Views.rotate(kernelY, 0, 1);
				yConvolverRotated = Computers.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class,
						in(), filter);
			} else {
				// rotate kernelX
				IntervalView<T> filter = Views.rotate(kernelX, 0, 1);
				temp = Computers.unary(ops(), Ops.Filter.Convolve.class, RandomAccessibleInterval.class, in(), filter);
			}
			xConvolverRotatedList.add(temp);
		}

		System.out.println("breakpoint");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void compute1(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {

		RandomAccessibleInterval<T> aux = createRAIFromRAI.compute1(input);
		copyRAI.compute1(input, aux);

		for (int j = input.numDimensions() - 1; j >= 0; j--) {
			RandomAccessibleInterval<T> derivative = createRAIFromRAI.compute1(input);
			if (j != 0) {
				if (dimension == j) {
					yConvolverRotated.compute1(Views.interval(Views.extendMirrorDouble(aux), input), derivative);
				} else {
					UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> xConvolverRotated = xConvolverRotatedList
							.get(j);
					xConvolverRotated.compute1(Views.interval(Views.extendMirrorDouble(aux), input), derivative);
				}
			} else {
				if (dimension == j) {
					yConvolver.compute1(Views.interval(Views.extendMirrorDouble(aux), input), derivative);
				} else {
					xConvolver.compute1(Views.interval(Views.extendMirrorDouble(aux), input), derivative);
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
