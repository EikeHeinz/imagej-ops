package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.BilateralFeature;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.BilateralFeature.class)
public class BilateralPixelFeature<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements BilateralFeature {

	private int radius = 10;
	private double[] spatial = new double[] { 5, 10 };
	private double[] domain = new double[] { 50, 100 };
	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> filterOps;

	@Override
	public void initialize() {
		filterOps = new ArrayList<>();
		for (int i = 0; i < spatial.length; i++) {
			filterOps.add(RAIs.function(ops(), Ops.Filter.BilateralFilter.class, in(), spatial[i], domain[i], radius));

		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> op : filterOps) {
			results.add(op.calculate(input));
		}
		return Views.stack(results);
	}

}
