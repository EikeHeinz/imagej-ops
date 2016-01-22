package net.imagej.ops.filter.derivative;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Filter.DirectionalDerivative.class, name = Ops.Filter.DirectionalDerivative.NAME)
public class DirectionalDerivativeRAI<T extends RealType<T>> extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {
	
	@Parameter
	private int dimension;
	private IntervalView<T> kernelX;
	private IntervalView<T> kernelY;
	
	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernel = ops().create().kernelSobel(2);
		kernelX = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 0);
		kernelY = Views.hyperSlice(Views.hyperSlice(kernel, 3, 0), 2, 1);
	}

	@Override
	public void compute1(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {

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
		RandomAccessibleInterval<T> temp = (RandomAccessibleInterval<T>) ops().math().add(output, aux);
		ops().copy().rai(output, temp);
		
		
	}

	@Override
	public void compute0(RandomAccessibleInterval<T> output) {
		compute1(in(),output);
		
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> output = createOutput(input);
		compute1(input,output);
		return output;
	}

	@Override
	public RandomAccessibleInterval<T> compute0() {
		RandomAccessibleInterval<T> output = createOutput();
		compute1(in(),output);
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
