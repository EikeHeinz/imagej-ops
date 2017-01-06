
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.Kuwahara;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import Jama.Matrix;

@Plugin(type = Ops.Pixelfeatures.Kuwahara.class)
public class KuwaharaPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Kuwahara
{

	// TODO verify results

	@Parameter
	private int kernelSize;

	@Parameter
	private int numberOfAngles;

	@Parameter(required = false)
	private KuwaharaCriterionMethod criterionMethod =
		KuwaharaCriterionMethod.VARIANCE;

	private int imageWidth;

	private int imageHeight;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	private List<RandomAccessibleInterval<T>> kernels;

	// TODO add string to enum for GUI?
	public enum KuwaharaCriterionMethod {
			VARIANCE, VARIANCE_DIV_MEAN, VARIANCE_DIV_MEAN_SQR
	}

	@Override
	public void initialize() {
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());

		// ------- create kernels ----------

		int sizeTemp = kernelSize + 2 * 3;

		// create an image with a line
		Img<T> kernel = (Img<T>) ops().create().img(new int[] { sizeTemp,
			sizeTemp });
		RandomAccess<T> kernelRA = kernel.randomAccess();

		int x1 = (sizeTemp - 1) / 2;
		for (int y1 = 0; y1 < sizeTemp; y1++) {
			kernelRA.setPosition(new int[] { x1, y1 });
			kernelRA.get().setOne();
		}

		double rotationAngle = 180 / numberOfAngles;

		kernels = new ArrayList<>();

		// floor instead of ceil because counting starts at 0
		long offset = sizeTemp / 2;

		IntervalView<T> translatedkernel = Views.translate(kernel, -offset,
			-offset);
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedKernel =
			Views.extendZero(translatedkernel);

		for (int i = 0; i < numberOfAngles; i++) {
			double currentAngle = i * rotationAngle;
			double[][] rotationArray = new double[2][3];
			rotationArray[0][0] = Math.cos(currentAngle);
			rotationArray[0][1] = -Math.sin(currentAngle);
			rotationArray[0][2] = 0;
			rotationArray[1][0] = Math.sin(currentAngle);
			rotationArray[1][1] = Math.cos(currentAngle);
			rotationArray[1][2] = 0;
			Matrix rotationMatrix = new Matrix(rotationArray);
			AffineTransform rotationTransform = new AffineTransform(rotationMatrix);

			AffineRandomAccessible<T, AffineGet> rotated = RealViews.affine(Views
				.interpolate(extendedKernel, new NLinearInterpolatorFactory<>()),
				rotationTransform);

			MixedTransformView<T> backtranslated = Views.translate(rotated, offset,
				offset);
			IntervalView<T> rotatedKernel = Views.interval(backtranslated, kernel);
			// subtract the 3 pixels added earlier
			FinalInterval interval = FinalInterval.createMinMax(3, 3, sizeTemp - 3,
				sizeTemp - 3);
			IntervalView<T> finalKernel = Views.interval(rotatedKernel, interval);

			// -- KERNEL SYSOUT DEBUG
//			System.out.println("Begin-Kernel--------------" + i);
//			Cursor<T> rotatedCursor = Views.iterable(rotatedKernel).cursor();
//			String values = "";
//			int counter = 0;
//			while (rotatedCursor.hasNext()) {
//				T value = rotatedCursor.next();
//				values += value + "|";
//				counter++;
//				if (counter == sizeTemp) {
//					counter = 0;
//					System.out.println(values);
//					values = "";
//				}
//
//			}
//			System.out.println("End-Kernel--------------");

			kernels.add(finalKernel);
		}

		// ------- end create kernels ----------
	}

	@Override
	public RandomAccessibleInterval<T> calculate(
		RandomAccessibleInterval<T> input)
	{
		// TODO unnecessary?
		imageWidth = (int) in().dimension(0);
		imageHeight = (int) in().dimension(1);

		long[] mins = new long[2];
		input.min(mins);
		long imageMin = (mins[0] > mins[1] ? mins[1] : mins[0]);
		// store value and sqr(value)
		RandomAccess<T> inputRA = input.randomAccess();
		RandomAccessibleInterval<T> image = createOp.calculate(input);
		RandomAccessibleInterval<T> imageSqr = createOp.calculate(input);
		RandomAccess<T> imageSqrRA = imageSqr.randomAccess();
		Cursor<T> imageCursor = Views.iterable(image).cursor();
		while (imageCursor.hasNext()) {
			imageCursor.next();
			long[] pos = new long[2];
			imageCursor.localize(pos);
			inputRA.setPosition(pos);
			imageSqrRA.setPosition(pos);
			// subtracting min for poisson statistics
			double value = inputRA.get().getRealDouble() - imageMin;
			imageCursor.get().setReal(value);
			imageSqrRA.get().setReal(Math.pow(value, 2));

		}

		RandomAccessibleInterval<T> value = createOp.calculate(input);
		RandomAccessibleInterval<T> criterion = createOp.calculate(input);
		RandomAccessibleInterval<T> result = createOp.calculate(input);
		RandomAccessibleInterval<T> resultTemp = createOp.calculate(input);
		RandomAccessibleInterval<T> resultCriterion = createOp.calculate(input);
		RandomAccessibleInterval<T> resultCriterionTemp = createOp.calculate(input);

		Cursor<T> resultCriterionCursor = Views.iterable(resultCriterion).cursor();
		while (resultCriterionCursor.hasNext()) {
			T resultValue = resultCriterionCursor.next();
			resultValue.setReal(Float.MAX_VALUE);
		}

		double kernelSum = 0.0d;
		int i = 0;
		// loop through the different line orientations
		for (RandomAccessibleInterval<T> kernel : kernels) {

			RandomAccessibleInterval<T> convolved1 = ops().filter().convolve(Views
				.interval(Views.extendMirrorDouble(image), image), kernel);
			RandomAccessibleInterval<T> convolved2 = ops().filter().convolve(Views
				.interval(Views.extendMirrorDouble(imageSqr), imageSqr), kernel);
			System.out.println("kernel " + i);
			kernelSum = kernelSum(kernel);
			System.out.println("Sum:" + kernelSum);
			i++;

			calculateCriterion(convolved1, convolved2, kernelSum, value, criterion);

			// maybe inline
			KuwaharaGM(value, criterion, kernel, resultTemp, resultCriterionTemp);

			// set result and criterion to correct values
			RandomAccess<T> resultRA = result.randomAccess();
			RandomAccess<T> resultTempRA = resultTemp.randomAccess();
			RandomAccess<T> resultCriterionRA = resultCriterion.randomAccess();
			FinalInterval interval = FinalInterval.createMinMax(kernelSize,
				kernelSize, imageWidth - kernelSize, imageHeight - kernelSize);
			Cursor<T> resultCriterionTempCursor = Views.interval(resultCriterionTemp,
				interval).cursor();
			while (resultCriterionTempCursor.hasNext()) {
				resultCriterionTempCursor.next();
				resultCriterionRA.setPosition(resultCriterionTempCursor);
				if (resultCriterionTempCursor.get().getRealDouble() < resultCriterionRA
					.get().getRealDouble())
				{
					resultCriterionRA.get().setReal(resultCriterionTempCursor.get()
						.getRealDouble());
					resultRA.setPosition(resultCriterionTempCursor);
					resultTempRA.setPosition(resultCriterionTempCursor);
					resultRA.get().setReal(resultTempRA.get().getRealDouble());
				}
			}
		}

		// put the result into the output image
		RandomAccessibleInterval<T> output = createOp.calculate(input);
		RandomAccess<T> outputRA = output.randomAccess();
		RandomAccess<T> imFloatRA = result.randomAccess();
		// Cursor<T> inputCursor = Views.iterable(input).cursor();
		// while(inputCursor.hasNext()) {
		//
		// }
		for (int x1 = 0; x1 < imageWidth; x1++) {
			for (int y1 = 0; y1 < imageHeight; y1++) {
				int x2 = x1;
				int y2 = y1; // duplicate the last meaningful pixels to the
				// boundaries
				if (x1 < kernelSize) {
					x2 = kernelSize;
				}
				if (x1 >= imageWidth - kernelSize) {
					x2 = imageWidth - kernelSize - 1;
				}
				if (y1 < kernelSize) {
					y2 = kernelSize;
				}
				if (y1 >= imageHeight - kernelSize) {
					y2 = imageHeight - kernelSize - 1;
				}
				outputRA.setPosition(new int[] { x1, y1 });
				imFloatRA.setPosition(new int[] { x2, y2 });
				// add min back to avoid shifting offset between images of a stack
				outputRA.get().setReal(imFloatRA.get().getRealDouble() + 0.5 +
					imageMin);
			}
		}
		return output;

	}

	private double kernelSum(RandomAccessibleInterval<T> kernel) {
		double kernelSum = 0;
		Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
		while (kernelCursor.hasNext()) {
			T value = kernelCursor.next();
			kernelSum += value.getRealDouble();
		}
		return kernelSum;
	}

	private void KuwaharaGM(RandomAccessibleInterval<T> value,
		RandomAccessibleInterval<T> criterion, RandomAccessibleInterval<T> kernel,
		RandomAccessibleInterval<T> result,
		RandomAccessibleInterval<T> resultCriterion)
	{
		int x1min, x1max, y1min, y1max;
		int x2min, x2max, y2min, y2max;

		x1min = (kernelSize - 1) / 2;
		x1max = imageWidth - (kernelSize - 1) / 2 - 1;
		y1min = (kernelSize - 1) / 2;
		y1max = imageHeight - (kernelSize - 1) / 2 - 1;

		int x1minPos, y1minPos;
		double min;
		RandomAccess<T> resultCriterionRA = resultCriterion.randomAccess();
		RandomAccess<T> valueRA = value.randomAccess();
		FinalInterval test = FinalInterval.createMinMax(x1min, y1min, x1max, y1max);
		Cursor<T> resultIntervalCursor = Views.interval(result, test).cursor();
		while (resultIntervalCursor.hasNext()) {
			resultIntervalCursor.next();
			int[] pos = new int[2];
			resultIntervalCursor.localize(pos);
			x2min = pos[0] - (kernelSize - 1) / 2;
			x2max = pos[0] + (kernelSize - 1) / 2;
			y2min = pos[1] - (kernelSize - 1) / 2;
			y2max = pos[1] + (kernelSize - 1) / 2;
			min = Double.MAX_VALUE;
			x1minPos = pos[0];
			y1minPos = pos[1];
			Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
			FinalInterval interval = FinalInterval.createMinMax(x2min, y2min, x2max,
				y2max);
			IntervalView<T> intervalCriterion = Views.interval(criterion, interval);
			Cursor<T> cursor = intervalCriterion.cursor();
			// find min criterion
			while (cursor.hasNext()) {
				cursor.next();
				if (kernelCursor.hasNext()) {
					if (kernelCursor.next().getRealDouble() > 0) {
						if (cursor.get().getRealDouble() < min) {
							min = cursor.get().getRealDouble();
							int[] position = new int[2];
							cursor.localize(position);
							x1minPos = position[0];
							y1minPos = position[1];
						}
					}
				}
			}

			valueRA.setPosition(new int[] { x1minPos, y1minPos });
			resultIntervalCursor.get().setReal(valueRA.get().getRealDouble());
			resultCriterionRA.setPosition(resultIntervalCursor);
			resultCriterionRA.get().setReal(min);
		}
	}


	private final void calculateCriterion(RandomAccessibleInterval<T> imSum,
		RandomAccessibleInterval<T> imSumOfSquares, double kernelSum,
		RandomAccessibleInterval<T> value, RandomAccessibleInterval<T> criterion)
	{
		Cursor<T> imSumCursor = Views.iterable(imSum).cursor();
		RandomAccess<T> imSumOfSquaresRA = imSumOfSquares.randomAccess();
		RandomAccess<T> valueRA = value.randomAccess();
		RandomAccess<T> criterionRA = criterion.randomAccess();
		while (imSumCursor.hasNext()) {
			imSumCursor.next();
			imSumOfSquaresRA.setPosition(imSumCursor);
			valueRA.setPosition(imSumCursor);
			criterionRA.setPosition(imSumCursor);
			double tempValue = imSumCursor.get().getRealDouble() / kernelSum;
			double tempCriterion = 0.0d;
			switch (criterionMethod) {
				case VARIANCE:
					tempCriterion = imSumOfSquaresRA.get().getRealDouble() / kernelSum -
						Math.pow(tempValue, 2);
					break;
				case VARIANCE_DIV_MEAN:
					tempCriterion = (imSumOfSquaresRA.get().getRealDouble() / kernelSum) -
						(Math.pow(tempValue, 2)) / (tempValue + Float.MIN_VALUE);
					break;
				case VARIANCE_DIV_MEAN_SQR:
					tempCriterion = (imSumOfSquaresRA.get().getRealDouble() / kernelSum) -
						(Math.pow(tempValue, 2)) / (Math.pow(tempValue, 2) +
							Float.MIN_VALUE);
					break;
			}
			valueRA.get().setReal(tempValue);
			criterionRA.get().setReal(tempCriterion - (Math.pow(tempValue, 2)));
		}
	}

	//	void setResultAndCriterion(RandomAccessibleInterval<T> result,
//		RandomAccessibleInterval<T> resultTemp,
//		RandomAccessibleInterval<T> resultCriterion,
//		RandomAccessibleInterval<T> resultCriterionTemp)
//	{
//		RandomAccess<T> resultRA = result.randomAccess();
//		RandomAccess<T> resultTempRA = resultTemp.randomAccess();
//		RandomAccess<T> resultCriterionRA = resultCriterion.randomAccess();
//		FinalInterval interval = FinalInterval.createMinMax(kernelSize, kernelSize,
//			imageWidth - kernelSize, imageHeight - kernelSize);
//		Cursor<T> resultCriterionTempCursor = Views.interval(resultCriterionTemp,
//			interval).cursor();
//		while (resultCriterionTempCursor.hasNext()) {
//			resultCriterionTempCursor.next();
//			resultCriterionRA.setPosition(resultCriterionTempCursor);
//			if (resultCriterionTempCursor.get().getRealDouble() < resultCriterionRA
//				.get().getRealDouble())
//			{
//				resultCriterionRA.get().setReal(resultCriterionTempCursor.get()
//					.getRealDouble());
//				resultRA.setPosition(resultCriterionTempCursor);
//				resultTempRA.setPosition(resultCriterionTempCursor);
//				resultRA.get().setReal(resultTempRA.get().getRealDouble());
//			}
//		}
//	}

//	private void putFloat2Image(RandomAccessibleInterval<T> input,
//		RandomAccessibleInterval<T> imFloat, long imMin)
//	{
//		int x2, y2;
//		RandomAccess<T> inputRA = input.randomAccess();
//		RandomAccess<T> imFloatRA = imFloat.randomAccess();
//		// Cursor<T> inputCursor = Views.iterable(input).cursor();
//		// while(inputCursor.hasNext()) {
//		//
//		// }
//		for (int x1 = 0; x1 < imageWidth; x1++) {
//			for (int y1 = 0; y1 < imageHeight; y1++) {
//				x2 = x1;
//				y2 = y1; // duplicate the last meaningful pixels to the
//				// boundaries
//				if (x1 < kernelSize) {
//					x2 = kernelSize;
//				}
//				if (x1 >= imageWidth - kernelSize) {
//					x2 = imageWidth - kernelSize - 1;
//				}
//				if (y1 < kernelSize) {
//					y2 = kernelSize;
//				}
//				if (y1 >= imageHeight - kernelSize) {
//					y2 = imageHeight - kernelSize - 1;
//				}
//				inputRA.setPosition(new int[] { x1, y1 });
//				imFloatRA.setPosition(new int[] { x2, y2 });
//				inputRA.get().setReal(imFloatRA.get().getRealDouble() + 0.5 + imMin);
//			}
//		}
//
//	}

}
