
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import Jama.Matrix;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.Kuwahara;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
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

@Plugin(type = Ops.Pixelfeatures.Kuwahara.class)
public class KuwaharaPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
	implements Kuwahara
{

	// TODO verify results, combine criterion calculation, create initialize

	@Parameter
	private int kernelSize;

	@Parameter
	private int numberOfAngles;

	@Parameter(required = false)
	private KuwaharaCriterionMethod criterionMethod =
		KuwaharaCriterionMethod.VARIANCE;

	private int imageWidth;

	private int imageHeight;

	private int kernelWidth;

	private int kernelHeight;

	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createOp;

	private ArrayList<RandomAccessibleInterval<T>> kernels;

	// TODO add string to enum for GUI?
	public enum KuwaharaCriterionMethod {
			VARIANCE, VARIANCE_DIV_MEAN, VARIANCE_DIV_MEAN_SQR
	}

	@Override
	public void initialize() {
		createOp = RAIs.function(ops(), Ops.Create.Img.class, in());

		/*
		 * create kernel and rotate it
		 */

//		int nB = 3;
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
//		System.out.println("OFFSET:" + offset + "|sizeTemp:" + sizeTemp);

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

			kernels.add(rotatedKernel);
		}
	}

	@Override
	public RandomAccessibleInterval<T> calculate(
		RandomAccessibleInterval<T> input)
	{
		// int size = (sizeROI + 1) / 2;
		// int offset = (sizeROI - 1) / 2;
		//
		// float[][] mean = new float[(int)input.dimension(0)][(int)
		// input.dimension(1)];
		// float[][] variance = new float[(int)input.dimension(0)][(int)
		// input.dimension(1)];
		//
		// // test
		// int x1start = offset;
		// int y1start = offset;

		// TODO all 4 parameters unnecessary?
		imageWidth = (int) input.dimension(0);
		imageHeight = (int) input.dimension(1);
		kernelWidth = kernelSize;
		kernelHeight = kernelSize;

		long[] mins = new long[2];
		input.min(mins);
		long imageMin = (mins[0] > mins[1] ? mins[1] : mins[0]);
		// subtract the minimum and
		// store square and value of image in integer arrays
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
			System.out.println(kernelWidth + "|" + kernel.dimension(0));

			i++;
			// TODO: combine criterion calculation
			switch (criterionMethod) {
				case VARIANCE:
					calculateCriterionVariance(convolved1, convolved2, kernelSum, value,
						criterion);
					break;
				case VARIANCE_DIV_MEAN:
					calculateCriterionVarianceDivMean(convolved1, convolved2, kernelSum,
						value, criterion);
					break;
				case VARIANCE_DIV_MEAN_SQR:
					calculateCriterionVarianceDivMean2(convolved1, convolved2, kernelSum,
						value, criterion);
					break;
			}
			KuwaharaGM(value, criterion, kernel, resultTemp, resultCriterionTemp);
			setResultAndCriterion(result, resultTemp, resultCriterion,
				resultCriterionTemp);
		}

		// put the result into the image
		RandomAccessibleInterval<T> output = createOp.calculate(input);
		putFloat2Image(output, result, imageMin); // add also the minimum back
		// to
		// the
		// image to avoid that the offset
		// shifts between images of a stack
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

		x1min = (kernelWidth - 1) / 2;
		x1max = imageWidth - (kernelWidth - 1) / 2 - 1;
		y1min = (kernelHeight - 1) / 2;
		y1max = imageHeight - (kernelHeight - 1) / 2 - 1;

		int sum1, sum2, n, i;

		int x1minPos, y1minPos;
		double min;
		RandomAccess<T> resultRA = result.randomAccess();
		RandomAccess<T> resultCriterionRA = resultCriterion.randomAccess();
		RandomAccess<T> valueRA = value.randomAccess();
		for (int x1 = x1min; x1 <= x1max; x1++) {
			for (int y1 = y1min; y1 <= y1max; y1++) {
				x2min = x1 - (kernelWidth - 1) / 2;
				x2max = x1 + (kernelWidth - 1) / 2;
				y2min = y1 - (kernelHeight - 1) / 2;
				y2max = y1 + (kernelHeight - 1) / 2;
				i = 0;
				n = 0;
				min = Double.MAX_VALUE;
				x1minPos = x1;
				y1minPos = y1;
				Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
				RandomAccess<T> criterionRA = criterion.randomAccess();
				for (int y2 = y2min; y2 <= y2max; y2++) {
					for (int x2 = x2min; x2 <= x2max; x2++) {
						// if (pixelsKernel[i++] > 0) { // searches for minimal
						// criterion along
						// the lines in the kernels
						// (=shifting)
						if (kernelCursor.hasNext()) {
							if (kernelCursor.next().getRealDouble() > 0) {
								criterionRA.setPosition(new int[] { x2, y2 });
								// if (criterion[x2][y2] < min) {
								if (criterionRA.get().getRealDouble() < min) {
									// min = criterion[x2][y2];
									min = criterionRA.get().getRealDouble();
									x1minPos = x2;
									y1minPos = y2;
									n++;
								}
							}
						}
					} // y2
				} // x2
					// result[x1][y1] = value[x1minPos][y1minPos];
				resultRA.setPosition(new int[] { x1, y1 });
				valueRA.setPosition(new int[] { x1minPos, y1minPos });
				resultRA.get().setReal(valueRA.get().getRealDouble());
				// resultCriterion[x1][y1] = min;
				resultCriterionRA.setPosition(new int[] { x1, y1 });
				resultCriterionRA.get().setReal(min);
			} // y1
		} // x1
	}

	void setResultAndCriterion(RandomAccessibleInterval<T> result,
		RandomAccessibleInterval<T> resultTemp,
		RandomAccessibleInterval<T> resultCriterion,
		RandomAccessibleInterval<T> resultCriterionTemp)
	{
		RandomAccess<T> resultRA = result.randomAccess();
		RandomAccess<T> resultTempRA = resultTemp.randomAccess();
		RandomAccess<T> resultCriterionRA = resultCriterion.randomAccess();
		RandomAccess<T> resultCriterionTempRA = resultCriterionTemp.randomAccess();
		for (int x1 = kernelWidth; x1 < imageWidth - kernelWidth; x1++) {
			for (int y1 = kernelHeight; y1 < imageHeight - kernelHeight; y1++) {
				int[] position = new int[] { x1, y1 };
				resultCriterionTempRA.setPosition(position);
				resultCriterionRA.setPosition(position);
				// if (resultCriterionTemp[x1][y1] < resultCriterion[x1][y1]) {
				if (resultCriterionTempRA.get().getRealDouble() < resultCriterionRA
					.get().getRealDouble())
				{
					// resultCriterion[x1][y1] = resultCriterionTemp[x1][y1];
					resultCriterionRA.get().setReal(resultCriterionTempRA.get()
						.getRealDouble());
					// result[x1][y1]=100/resultCriterionTemp[x1][y1]; // show
					// how the
					// criterion looks like
					// result[x1][y1] = resultTemp[x1][y1];
					resultRA.setPosition(position);
					resultTempRA.setPosition(position);
					resultRA.get().setReal(resultTempRA.get().getRealDouble());
				}
			}
		}
	}

//	private void setFloatArray(double[][] array, float val) {
//		for (int x1 = 0; x1 < imageWidth; x1++) {
//			for (int y1 = 0; y1 < imageHeight; y1++) {
//				array[x1][y1] = val;
//			}
//		}
//	}

	// TODO inline kernelSum, combine all three methods based on criterion
	private final void calculateCriterionVariance(
		RandomAccessibleInterval<T> imSum,
		RandomAccessibleInterval<T> imSumOfSquares, double kernelSum,
		RandomAccessibleInterval<T> value, RandomAccessibleInterval<T> criterion)
	{
		Cursor<T> imSumCursor = Views.iterable(imSum).cursor();
		RandomAccess<T> imSumOfSquaresRA = imSumOfSquares.randomAccess();
		RandomAccess<T> valueRA = value.randomAccess();
		RandomAccess<T> criterionRA = criterion.randomAccess();
		while (imSumCursor.hasNext()) {
			imSumCursor.next();
			long[] pos = new long[2];
			imSumCursor.localize(pos);
			imSumOfSquaresRA.setPosition(pos);
			valueRA.setPosition(pos);
			criterionRA.setPosition(pos);
			double temp = imSumCursor.get().getRealDouble() / kernelSum;
			valueRA.get().setReal(temp);
			double temp2 = imSumOfSquaresRA.get().getRealDouble() / kernelSum;
			criterionRA.get().setReal(temp2 - (Math.pow(temp, 2)));
		}
		// for (int x1 = 0; x1 < imageWidth; x1++) {
		// for (int y1 = 0; y1 < imageHeight; y1++) {
		// value[x1][y1] = imSum[x1][y1] / kernelSum;
		// criterion[x1][y1] = imSumOfSquares[x1][y1] / kernelSum -
		// value[x1][y1] *
		// value[x1][y1];
		// }
		// }
	}

	private final void calculateCriterionVarianceDivMean(
		RandomAccessibleInterval<T> imSum,
		RandomAccessibleInterval<T> imSumOfSquares, double kernelSum,
		RandomAccessibleInterval<T> value, RandomAccessibleInterval<T> criterion)
	{
		Cursor<T> imSumCursor = Views.iterable(imSum).cursor();
		RandomAccess<T> imSumOfSquaresRA = imSumOfSquares.randomAccess();
		RandomAccess<T> valueRA = value.randomAccess();
		RandomAccess<T> criterionRA = criterion.randomAccess();
		while (imSumCursor.hasNext()) {
			imSumCursor.next();
			long[] pos = new long[2];
			imSumCursor.localize(pos);
			imSumOfSquaresRA.setPosition(pos);
			valueRA.setPosition(pos);
			criterionRA.setPosition(pos);
			double temp = imSumCursor.get().getRealDouble() / kernelSum;
			valueRA.get().setReal(temp);
			double temp2 = (imSumOfSquaresRA.get().getRealDouble() / kernelSum) -
				(Math.pow(temp, 2)) / (temp + Float.MIN_VALUE);
			criterionRA.get().setReal(temp2);

			// for (int x1 = 0; x1 < imageWidth; x1++) {
			// for (int y1 = 0; y1 < imageHeight; y1++) {
			// value[x1][y1] = imSum[x1][y1] / kernelSum;
			// criterion[x1][y1] = (imSumOfSquares[x1][y1] / kernelSum -
			// value[x1][y1] * value[x1][y1]) / (value[x1][y1] +
			// Float.MIN_VALUE);
			// }
		}
	}

	private final void calculateCriterionVarianceDivMean2(
		RandomAccessibleInterval<T> imSum,
		RandomAccessibleInterval<T> imSumOfSquares, double kernelSum,
		RandomAccessibleInterval<T> value, RandomAccessibleInterval<T> criterion)
	{
		Cursor<T> imSumCursor = Views.iterable(imSum).cursor();
		RandomAccess<T> imSumOfSquaresRA = imSumOfSquares.randomAccess();
		RandomAccess<T> valueRA = value.randomAccess();
		RandomAccess<T> criterionRA = criterion.randomAccess();
		while (imSumCursor.hasNext()) {
			imSumCursor.next();
			long[] pos = new long[2];
			imSumCursor.localize(pos);
			imSumOfSquaresRA.setPosition(pos);
			valueRA.setPosition(pos);
			criterionRA.setPosition(pos);
			double temp = imSumCursor.get().getRealDouble() / kernelSum;
			valueRA.get().setReal(temp);
			double temp2 = (imSumOfSquaresRA.get().getRealDouble() / kernelSum) -
				(Math.pow(temp, 2)) / (Math.pow(temp, 2) + Float.MIN_VALUE);
			criterionRA.get().setReal(temp2);

			// for (int x1 = 0; x1 < imageWidth; x1++) {
			// for (int y1 = 0; y1 < imageHeight; y1++) {
			// value[x1][y1] = imSum[x1][y1] / kernelSum;
			// criterion[x1][y1] = (imSumOfSquares[x1][y1] / kernelSum -
			// value[x1][y1] * value[x1][y1]) / (value[x1][y1] * value[x1][y1] +
			// Float.MIN_VALUE);
			// }
		}
	}

	private void putFloat2Image(RandomAccessibleInterval<T> input,
		RandomAccessibleInterval<T> imFloat, long imMin)
	{
		int x2, y2;
		RandomAccess<T> inputRA = input.randomAccess();
		RandomAccess<T> imFloatRA = imFloat.randomAccess();
		// Cursor<T> inputCursor = Views.iterable(input).cursor();
		// while(inputCursor.hasNext()) {
		//
		// }
		for (int x1 = 0; x1 < imageWidth; x1++) {
			for (int y1 = 0; y1 < imageHeight; y1++) {
				x2 = x1;
				y2 = y1; // duplicate the last meaningful pixels to the
				// boundaries
				if (x1 < kernelWidth) {
					x2 = kernelWidth;
				}
				if (x1 >= imageWidth - kernelWidth) {
					x2 = imageWidth - kernelWidth - 1;
				}
				if (y1 < kernelHeight) {
					y2 = kernelHeight;
				}
				if (y1 >= imageHeight - kernelHeight) {
					y2 = imageHeight - kernelHeight - 1;
				}
				inputRA.setPosition(new int[] { x1, y1 });
				imFloatRA.setPosition(new int[] { x2, y2 });
				inputRA.get().setReal(imFloatRA.get().getRealDouble() + 0.5 + imMin);
			}
		}

	}

}
