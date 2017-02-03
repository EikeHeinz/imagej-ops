package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Math.Sqr;
import net.imagej.ops.Ops.Math.Sqrt;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.BinaryComputerOp;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

@Plugin(type = Ops.Pixelfeatures.ManualSobel.class)
public class Sobel<T extends RealType<T>> extends
		AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> implements Ops.Pixelfeatures.ManualSobel{

	private UnaryFunctionOp<Interval, RandomAccessibleInterval<T>> createOp;
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> squareMapOp;
	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrtMapOp;
	private BinaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> addOp;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAIFromRAI;

	@Override
	public void initialize() {
		createOp = (UnaryFunctionOp) Functions.unary(ops(), Ops.Create.Img.class, RandomAccessibleInterval.class,
				Dimensions.class, Util.getTypeFromInterval(in()));

		createRAIFromRAI = RAIs.function(ops(), Ops.Create.Img.class, in());

		Sqr squareOp = ops().op(Ops.Math.Sqr.class, RealType.class, RealType.class);
		squareMapOp = RAIs.computer(ops(), Ops.Map.class, in(), squareOp);
		Sqrt sqrtOp = ops().op(Ops.Math.Sqrt.class, RealType.class, RealType.class);
		sqrtMapOp = RAIs.computer(ops(), Ops.Map.class, in(), sqrtOp);
		addOp = RAIs.binaryComputer(ops(), Ops.Math.Add.class, in(), in());
	}

	@Override
	public RandomAccessibleInterval<T> calculate(RandomAccessibleInterval<T> input) {
		List<RandomAccessibleInterval<T>> results = new ArrayList<>();
		// multiply derivative by 1/8
		RandomAccessibleInterval<T> outTemp = sobelminmaxdiv(input);
		results.add(outTemp);
		results.add(sobeldiv(input));
		results.add(sobelminmax(input));
		results.add(getSobelAbs(input));
		// TODO Auto-generated method stub
		return Views.stack(results);
	}

	private RandomAccessibleInterval<T> sobelminmaxdiv(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> emp1 = ops().filter()
				.convolve(Views.interval(Views.extendMirrorDouble(input), input), getXDirection());
		RandomAccessibleInterval<T> emp2 = ops().filter()
				.convolve(Views.interval(Views.extendMirrorDouble(input), input), getYDirection());
		Cursor<T> emp1Cur = Views.iterable(emp1).cursor();
		Cursor<T> emp2Cur = Views.iterable(emp2).cursor();
		while(emp1Cur.hasNext()) {
			T valueEmp1 = emp1Cur.next();
			T valueEmp2 = emp2Cur.next();
			emp1Cur.get().setReal(valueEmp1.getRealDouble() * 0.125);
			emp2Cur.get().setReal(valueEmp2.getRealDouble() * 0.125);
		}
		RandomAccessibleInterval<T> temp1 = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> temp2 = createRAIFromRAI.calculate(input);
		squareMapOp.compute(emp1, temp1);
		squareMapOp.compute(emp2, temp2);
		RandomAccessibleInterval<T> outTemp = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> output = createRAIFromRAI.calculate(input);
		addOp.compute(temp1, temp2, outTemp);
		sqrtMapOp.compute(outTemp, outTemp);
		Cursor<T> outTempCursor = Views.iterable(outTemp).cursor();
		RandomAccess<T> outputRA = output.randomAccess();
		Pair<T, T> minMax = ops().stats().minMax((Iterable<T>)emp1); 
//		T min = ops().stats().min((Iterable<T>) emp1);
//		T max = ops().stats().max((Iterable<T>) emp1);
		while (outTempCursor.hasNext()) {
			outTempCursor.next();
			final double gradient = outTempCursor.get().getRealDouble();
			outputRA.setPosition(outTempCursor);
			outputRA.get().setReal(Math.min(Math.max(minMax.getA().getRealDouble(), gradient), minMax.getB().getRealDouble()));
		}
		return outTemp;
	}
	
	private RandomAccessibleInterval<T> sobeldiv(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> emp1 = ops().filter()
				.convolve(Views.interval(Views.extendMirrorDouble(input), input), getXDirection());
		RandomAccessibleInterval<T> emp2 = ops().filter()
				.convolve(Views.interval(Views.extendMirrorDouble(input), input), getYDirection());
		Cursor<T> emp1Cur = Views.iterable(emp1).cursor();
		Cursor<T> emp2Cur = Views.iterable(emp2).cursor();
		while(emp1Cur.hasNext()) {
			T valueEmp1 = emp1Cur.next();
			T valueEmp2 = emp2Cur.next();
			emp1Cur.get().setReal(valueEmp1.getRealDouble() * 0.125);
			emp2Cur.get().setReal(valueEmp2.getRealDouble() * 0.125);
		}
		RandomAccessibleInterval<T> temp1 = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> temp2 = createRAIFromRAI.calculate(input);
		squareMapOp.compute(emp1, temp1);
		squareMapOp.compute(emp2, temp2);
		RandomAccessibleInterval<T> outTemp = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> output = createRAIFromRAI.calculate(input);
		addOp.compute(temp1, temp2, outTemp);
		sqrtMapOp.compute(outTemp, outTemp);
		return outTemp;
	}
	
	private RandomAccessibleInterval<T> sobelminmax(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> emp1 = ops().filter()
				.convolve(Views.interval(Views.extendMirrorDouble(input), input), getXDirection());
		RandomAccessibleInterval<T> emp2 = ops().filter()
				.convolve(Views.interval(Views.extendMirrorDouble(input), input), getYDirection());
		RandomAccessibleInterval<T> temp1 = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> temp2 = createRAIFromRAI.calculate(input);
		squareMapOp.compute(emp1, temp1);
		squareMapOp.compute(emp2, temp2);
		RandomAccessibleInterval<T> outTemp = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> output = createRAIFromRAI.calculate(input);
		addOp.compute(temp1, temp2, outTemp);
		sqrtMapOp.compute(outTemp, outTemp);
		Cursor<T> outTempCursor = Views.iterable(outTemp).cursor();
		RandomAccess<T> outputRA = output.randomAccess();
		Pair<T, T> minMax = ops().stats().minMax((Iterable<T>)emp1); 
		while (outTempCursor.hasNext()) {
			outTempCursor.next();
			final double gradient = outTempCursor.get().getRealDouble();
			outputRA.setPosition(outTempCursor);
			outputRA.get().setReal(Math.min(Math.max(minMax.getA().getRealDouble(), gradient), minMax.getB().getRealDouble()));
		}
		return outTemp;
	}
	
	private RandomAccessibleInterval<T> getSobelAbs(RandomAccessibleInterval<T> input) {
		RandomAccessibleInterval<T> derix = ops().filter().partialDerivative(input, 0);
        RandomAccessibleInterval<T> deriy = ops().filter().partialDerivative(input, 1);
        Cursor<T> derixCur = Views.iterable(derix).cursor();
        RandomAccess<T> deriyRA = deriy.randomAccess();
        while(derixCur.hasNext()) {
            T valuex = derixCur.next();
            deriyRA.setPosition(derixCur);
            T valuey = deriyRA.get();
            derixCur.get().setReal(Math.abs(valuex.getRealDouble()));
            deriyRA.get().setReal(Math.abs(valuey.getRealDouble()));
        }
		RandomAccessibleInterval<T> temp1 = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> temp2 = createRAIFromRAI.calculate(input);
        squareMapOp.compute(derix, temp1);
		squareMapOp.compute(deriy, temp2);
		RandomAccessibleInterval<T> outTemp = createRAIFromRAI.calculate(input);
		RandomAccessibleInterval<T> output = createRAIFromRAI.calculate(input);
		addOp.compute(temp1, temp2, outTemp);
		sqrtMapOp.compute(outTemp, outTemp);
		return outTemp;
	}

	private RandomAccessibleInterval<T> getXDirection() {
		long[] dims = new long[] { 3, 3 };
		RandomAccessibleInterval<T> kernel = createOp.calculate(new FinalInterval(dims));
		Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
		kernelCursor.next();
		kernelCursor.get().setReal(1);
		kernelCursor.next();
		kernelCursor.get().setReal(0);
		kernelCursor.next();
		kernelCursor.get().setReal(-1);
		kernelCursor.next();
		kernelCursor.get().setReal(2);
		kernelCursor.next();
		kernelCursor.get().setReal(0);
		kernelCursor.next();
		kernelCursor.get().setReal(-2);
		kernelCursor.next();
		kernelCursor.get().setReal(1);
		kernelCursor.next();
		kernelCursor.get().setReal(0);
		kernelCursor.next();
		kernelCursor.get().setReal(-1);
		return kernel;
	}

	private RandomAccessibleInterval<T> getYDirection() {
		long[] dims = new long[] { 3, 3 };
		RandomAccessibleInterval<T> kernel = createOp.calculate(new FinalInterval(dims));
		Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
		kernelCursor.next();
		kernelCursor.get().setReal(1);
		kernelCursor.next();
		kernelCursor.get().setReal(2);
		kernelCursor.next();
		kernelCursor.get().setReal(1);
		kernelCursor.next();
		kernelCursor.get().setReal(0);
		kernelCursor.next();
		kernelCursor.get().setReal(0);
		kernelCursor.next();
		kernelCursor.get().setReal(0);
		kernelCursor.next();
		kernelCursor.get().setReal(-1);
		kernelCursor.next();
		kernelCursor.get().setReal(-2);
		kernelCursor.next();
		kernelCursor.get().setReal(-1);
		return kernel;
	}
}
