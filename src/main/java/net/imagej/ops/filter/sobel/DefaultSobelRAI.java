package net.imagej.ops.filter.sobel;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Convolve;
import net.imagej.ops.Ops.Math.Sqr;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Filter.Sobel.class, name = Ops.Filter.Sobel.NAME)
public class DefaultSobelRAI<T extends RealType<T>>
		extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createOutputOp;
	private IntervalView<T> kernelX;
	private IntervalView<T> kernelY;
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> squareMapOp;
	private UnaryComputerOp<RandomAccessibleInterval, RandomAccessibleInterval> sqrtMapOp;


	// TODO fix sobel kernel creation in init method

	@Override
	public void initialize() {
		createOutputOp = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class, in());
		RandomAccessibleInterval<T> kernel = ops().create().kernelSobel(2);
		kernelX = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);
		kernelY = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 1);
		
		Sqr squareOp = ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class);
		squareMapOp = Computers.unary(ops(),
				Ops.Map.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class, squareOp);
		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMapOp = Computers.unary(ops(),
				Ops.Map.class, RandomAccessibleInterval.class, RandomAccessibleInterval.class, sqrtOp);

	}

	@Override
	public void compute1(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {

		for (int i = 0; i < input.numDimensions(); i++) {

			int dimension = i;

			RandomAccessibleInterval<T> aux = ops().create().img(input);
			ops().copy().rai(aux, input);

			// calculate derivative on that direction with 1-d filter
			for (int j = input.numDimensions() - 1; j >= 0; j--) {
				RandomAccessibleInterval<T> derivative = ops().create().img(aux);

				if (j != 0) {
					IntervalView<T> filter = dimension == j ? Views.rotate(kernelY, 0, j) : Views.rotate(kernelX, 0, j);
					ops().filter().convolve(derivative, Views.extendMirrorSingle(aux), filter);
				} else {
					if (dimension == j) {
						IntervalView<T> filter = Views.interval(kernelY, kernelY);
						ops().filter().convolve(derivative, Views.extendMirrorSingle(aux), filter);
					} else {
						IntervalView<T> filter = Views.interval(kernelX, kernelX);
						ops().filter().convolve(derivative, Views.extendMirrorSingle(aux), filter);
					}
				}
				aux = derivative;

			}
			RandomAccessibleInterval<T> out = aux;


			squareMapOp.compute1(out, out);
			RandomAccessibleInterval<T> temp = (RandomAccessibleInterval<T>) ops().math().add(output, out);
			ops().copy().rai(output,temp);
		}

		sqrtMapOp.compute1(output, output);
	}

	@Override
	public void compute0(RandomAccessibleInterval<T> output) {
		compute1(in(),output);
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = createOutputOp.compute1(input);
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
