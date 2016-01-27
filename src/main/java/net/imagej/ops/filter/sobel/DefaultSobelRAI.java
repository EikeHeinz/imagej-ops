package net.imagej.ops.filter.sobel;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Math.Sqr;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Filter.Sobel.class, name = Ops.Filter.Sobel.NAME)
public class DefaultSobelRAI<T extends RealType<T>>
		extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createOutputOp;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> squareMapOp;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> sqrtMapOp;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> addOp;

	@Override
	public void initialize() {
		createOutputOp = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class, in());

		Sqr squareOp = ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class);
		squareMapOp = Computers.unary(ops(), Ops.Map.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class, squareOp);
		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMapOp = Computers.unary(ops(), Ops.Map.class, RandomAccessibleInterval.class,
				RandomAccessibleInterval.class, sqrtOp);
		addOp = Computers.binary(ops(), Ops.Math.Add.class, RandomAccessibleInterval.class, in(), in());
	}

	@Override
	public void compute1(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {

		for (int i = 0; i < input.numDimensions(); i++) {

			RandomAccessibleInterval<T> derivative = createOutputOp.compute1(input);
			ops().filter().directionalDerivative(derivative, input, i);

			squareMapOp.compute1(derivative, derivative);
			addOp.compute2(output, derivative, output);
		}

		sqrtMapOp.compute1(output, output);
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
		return compute1(in());
	}

	@Override
	public RandomAccessibleInterval<T> createOutput(RandomAccessibleInterval<T> input) {
		return createOutputOp.compute1(input);
	}

	@Override
	public RandomAccessibleInterval<T> createOutput() {
		return createOutputOp.compute1(in());
	}

}
