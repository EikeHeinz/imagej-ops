package net.imagej.ops.image.pixelfeature;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

@Plugin(type = Ops.Image.HessianPxFeature.class, name = Ops.Image.HessianPxFeature.NAME)
public class HessianPixelFeatureOp<T extends RealType<T>> extends AbstractPixelFeatureOp<T> {

	// TODO init() - create all used ops in init


	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrtMapOp;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> addRAIOp;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> createRAIOp;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> multiplyRAIOp;
	@SuppressWarnings("rawtypes")
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval> copyRAIOp;
	@SuppressWarnings("rawtypes")
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval> subtractRAIOp;
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<Dimensions, RandomAccessibleInterval> createRAIFromDimOp;


	// TODO initialize multiplyOp(img,type) and divideOp(img,type)

	@Override
	public void initialize() {
		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMapOp = Computers.unary(ops(), Ops.Map.class, in(), in(), sqrtOp);
        addRAIOp = Computers.binary(ops(), Ops.Math.Add.class, RandomAccessibleInterval.class, in(), in());
		multiplyRAIOp = Computers.binary(ops(), Ops.Math.Multiply.class, RandomAccessibleInterval.class, in(), in());
		
		subtractRAIOp = Computers.binary(ops(), Ops.Math.Subtract.class, RandomAccessibleInterval.class, in(), in());
		createRAIOp = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class,
				in());
		createRAIFromDimOp = Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class,
				Dimensions.class);
		copyRAIOp = Computers.unary(ops(), Ops.Copy.RAI.class, RandomAccessibleInterval.class, in());
	}

	@Override
	public RandomAccessibleInterval<T> compute1(RandomAccessibleInterval<T> input) {

		// TODO n dimensional solution
		
		RandomAccessibleInterval<T> x = ops().filter().directionalDerivative(input, 0);
		RandomAccessibleInterval<T> xx = ops().filter().directionalDerivative(x, 0);
		

		RandomAccessibleInterval<T> xy = ops().filter().directionalDerivative(x, 1);

		RandomAccessibleInterval<T> y = ops().filter().directionalDerivative(input, 1);
		RandomAccessibleInterval<T> yx = ops().filter().directionalDerivative(y, 0);
		
		RandomAccessibleInterval<T> yy = ops().filter().directionalDerivative(y, 1);
		
		// dereference X and Y??
		x = null;
		y = null;

		/*
		 * output px value in hessian matrix is: [[XX,XY],[YX,YY]] =
		 * 
		 * [[a,b],[c,d]] Trace T=a+d Determinant D=ad-bc
		 * 
		 * First eigenvalue: (T/2) + sqrt((4b^2+(a-d)^2)/2) Second eigenvalue:
		 * (T/2) - sqrt((4b^2+(a-d)^2)/2)
		 */

		// create output img containing slice for each feature
		// (trace,determinant, 1st/2nd eigenvalue)
		long[] dims = new long[in().numDimensions() + 2];
		for (int i = 0; i < dims.length - 1; i++) {
			dims[i] = in().dimension(i);
		}
		dims[dims.length - 1] = 4;
		Dimensions dim = FinalDimensions.wrap(dims);
		// TODO create Dimensions object instead of using long array
		RandomAccessibleInterval<T> output = createRAIFromDimOp.compute1(dim);

		IntervalView<T> traceSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 0);
		IntervalView<T> determinantSlice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 1);
		IntervalView<T> eigenvalue1Slice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 2);
		IntervalView<T> eigenvalue2Slice = Views.hyperSlice(Views.hyperSlice(output, 3, 0), 2, 3);

		// calculate trace
		RandomAccessibleInterval<T> trace = createRAIOp.compute1(input);
		addRAIOp.compute2(xx, yy, trace);
		copyRAIOp.compute1(trace, traceSlice);

		// calculate determinant
		RandomAccessibleInterval<T> multiplyXXYY = createRAIOp.compute1(input);
		multiplyRAIOp.compute2(xx, yy, multiplyXXYY);

		RandomAccessibleInterval<T> multiplyXYYX = createRAIOp.compute1(input);
		multiplyRAIOp.compute2(xy, yx, multiplyXYYX);

		RandomAccessibleInterval<T> determinant = createRAIOp.compute1(input);
		subtractRAIOp.compute2(multiplyXXYY, multiplyXYYX, determinant);

		copyRAIOp.compute1(determinant, determinantSlice);

		// eigenvalues using trace and determinant:
		// (1/2) * (trace +/- sqrt(trace*trace - 4*determinant))
		RandomAccessibleInterval<T> traceSquare = createRAIOp.compute1(input);
		multiplyRAIOp.compute2(trace, trace, traceSquare);

		T type = Util.getTypeFromInterval(determinant);
		type.setReal(4.0d);
		// FIXME see comment above init()
		RandomAccessibleInterval<T> tempDeterminant = (RandomAccessibleInterval<T>) ops().math().multiply(determinant,
				type);
		RandomAccessibleInterval<T> sqrt = createRAIOp.compute1(input);
		subtractRAIOp.compute2(traceSquare, tempDeterminant, sqrt);
		
		sqrtMapOp.compute1(sqrt, sqrt);
		RandomAccessibleInterval<T> addSqrt = createRAIOp.compute1(input);
		addRAIOp.compute2(sqrt, trace, addSqrt);
		RandomAccessibleInterval<T> subtractSqrt = createRAIOp.compute1(input);
		subtractRAIOp.compute2(sqrt, trace, subtractSqrt);

		
		type.setReal(2.0d);
		// FIXME see comment above init()
		RandomAccessibleInterval<T> eigenvalue1 = (RandomAccessibleInterval<T>) ops().math().divide(addSqrt, type);
		RandomAccessibleInterval<T> eigenvalue2 = (RandomAccessibleInterval<T>) ops().math().divide(subtractSqrt, type);
		
		copyRAIOp.compute1(eigenvalue1, eigenvalue1Slice);
		copyRAIOp.compute1(eigenvalue2, eigenvalue2Slice);

		return output;

	}

}
