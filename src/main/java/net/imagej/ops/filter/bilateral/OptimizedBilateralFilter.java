package net.imagej.ops.filter.bilateral;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Map;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Filter.OptimizedBilateral.class)
public class OptimizedBilateralFilter<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements Ops.Filter.OptimizedBilateral {

	@Parameter
	private double spatial;

	@Parameter
	private double domain;

	@Parameter
	private int radius;

	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, IterableInterval> mapOp;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	@Override
	public void initialize() {

		@SuppressWarnings("rawtypes")
		UnaryComputerOp<RandomAccessibleInterval<T>, RealType> filterOp = Computers.unary(ops(),
				Ops.Filter.BilateralRegionFilter.class, RealType.class, in(), spatial, domain);
		Shape shape = new RectangleShape(radius, false);
		mapOp = Computers.unary(ops(), Map.class, IterableInterval.class, in(), shape, filterOp);

		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		IntervalView<T> extendedInput = Views.interval(Views.extendMirrorDouble(input), input);
		RandomAccessibleInterval<T> output = createOp.calculate(input);
		mapOp.compute(extendedInput, (IterableInterval<T>) output);
		return output;
	}

}
