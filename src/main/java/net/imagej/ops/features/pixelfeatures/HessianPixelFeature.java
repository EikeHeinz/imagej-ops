package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
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

	private UnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView> hesseComputer;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView> gaussFeatureOp;

	// TODO add gauss call

	@Override
	public void initialize() {
		hesseComputer = Functions.unary(ops(), Ops.Filter.Hessian.class, CompositeIntervalView.class, in());
		createRAIFromRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
		gaussFeatureOp = Functions.unary(ops(), Ops.Pixelfeatures.GaussPixelFeature.class, CompositeIntervalView.class, in(), minSigma,maxSigma);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public CompositeIntervalView<T, RealComposite<T>> compute1(RandomAccessibleInterval<T> input) {

		CompositeIntervalView<T, RealComposite<T>> hessianMatrix = hesseComputer.compute1(input);

		List<RandomAccessibleInterval<T>> results = new ArrayList<>();


		if (input.numDimensions() == 2) {

			/*
			 * output px value in hessian matrix is: [[XX,XY],[YX,YY]] =
			 * 
			 * [[a,b],[c,d]] Trace T=a+d Determinant D=ad-bc
			 * 
			 * module: sqrt(a^2+bc+d^2)
			 * 
			 * First eigenvalue: (T/2) + sqrt((4b^2+(a-d)^2)/2) Second
			 * eigenvalue: (T/2) - sqrt((4b^2+(a-d)^2)/2)
			 */

			Cursor<RealComposite<T>> hessianCursor = Views.iterable(hessianMatrix).cursor();
			RandomAccessibleInterval<T> trace = createRAIFromRAI.compute1(input);
			RandomAccessibleInterval<T> determinant = createRAIFromRAI.compute1(input);
			RandomAccessibleInterval<T> module = createRAIFromRAI.compute1(input);
			RandomAccessibleInterval<T> firstEigenvalue = createRAIFromRAI.compute1(input);
			RandomAccessibleInterval<T> secondEigenvalue = createRAIFromRAI.compute1(input);
			RandomAccess<T> traceRA = trace.randomAccess();
			RandomAccess<T> determinantRA = determinant.randomAccess();
			RandomAccess<T> moduleRA = module.randomAccess();
			RandomAccess<T> firstEigenvalueRA = firstEigenvalue.randomAccess();
			RandomAccess<T> secondEigenvalueRA = secondEigenvalue.randomAccess();
			while (hessianCursor.hasNext()) {
				RealComposite<T> composite = hessianCursor.next();
				long[] position = new long[2];
				hessianCursor.localize(position);
				traceRA.setPosition(position);
				double traceResult = composite.get(0).getRealDouble() + composite.get(3).getRealDouble();
				traceRA.get().setReal(traceResult);

				// determinant
				double ad = composite.get(0).getRealDouble() * composite.get(3).getRealDouble();
				double bc = composite.get(1).getRealDouble() * composite.get(2).getRealDouble();
				determinantRA.setPosition(position);
				determinantRA.get().setReal(ad - bc);

				// module
				double asquared = composite.get(0).getRealDouble() * composite.get(0).getRealDouble();
				double dsquared = composite.get(3).getRealDouble() * composite.get(3).getRealDouble();
				double moduleResult = Math.sqrt(asquared + bc + dsquared);
				moduleRA.get().setReal(moduleResult);

				// first + second eigenvalues
				double traceDiv2 = traceResult / 2;
				double bSquared4 = 4 * composite.get(1).getRealDouble() * composite.get(1).getRealDouble();
				double aMinusdSquared = Math.pow(composite.get(0).getRealDouble() -  composite.get(3).getRealDouble(), 2);
				double sqrt = Math.sqrt((bSquared4 + aMinusdSquared)/2);

				firstEigenvalueRA.setPosition(position);
				double firstEigenvalueResult = traceDiv2 + sqrt;
				firstEigenvalueRA.get().setReal(firstEigenvalueResult);

				secondEigenvalueRA.setPosition(position);
				double secondEigenvalueResult = traceDiv2 - sqrt;
				secondEigenvalueRA.get().setReal(secondEigenvalueResult);

			}
			results.add(module);
			results.add(trace);
			results.add(determinant);
			results.add(firstEigenvalue);
			results.add(secondEigenvalue);
		}

		// TODO implement 3d case
		
		// } else if (input.numDimensions() == 3) {
		//
		// RandomAccessibleInterval<T> xx = hesseOutput[0][0];//
		// hesseSlices.get(0);
		// RandomAccessibleInterval<T> xy = hesseOutput[0][1];//
		// hesseSlices.get(1);
		// RandomAccessibleInterval<T> xz = hesseOutput[0][2];//
		// hesseSlices.get(2);
		// RandomAccessibleInterval<T> yx = hesseOutput[1][0];//
		// hesseSlices.get(3);
		// RandomAccessibleInterval<T> yy = hesseOutput[1][1];//
		// hesseSlices.get(4);
		// RandomAccessibleInterval<T> yz = hesseOutput[1][2];//
		// hesseSlices.get(5);
		// RandomAccessibleInterval<T> zx = hesseOutput[2][0]; //
		// hesseSlices.get(6);
		// RandomAccessibleInterval<T> zy = hesseOutput[2][1];//
		// hesseSlices.get(7);
		// RandomAccessibleInterval<T> zz = hesseOutput[2][2];//
		// hesseSlices.get(8);
		//
		// long[] dims = new long[in().numDimensions() + 2];
		// for (int i = 0; i < dims.length - 1; i++) {
		// dims[i] = in().dimension(i);
		// }
		// // for now only trace and determinant are supported
		// dims[dims.length - 1] = 2;
		// Dimensions dim = FinalDimensions.wrap(dims);
		// output = createRAIFromDim.compute1(dim);
		//
		// IntervalView<T> traceSlice =
		// Views.hyperSlice(Views.hyperSlice(output, 3, 0), 3, 0);
		// IntervalView<T> determinantSlice =
		// Views.hyperSlice(Views.hyperSlice(output, 3, 0), 3, 1);
		//
		// // calculate trace
		// RandomAccessibleInterval<T> trace = createRAIFromRAI.compute1(xx);
		// addRAI.compute2(xx, yy, trace);
		// addRAI.compute2(trace, zz, traceSlice);
		//
		// // calculate determinant
		// // det = xx(yyzz - yzzy) - xy(yxzz - yzzx) + xz(yxzy - yyzx)
		// RandomAccessibleInterval<T> determinant =
		// createRAIFromRAI.compute1(xx);
		//
		// RandomAccessibleInterval<T> yyzz = createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(yy, zz, yyzz);
		// RandomAccessibleInterval<T> yzzy = createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(yz, zy, yzzy);
		// RandomAccessibleInterval<T> yyzzyzzy = createRAIFromRAI.compute1(xx);
		// subtractRAI.compute2(yyzz, yzzy, yyzzyzzy);
		// RandomAccessibleInterval<T> intermediateResult1 =
		// createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(xx, yyzzyzzy, intermediateResult1);
		//
		// RandomAccessibleInterval<T> yxzz = createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(yx, zz, yxzz);
		// RandomAccessibleInterval<T> yzzx = createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(yz, zx, yzzx);
		// RandomAccessibleInterval<T> yxzzyzzx = createRAIFromRAI.compute1(xx);
		// subtractRAI.compute2(yxzz, yzzx, yxzzyzzx);
		// RandomAccessibleInterval<T> intermediateResult2 =
		// createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(xy, yxzzyzzx, intermediateResult2);
		//
		// RandomAccessibleInterval<T> yxzy = createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(yx, zy, yxzy);
		// RandomAccessibleInterval<T> yyzx = createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(yy, zx, yyzx);
		// RandomAccessibleInterval<T> yxzyyyzx = createRAIFromRAI.compute1(xx);
		// subtractRAI.compute2(yxzy, yyzx, yxzyyyzx);
		// RandomAccessibleInterval<T> intermediateResult3 =
		// createRAIFromRAI.compute1(xx);
		// multiplyRAI.compute2(xz, yxzyyyzx, intermediateResult3);
		//
		// subtractRAI.compute2(intermediateResult1, intermediateResult2,
		// determinant);
		//
		// addRAI.compute2(determinant, intermediateResult3, determinantSlice);
		//
		// }

		RandomAccessibleInterval<T> stacked = Views.stack(results);
		return Views.collapseReal(stacked);
	}
}
