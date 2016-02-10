
package net.imagej.ops.filter.hessian;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Hessian;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Filter.Hessian.class, name = Ops.Filter.Hessian.NAME)
public class DefaultHessianRAI<T extends RealType<T>> extends
	AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>[][]>
	implements Hessian
{

	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> copyRAI;
	@SuppressWarnings("rawtypes")
	private List<UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval>> derivativeComputers;

	@Override
	public void initialize() {
		copyRAI = Computers.unary(ops(), Ops.Copy.RAI.class,
			RandomAccessibleInterval.class, in());

		derivativeComputers = new ArrayList<>();
		for (int i = 0; i < in().numDimensions(); i++) {
			derivativeComputers.add(Computers.unary(ops(),
				Ops.Filter.DirectionalDerivative.class, RandomAccessibleInterval.class,
				in(), i));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void compute1(RandomAccessibleInterval<T> input,
		RandomAccessibleInterval<T>[][] output)
	{

		for (int i = 0; i < input.numDimensions(); i++) {
			RandomAccessibleInterval<T> derivative = ops().create().img(input);
			copyRAI.compute1(input, derivative);
			UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> iderivativeComputer =
				derivativeComputers.get(i);
			iderivativeComputer.compute1(input, derivative);

			for (int j = 0; j < input.numDimensions(); j++) {
				output[i][j] = ops().create().img(input);
				UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> jderivativeComputer =
					derivativeComputers.get(j);
				jderivativeComputer.compute1(derivative, output[i][j]);
				output[j][i] = output[i][j];
			}
		}
	}

	@Override
	public void compute0(RandomAccessibleInterval<T>[][] output) {
		compute1(in(), output);
	}

	@Override
	public RandomAccessibleInterval<T>[][] compute1(
		RandomAccessibleInterval<T> input)
	{
		RandomAccessibleInterval<T>[][] output = createOutput(input);
		compute1(input, output);
		return output;
	}

	@Override
	public RandomAccessibleInterval<T>[][] compute0() {
		RandomAccessibleInterval<T>[][] output = createOutput(in());
		compute1(in(), output);
		return output;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T>[][] createOutput(
		RandomAccessibleInterval<T> input)
	{
		return new RandomAccessibleInterval[input.numDimensions()][input
			.numDimensions()];
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T>[][] createOutput() {
		return new RandomAccessibleInterval[in().numDimensions()][in()
			.numDimensions()];
	}

}
