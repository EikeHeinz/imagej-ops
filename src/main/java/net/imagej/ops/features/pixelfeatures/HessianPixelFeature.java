
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Filter.Gauss;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.HessianPixelFeature.class,
	name = Ops.Pixelfeatures.HessianPixelFeature.NAME)
public class HessianPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>>
	implements Ops.Pixelfeatures.HessianPixelFeature
{

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView> hessianOp;

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	@Override
	public void initialize() {
		hessianOp = Functions.unary(ops(), Ops.Filter.Hessian.class,
			CompositeIntervalView.class, in());
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());

		double maxSteps = ops().math().floor(Math.log(maxSigma) / Math.log(2));

		gaussOps = new ArrayList<>();
		for (int i = 0; i <= maxSteps; i++) {
			double[] sigmas = new double[in().numDimensions()];
			Arrays.fill(sigmas, Math.pow(2, i) * minSigma);
			gaussOps.add(RAIs.function(ops(), Gauss.class, in(), sigmas));
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public CompositeIntervalView<T, RealComposite<T>> calculate(
		RandomAccessibleInterval<T> input)
	{
		List<CompositeIntervalView<T, RealComposite<T>>> blurredHessianMatrices =
			new ArrayList<>();

		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			blurredHessianMatrices.add(hessianOp.calculate(gaussOp.calculate(input)));
		}

		List<RandomAccessibleInterval<T>> results = new ArrayList<>();

		/*
		 * for reference
		 * http://imagej.net/Trainable_Weka_Segmentation#Training_features_.282D
		 * .29 (2016-12-06)
		 */

		List<RandomAccessibleInterval<T>> intermediateResults = new ArrayList<>();
		for (CompositeIntervalView<T, RealComposite<T>> hessianMatrix : blurredHessianMatrices) {
			Cursor<RealComposite<T>> hessianCursor = Views.iterable(hessianMatrix)
				.cursor();
			RandomAccessibleInterval<T> trace = createRAI.calculate(input);
			RandomAccessibleInterval<T> determinant = createRAI.calculate(input);
			RandomAccess<T> traceRA = trace.randomAccess();
			RandomAccess<T> determinantRA = determinant.randomAccess();
			if (input.numDimensions() == 2) {
				RandomAccessibleInterval<T> module = createRAI.calculate(input);
				RandomAccessibleInterval<T> firstEigenvalue = createRAI.calculate(
					input);
				RandomAccessibleInterval<T> secondEigenvalue = createRAI.calculate(
					input);
				RandomAccessibleInterval<T> orientation = createRAI.calculate(input);
				RandomAccessibleInterval<T> gnsed = createRAI.calculate(input);
				RandomAccessibleInterval<T> sgned = createRAI.calculate(input);
				RandomAccess<T> moduleRA = module.randomAccess();
				RandomAccess<T> firstEigenvalueRA = firstEigenvalue.randomAccess();
				RandomAccess<T> secondEigenvalueRA = secondEigenvalue.randomAccess();
				RandomAccess<T> orientationRA = orientation.randomAccess();
				RandomAccess<T> gnsedRA = gnsed.randomAccess();
				RandomAccess<T> sgnedRA = sgned.randomAccess();
				while (hessianCursor.hasNext()) {
					RealComposite<T> composite = hessianCursor.next();
					double a = composite.get(0).getRealDouble();
					double b = composite.get(1).getRealDouble();
					double c = composite.get(2).getRealDouble();
					double d = composite.get(3).getRealDouble();
					long[] position = new long[2];
					hessianCursor.localize(position);

					// trace
					traceRA.setPosition(position);
					double traceResult = a + d;
					traceRA.get().setReal(traceResult);

					// determinant
					determinantRA.setPosition(position);
					determinantRA.get().setReal(a * d - b * c);

					// module
					double moduleResult = Math.sqrt(Math.pow(a, 2) + b * c + Math.pow(d,
						2));
					moduleRA.setPosition(position);
					moduleRA.get().setReal(moduleResult);

					// first + second eigenvalues
					double firstEigenvalueResult = ((a + d) / 2) + (Math.sqrt(((Math.pow(
						(a + d), 2) / 4) - ((a * d) - (b * c)))));
					firstEigenvalueRA.setPosition(position);
					firstEigenvalueRA.get().setReal(firstEigenvalueResult);

					double secondEigenvalueResult = ((a + d) / 2) - (Math.sqrt(((Math.pow(
						(a + d), 2) / 4) - ((a * d) - (b * c)))));
					secondEigenvalueRA.setPosition(position);
					secondEigenvalueRA.get().setReal(secondEigenvalueResult);

					// orientation
					double orientationResult = 0.5 * Math.acos(4 * Math.pow(b, 2) + Math
						.pow(a - d, 2));
					orientationRA.setPosition(position);
					orientationRA.get().setReal(orientationResult);

					double t = Math.pow(1, 0.75d);

					// gamma-normalized square eigenvalue difference
					double gnsedResult = Math.pow(t, 4) * Math.pow(a - d, 2) * (Math.pow(
						a - d, 2) + 4 * Math.pow(b, 2));
					gnsedRA.setPosition(position);
					gnsedRA.get().setReal(gnsedResult);

					// square of gamma-normalized eigenvalue difference
					double sgnedResult = Math.pow(t, 2) * (Math.pow(a - d, 2) + 4 * Math
						.pow(b, 2));
					sgnedRA.setPosition(position);
					sgnedRA.get().setReal(sgnedResult);
				}

				intermediateResults.add(module);
				intermediateResults.add(trace);
				intermediateResults.add(determinant);
				intermediateResults.add(firstEigenvalue);
				intermediateResults.add(secondEigenvalue);
				intermediateResults.add(orientation);
				intermediateResults.add(gnsed);
				intermediateResults.add(sgned);
				results.add(Views.stack(intermediateResults));
				intermediateResults.clear();

			}
			else if (input.numDimensions() == 3) {

				while (hessianCursor.hasNext()) {
					RealComposite<T> composite = hessianCursor.next();
					double a = composite.get(0).getRealDouble();
					double b = composite.get(1).getRealDouble();
					double c = composite.get(2).getRealDouble();
					double d = composite.get(3).getRealDouble();
					double e = composite.get(4).getRealDouble();
					double f = composite.get(5).getRealDouble();
					double g = composite.get(6).getRealDouble();
					double h = composite.get(7).getRealDouble();
					double i = composite.get(8).getRealDouble();
					long[] position = new long[3];
					hessianCursor.localize(position);

					// trace
					traceRA.setPosition(position);
					double traceResult = a + e + i;
					traceRA.get().setReal(traceResult);

					// determinant
					determinantRA.setPosition(position);
					double determinantResult = a * e * i + b * f * g + c * d * h - g * e *
						c - h * f * a - i * d * b;
					determinantRA.get().setReal(determinantResult);

				}

				intermediateResults.add(trace);
				intermediateResults.add(determinant);
				results.add(Views.stack(intermediateResults));
				intermediateResults.clear();

			}
		}

		RandomAccessibleInterval<T> stacked = Views.stack(results);
		return Views.collapseReal(stacked);
	}
}
