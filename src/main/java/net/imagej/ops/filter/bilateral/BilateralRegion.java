package net.imagej.ops.filter.bilateral;

import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Filter.BilateralRegionFilter.class)
public class BilateralRegion<T extends RealType<T>> extends AbstractUnaryComputerOp<IterableInterval<T>, T> implements Ops.Filter.BilateralRegionFilter {
	
	@Parameter
	private double spatial;
	
	@Parameter
	private double domain;

	@Override
	public void compute(IterableInterval<T> input, T output) {
		long[] dims = new long[input.numDimensions()];
		long[] centerPos = new long[input.numDimensions()];
//		RandomAccess<T> inputRA = ((RandomAccessibleInterval<T>) input).randomAccess();
		for(int i = 0; i < dims.length; i++) {
			dims[i]= input.dimension(i);
			centerPos[i] = dims[i]/2;
		}
		long maxSteps = (dims[0]*dims[1])/2;
		int step = 0;
		Cursor<T> inputCursor = input.cursor();
		while(inputCursor.hasNext() && step <= maxSteps){
			inputCursor.fwd();
			step++;
		}
//		inputRA.setPosition(centerPos);
//		double centerValue = inputRA.get().getRealDouble();
		double centerValue = inputCursor.get().getRealDouble();

		/*Cursor<T>*/ inputCursor = input.cursor();
		long[] testpos = new long[2];
		inputCursor.localize(testpos);
		double sum = 0.0d;
		double normalization = 0.0d;
		while (inputCursor.hasNext()) {
			double currentValue = inputCursor.next().getRealDouble();
			long[] cursorPos = new long[2];
			inputCursor.localize(cursorPos);

			double distance = Math.sqrt(Math.pow(centerPos[0] - cursorPos[0], 2)
					+ Math.pow(centerPos[1] - cursorPos[1], 2));
			double currentSpatial = gauss(distance, spatial);
			double difference = Math.abs(centerValue - currentValue);
			double currentDomain = gauss(difference, domain);
			sum += (currentValue * currentSpatial * currentDomain);
			normalization += currentSpatial * currentDomain;
		}
		
		output.setReal(1 / normalization * sum);
	}
	
	private double gauss(final double x, final double sigma) {
		return (1 / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp((-0.5 * x * x) / (sigma * sigma));
	}
}
