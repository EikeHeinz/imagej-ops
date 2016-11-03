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

@Plugin(type = Ops.Pixelfeatures.HessianPixelFeature.class, name = Ops.Pixelfeatures.HessianPixelFeature.NAME)
public class HessianPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>>
		implements Ops.Pixelfeatures.HessianPixelFeature {

	@Parameter
	private double minSigma;

	@Parameter
	private double maxSigma;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAIFromRAI;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView> hesseComputer;

	private List<UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>> gaussOps;

	// TODO check module

	@Override
	public void initialize() {
		hesseComputer = Functions.unary(ops(), Ops.Filter.Hessian.class, CompositeIntervalView.class, in());
		createRAIFromRAI = RAIs.function(ops(), Ops.Create.Img.class, in());

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
	public CompositeIntervalView<T, RealComposite<T>> compute1(RandomAccessibleInterval<T> input) {
		List<CompositeIntervalView<T, RealComposite<T>>> blurredHessianMatrices = new ArrayList<>();

		for (UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gaussOp : gaussOps) {
			blurredHessianMatrices.add(hesseComputer.compute1(gaussOp.compute1(input)));
		}

		List<RandomAccessibleInterval<T>> results = new ArrayList<>();

		/*
		 * output px value in hessian matrix is: [[XX,XY],[YX,YY]] =
		 * 
		 * [[a,b],[c,d]] Trace T=a+d Determinant D=ad-bc
		 * 
		 * module: sqrt(a^2+bc+d^2)
		 * 
		 * First eigenvalue: (T/2) + sqrt((4b^2+(a-d)^2)/2) Second eigenvalue:
		 * (T/2) - sqrt((4b^2+(a-d)^2)/2)
		 */

		List<RandomAccessibleInterval<T>> intermediateResults = new ArrayList<>();
		for (CompositeIntervalView<T, RealComposite<T>> hessianMatrix : blurredHessianMatrices) {
			Cursor<RealComposite<T>> hessianCursor = Views.iterable(hessianMatrix).cursor();
			RandomAccessibleInterval<T> trace = createRAIFromRAI.compute1(input);
			RandomAccessibleInterval<T> determinant = createRAIFromRAI.compute1(input);
			RandomAccess<T> traceRA = trace.randomAccess();
			RandomAccess<T> determinantRA = determinant.randomAccess();
			if (input.numDimensions() == 2) {
				// RandomAccessibleInterval<T> module =
				// createRAIFromRAI.compute1(input);
				RandomAccessibleInterval<T> firstEigenvalue = createRAIFromRAI.compute1(input);
				RandomAccessibleInterval<T> secondEigenvalue = createRAIFromRAI.compute1(input);
				// RandomAccess<T> moduleRA = module.randomAccess();
				RandomAccess<T> firstEigenvalueRA = firstEigenvalue.randomAccess();
				RandomAccess<T> secondEigenvalueRA = secondEigenvalue.randomAccess();
				while (hessianCursor.hasNext()) {
					RealComposite<T> composite = hessianCursor.next();
					long[] position = new long[2];
					hessianCursor.localize(position);

					// trace
					traceRA.setPosition(position);
					double traceResult = composite.get(0).getRealDouble() + composite.get(3).getRealDouble();
					traceRA.get().setReal(traceResult);

					// determinant
					double ad = composite.get(0).getRealDouble() * composite.get(3).getRealDouble();
					double bc = composite.get(1).getRealDouble() * composite.get(2).getRealDouble();
					determinantRA.setPosition(position);
					determinantRA.get().setReal(ad - bc);

					// module
					// double asquared = composite.get(0).getRealDouble() *
					// composite.get(0).getRealDouble();
					// double dsquared = composite.get(3).getRealDouble() *
					// composite.get(3).getRealDouble();
					// double moduleResult = Math.sqrt(asquared + bc +
					// dsquared);
					// moduleRA.get().setReal(moduleResult);

					// first + second eigenvalues
					double traceDiv2 = traceResult / 2;
					double bSquared4 = 4 * composite.get(1).getRealDouble() * composite.get(1).getRealDouble();
					double aMinusdSquared = Math
							.pow(composite.get(0).getRealDouble() - composite.get(3).getRealDouble(), 2);
					double sqrt = Math.sqrt((bSquared4 + aMinusdSquared) / 2);

					firstEigenvalueRA.setPosition(position);
					double firstEigenvalueResult = traceDiv2 + sqrt;
					firstEigenvalueRA.get().setReal(firstEigenvalueResult);

					secondEigenvalueRA.setPosition(position);
					double secondEigenvalueResult = traceDiv2 - sqrt;
					secondEigenvalueRA.get().setReal(secondEigenvalueResult);

				}
				intermediateResults.add(trace);
				// intermediateResults.add(module);
				intermediateResults.add(determinant);
				intermediateResults.add(firstEigenvalue);
				intermediateResults.add(secondEigenvalue);
				results.add(Views.stack(intermediateResults));
				intermediateResults.clear();

			} else if (input.numDimensions() == 3) {

				while (hessianCursor.hasNext()) {
					RealComposite<T> composite = hessianCursor.next();
					long[] position = new long[3];
					hessianCursor.localize(position);

					// trace
					traceRA.setPosition(position);
					double traceResult = composite.get(0).getRealDouble() + composite.get(4).getRealDouble()
							+ composite.get(8).getRealDouble();
					traceRA.get().setReal(traceResult);

					// determinant
					// det = a*e*i + b*f*g + c*d*h - g*e*c - h*f*a - i*d*b
					determinantRA.setPosition(position);
					double aei = composite.get(0).getRealDouble() * composite.get(4).getRealDouble()
							* composite.get(8).getRealDouble();
					double bfg = composite.get(1).getRealDouble() * composite.get(5).getRealDouble()
							* composite.get(6).getRealDouble();
					double cdh = composite.get(2).getRealDouble() * composite.get(3).getRealDouble()
							* composite.get(7).getRealDouble();
					double gec = composite.get(6).getRealDouble() * composite.get(4).getRealDouble()
							* composite.get(2).getRealDouble();
					double hfa = composite.get(7).getRealDouble() * composite.get(5).getRealDouble()
							* composite.get(0).getRealDouble();
					double idb = composite.get(8).getRealDouble() * composite.get(3).getRealDouble()
							* composite.get(1).getRealDouble();
					double determinantResult = aei + bfg + cdh - gec - hfa - idb;
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
