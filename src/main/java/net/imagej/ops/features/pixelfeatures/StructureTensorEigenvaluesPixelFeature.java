
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
import net.imagej.ops.Ops.Math.Sqr;
import net.imagej.ops.Ops.Pixelfeatures.StructureTensorFeature;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.StructureTensorFeature.class)
public class StructureTensorEigenvaluesPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements StructureTensorFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> derivativeComputerX;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> derivativeComputerY;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrRAI;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> multiplyRAI;

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	@Override
	public void initialize() {
		derivativeComputerX = RAIs.function(ops(), Ops.Filter.PartialDerivative.class, in(), 0);
		derivativeComputerY = RAIs.function(ops(), Ops.Filter.PartialDerivative.class, in(), 1);
		Sqr sqr = ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class);
		sqrRAI = RAIs.function(ops(), Ops.Map.class, in(), sqr, Util.getTypeFromInterval(in()));
		multiplyRAI = RAIs.binaryComputer(ops(), Ops.Math.Multiply.class, in(), in());
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));
		gaussOps = new ArrayList<>();
		for (int i = 0; i <= maxSteps; i++) {
			double[] sigmas = new double[in().numDimensions()];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(RAIs.function(ops(), Gauss.class, in(), sigmas));
		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			RandomAccessibleInterval<T> blurredInput = gaussOp.calculate(Views.interval(Views.extendMirrorDouble(input),input));
			RandomAccessibleInterval<T> derivativeX = derivativeComputerX.calculate(blurredInput);
			RandomAccessibleInterval<T> derivativeY = derivativeComputerY.calculate(blurredInput);
			List<RandomAccessibleInterval<T>> components = new ArrayList<>();

			RandomAccessibleInterval<T> squareX = sqrRAI.calculate(derivativeX);
			RandomAccessibleInterval<T> squareY = sqrRAI.calculate(derivativeY);
			RandomAccessibleInterval<T> xy = createRAI.calculate(input);
			multiplyRAI.compute(derivativeX, derivativeY, xy);

			components.add(squareX);
			components.add(xy);
			components.add(xy);
			components.add(squareY);

			CompositeIntervalView<T, RealComposite<T>> compositeview = Views.collapseReal(Views.stack(components));

			RandomAccessibleInterval<T> largeEigenvalues = createRAI.calculate(input);
			RandomAccess<T> largeEigenvaluesRA = largeEigenvalues.randomAccess();
			RandomAccessibleInterval<T> smallEigenvalues = createRAI.calculate(input);
			RandomAccess<T> smallEigenvaluesRA = smallEigenvalues.randomAccess();
			Cursor<RealComposite<T>> viewCursor = Views.iterable(compositeview).cursor();
			while (viewCursor.hasNext()) {
				RealComposite<T> composite = viewCursor.next();
				long[] position = new long[2];
				viewCursor.localize(position);
				largeEigenvaluesRA.setPosition(position);
				smallEigenvaluesRA.setPosition(position);
				double a = composite.get(0).getRealDouble();
				double b = composite.get(1).getRealDouble();
				double c = composite.get(2).getRealDouble();
				double d = composite.get(3).getRealDouble();
				double firstEigenvalue = ((a + d) / 2)
						+ (Math.sqrt(((Math.pow((a + d), 2) / 4) - ((a * d) - (b * c)))));
				double secondEigenvalue = ((a + d) / 2)
						- (Math.sqrt(((Math.pow((a + d), 2) / 4) - ((a * d) - (b * c)))));
				if (firstEigenvalue < secondEigenvalue) {
					largeEigenvaluesRA.get().setReal(secondEigenvalue);
					smallEigenvaluesRA.get().setReal(firstEigenvalue);
				} else {
					largeEigenvaluesRA.get().setReal(firstEigenvalue);
					smallEigenvaluesRA.get().setReal(secondEigenvalue);
				}

			}
			results.add(largeEigenvalues);
			results.add(smallEigenvalues);
		}

		return Views.stack(results);
	}

}
