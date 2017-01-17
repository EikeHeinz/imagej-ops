
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;

public abstract class AbstractNeighborhoodStatBasedPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {

	@Parameter
	protected int span;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	protected UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>[] filterOp;

	@Override
	public void initialize() {
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
	}

	@Override
	public RandomAccessibleInterval<T> calculate(final RandomAccessibleInterval<T> in) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (int i = 0; i < filterOp.length; i++) {
			RandomAccessibleInterval<T> out = createRAI.calculate(in);
			filterOp[i].compute(Views.interval(Views.extendMirrorDouble(in), in), out);
			results.add(out);
		}
		return Views.stack(results);
	}
}
