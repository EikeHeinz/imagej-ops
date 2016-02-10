
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


public class HessianPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
{

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrtMap;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> addRAI;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createRAIFromRAI;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> multiplyRAI;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> copyRAI;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> subtractRAI;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<Dimensions, RandomAccessibleInterval> createRAIFromDim;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> multiplyRAIby4;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> divideRAIby2;
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>[][]> hesseComputer;
	private RandomAccessibleInterval<T>[][] hesseOutput;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {

		hesseOutput = new RandomAccessibleInterval[in().numDimensions()][in()
			.numDimensions()];
		hesseComputer = Computers.unary(ops(), Ops.Filter.Hessian.class,
			hesseOutput, in());

		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMap = Computers.unary(ops(), Ops.Map.class, in(), in(), sqrtOp);
		addRAI = Computers.binary(ops(), Ops.Math.Add.class,
			RandomAccessibleInterval.class, in(), in());
		multiplyRAI = Computers.binary(ops(), Ops.Math.Multiply.class,
			RandomAccessibleInterval.class, in(), in());

		T type4 = (T) ops().create().nativeType(Util.getTypeFromInterval(in())
			.getClass());
		type4.setReal(4.0d);
		multiplyRAIby4 = Computers.unary(ops(), Ops.Math.Multiply.class,
			RandomAccessibleInterval.class, in(), type4);
		T type2 = (T) ops().create().nativeType(Util.getTypeFromInterval(in())
			.getClass());
		type2.setReal(2.0d);

		divideRAIby2 = Computers.unary(ops(), Ops.Math.Divide.class,
			RandomAccessibleInterval.class, in(), type2);
		subtractRAI = Computers.binary(ops(), Ops.Math.Subtract.class,
			RandomAccessibleInterval.class, in(), in());
		createRAIFromRAI = Functions.unary(ops(), Ops.Create.Img.class,
			RandomAccessibleInterval.class, in());
		createRAIFromDim = Functions.unary(ops(), Ops.Create.Img.class,
			RandomAccessibleInterval.class, Dimensions.class);
		copyRAI = Computers.unary(ops(), Ops.Copy.RAI.class,
			RandomAccessibleInterval.class, in());
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public RandomAccessibleInterval<T> compute1(
		RandomAccessibleInterval<T> input)
	{

		RandomAccessibleInterval<T> output = null;

		hesseComputer.compute1(input, hesseOutput);

		if (input.numDimensions() == 2) {

			/*
			 * output px value in hessian matrix is: [[XX,XY],[YX,YY]] =
			 * 
			 * [[a,b],[c,d]] Trace T=a+d Determinant D=ad-bc
			 * 
			 * First eigenvalue: (T/2) + sqrt((4b^2+(a-d)^2)/2) Second
			 * eigenvalue: (T/2) - sqrt((4b^2+(a-d)^2)/2)
			 */

			// create output img containing slice for each feature
			// (trace,determinant, 1st/2nd eigenvalue)
			long[] dims = new long[in().numDimensions() + 2];
			for (int i = 0; i < dims.length - 1; i++) {
				dims[i] = in().dimension(i);
			}
			dims[dims.length - 1] = 4;
			Dimensions dim = FinalDimensions.wrap(dims);
			output = createRAIFromDim.compute1(dim);

			IntervalView<T> traceSlice = Views.hyperSlice(Views.hyperSlice(output, 3,
				0), 2, 0);
			IntervalView<T> determinantSlice = Views.hyperSlice(Views.hyperSlice(
				output, 3, 0), 2, 1);
			IntervalView<T> eigenvalue1Slice = Views.hyperSlice(Views.hyperSlice(
				output, 3, 0), 2, 2);
			IntervalView<T> eigenvalue2Slice = Views.hyperSlice(Views.hyperSlice(
				output, 3, 0), 2, 3);

			// calculate trace
			RandomAccessibleInterval<T> trace = createRAIFromRAI.compute1(input);
			addRAI.compute2(hesseOutput[0][0], hesseOutput[1][1], trace);
			copyRAI.compute1(trace, traceSlice);

			// calculate determinant
			RandomAccessibleInterval<T> xxyy = createRAIFromRAI.compute1(input);
			multiplyRAI.compute2(hesseOutput[0][0], hesseOutput[1][1], xxyy);

			RandomAccessibleInterval<T> xyyx = createRAIFromRAI.compute1(input);
			multiplyRAI.compute2(hesseOutput[0][1], hesseOutput[1][0], xyyx);

			RandomAccessibleInterval<T> determinant = createRAIFromRAI.compute1(
				input);
			subtractRAI.compute2(xxyy, xyyx, determinant);

			copyRAI.compute1(determinant, determinantSlice);

			// eigenvalues using trace and determinant:
			// (1/2) * (trace +/- sqrt(trace*trace - 4*determinant))
			RandomAccessibleInterval<T> traceSquare = createRAIFromRAI.compute1(
				input);
			multiplyRAI.compute2(trace, trace, traceSquare);

			RandomAccessibleInterval<T> tempDeterminant = createRAIFromRAI.compute1(
				input);
			multiplyRAIby4.compute1(determinant, tempDeterminant);

			RandomAccessibleInterval<T> sqrt = createRAIFromRAI.compute1(input);
			subtractRAI.compute2(traceSquare, tempDeterminant, sqrt);

			sqrtMap.compute1(sqrt, sqrt);
			RandomAccessibleInterval<T> addSqrt = createRAIFromRAI.compute1(input);
			addRAI.compute2(sqrt, trace, addSqrt);
			RandomAccessibleInterval<T> subtractSqrt = createRAIFromRAI.compute1(
				input);
			subtractRAI.compute2(sqrt, trace, subtractSqrt);

			divideRAIby2.compute1(addSqrt, eigenvalue1Slice);
			divideRAIby2.compute1(subtractSqrt, eigenvalue2Slice);

		}
		else if (input.numDimensions() == 3) {

			RandomAccessibleInterval<T> xx = hesseOutput[0][0];// hesseSlices.get(0);
			RandomAccessibleInterval<T> xy = hesseOutput[0][1];// hesseSlices.get(1);
			RandomAccessibleInterval<T> xz = hesseOutput[0][2];// hesseSlices.get(2);
			RandomAccessibleInterval<T> yx = hesseOutput[1][0];// hesseSlices.get(3);
			RandomAccessibleInterval<T> yy = hesseOutput[1][1];// hesseSlices.get(4);
			RandomAccessibleInterval<T> yz = hesseOutput[1][2];// hesseSlices.get(5);
			RandomAccessibleInterval<T> zx = hesseOutput[2][0]; // hesseSlices.get(6);
			RandomAccessibleInterval<T> zy = hesseOutput[2][1];// hesseSlices.get(7);
			RandomAccessibleInterval<T> zz = hesseOutput[2][2];// hesseSlices.get(8);

			long[] dims = new long[in().numDimensions() + 2];
			for (int i = 0; i < dims.length - 1; i++) {
				dims[i] = in().dimension(i);
			}
			// for now only trace and determinant are supported
			dims[dims.length - 1] = 2;
			Dimensions dim = FinalDimensions.wrap(dims);
			output = createRAIFromDim.compute1(dim);

			IntervalView<T> traceSlice = Views.hyperSlice(Views.hyperSlice(output, 3,
				0), 3, 0);
			IntervalView<T> determinantSlice = Views.hyperSlice(Views.hyperSlice(
				output, 3, 0), 3, 1);

			// calculate trace
			RandomAccessibleInterval<T> trace = createRAIFromRAI.compute1(xx);
			addRAI.compute2(xx, yy, trace);
			addRAI.compute2(trace, zz, traceSlice);

			// calculate determinant
			// det = xx(yyzz - yzzy) - xy(yxzz - yzzx) + xz(yxzy - yyzx)
			RandomAccessibleInterval<T> determinant = createRAIFromRAI.compute1(xx);

			RandomAccessibleInterval<T> yyzz = createRAIFromRAI.compute1(xx);
			multiplyRAI.compute2(yy, zz, yyzz);
			RandomAccessibleInterval<T> yzzy = createRAIFromRAI.compute1(xx);
			multiplyRAI.compute2(yz, zy, yzzy);
			RandomAccessibleInterval<T> yyzzyzzy = createRAIFromRAI.compute1(xx);
			subtractRAI.compute2(yyzz, yzzy, yyzzyzzy);
			RandomAccessibleInterval<T> intermediateResult1 = createRAIFromRAI
				.compute1(xx);
			multiplyRAI.compute2(xx, yyzzyzzy, intermediateResult1);

			RandomAccessibleInterval<T> yxzz = createRAIFromRAI.compute1(xx);
			multiplyRAI.compute2(yx, zz, yxzz);
			RandomAccessibleInterval<T> yzzx = createRAIFromRAI.compute1(xx);
			multiplyRAI.compute2(yz, zx, yzzx);
			RandomAccessibleInterval<T> yxzzyzzx = createRAIFromRAI.compute1(xx);
			subtractRAI.compute2(yxzz, yzzx, yxzzyzzx);
			RandomAccessibleInterval<T> intermediateResult2 = createRAIFromRAI
				.compute1(xx);
			multiplyRAI.compute2(xy, yxzzyzzx, intermediateResult2);

			RandomAccessibleInterval<T> yxzy = createRAIFromRAI.compute1(xx);
			multiplyRAI.compute2(yx, zy, yxzy);
			RandomAccessibleInterval<T> yyzx = createRAIFromRAI.compute1(xx);
			multiplyRAI.compute2(yy, zx, yyzx);
			RandomAccessibleInterval<T> yxzyyyzx = createRAIFromRAI.compute1(xx);
			subtractRAI.compute2(yxzy, yyzx, yxzyyyzx);
			RandomAccessibleInterval<T> intermediateResult3 = createRAIFromRAI
				.compute1(xx);
			multiplyRAI.compute2(xz, yxzyyyzx, intermediateResult3);

			subtractRAI.compute2(intermediateResult1, intermediateResult2,
				determinant);

			addRAI.compute2(determinant, intermediateResult3, determinantSlice);

		}
		return output;
	}
}
