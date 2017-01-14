package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.KuwaharaFeature;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.KuwaharaFeature.class)
public class KuwaharaPixelFeature<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements KuwaharaFeature {
	
	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> kuwaharaOps;
	
	@Override
	public void initialize() {

		kuwaharaOps = new ArrayList<>();
		int currentSize = 3;

		while (currentSize <= 9) {
			kuwaharaOps.add(RAIs.function(ops(), Ops.Filter.Kuwahara.class, in(), currentSize));
			currentSize += 2;
		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> op : kuwaharaOps) {
			results.add(op.calculate(input));
		}
		return Views.stack(results);
	}
}
