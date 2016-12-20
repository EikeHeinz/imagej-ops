
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import Jama.Matrix;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.Kuwahara;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
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

	@Parameter
	private int size;

	@Parameter
	private int numberOfAngles;

	private int imageWidth;

	private int imageHeight;

	private int kernelWidth;

	private int kernelHeight;

	private CriterionMethod criterionMethod = CriterionMethod.VARIANCE;

	public enum CriterionMethod {
			VARIANCE, VARIANCE_DIV_MEAN, VARIANCE_DIV_MEAN_SQR
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

		imageWidth = (int) input.dimension(0);
		imageHeight = (int) input.dimension(1);
		kernelWidth = size;
		kernelHeight = size;

		List<RandomAccessibleInterval<T>> imsKernels = createKernel(size,
			numberOfAngles);
		return filter(input, imsKernels);

	}

	private List<RandomAccessibleInterval<T>> createKernel(int size,
		int nAngles)
	{

		// int x1, y1;

		int nB = 3; // ...
		int sizeTemp = size + 2 * nB; // ...

		// create an image with a line
		Img<T> kernel = (Img<T>) ops().create().img(new int[] { sizeTemp,
			sizeTemp });
		// ImagePlus impLine =
		// NewImage.createShortImage("imLine",sizeTemp,sizeTemp,1,NewImage.FILL_BLACK);
		// ImageProcessor ipLine = impLine.getProcessor();
		RandomAccess<T> kernelRA = kernel.randomAccess();

		int x1 = (sizeTemp - 1) / 2;
		for (int y1 = 0; y1 < sizeTemp; y1++) {
			// ipLine.putPixel(x1, y1, 1);
			kernelRA.setPosition(new int[] { x1, y1 });
			kernelRA.get().setReal(1.0f);
		}

		// int iAngle;
		double rotationAngle = 180 / nAngles;

		// ImagePlus impLineRotated =
		// NewImage.createShortImage("imLineRot",sizeTemp, sizeTemp
		// ,1,NewImage.FILL_BLACK);
		// ImageProcessor ipLineRotated = impLineRotated.getProcessor();
		Img<T> kernelRotated = (Img<T>) ops().create().img(new int[] { sizeTemp,
			sizeTemp });
		// create an empty imStack that will contain pointers to rotLineStack
		List<RandomAccessibleInterval<T>> kernels = new ArrayList<>();

		// this is the place where the data is really stored
		short[][] rotLineStack = new short[nAngles][size * size];
//		double tempSizeTemp = sizeTemp;
		// floor instead of ceil because counting starts at 0
		long offset = sizeTemp / 2;
		System.out.println("OFFSET:" + offset + "|sizeTemp:" + sizeTemp);

		IntervalView<T> translatedkernel = Views.translate(kernel, -offset,
			-offset);
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedKernel =
			Views.extendZero(translatedkernel);

		for (int iAngle = 0; iAngle < nAngles; iAngle++) {
			double currentAngle = iAngle * rotationAngle;
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

//			IntervalView<T> rotatedKernel = Views.interval(Views.extendZero(Views.interval(rotated, kernel)), kernel);
			MixedTransformView<T> backtranslated = Views.translate(rotated, offset,
				offset);
			IntervalView<T> rotatedKernel = Views.interval(backtranslated, kernel);
			Cursor<T> rotatedCursor = Views.iterable(rotatedKernel).cursor();
			System.out.println("Begin-Kernel--------------" + iAngle);
			// FIXME kernel size isn't correct, too many iterations
			String values = "";
			int counter = 0;
			while (rotatedCursor.hasNext()) {
				T value = rotatedCursor.next();
				values += value + "|";
				counter++;
				if (counter == sizeTemp) {
					counter = 0;
					System.out.println(values);
					values = "";
				}

			}
			System.out.println("End-Kernel--------------");

			// ipLineRotated.copyBits(ipLine,0,0,Blitter.COPY); // starting from
			// original works better
			// ipLineRotated.rotate(iAngle*rotationAngle);
			//
			// // save values in a 2D array and discard the boundaries
			// int i = 0;
			// for(x1=nB ; x1<sizeTemp-nB ; x1++) {
			// for(y1=nB ; y1<sizeTemp-nB ; y1++) {
			// rotLineStack[iAngle][i] = (short) ipLineRotated.getPixel(x1, y1);
			// i++;
			// }
			// }

			// set pointer in imsKernel to the value stored in rotLineStack
			kernels.add(rotatedKernel);
		}
		// display kernels (just for checking)
		// if (showKernels) {
		// ImagePlus impKernel = new ImagePlus("Kernels", kernels);
		// impKernel.show();
		// }

		return kernels; // bascially a pointer array to 'rotLineStack'
	}

	private RandomAccessibleInterval<T> filter(RandomAccessibleInterval<T> input,
		List<RandomAccessibleInterval<T>> kernels)
	{

		// double[][] im = new double[imageWidth][imageHeight];
		// double[][] imSquare = new double[imageWidth][imageHeight];

		// int sum, sum2, n, x1, y1, x2, y2, x2min, x2max, y2min, y2max/*,
		// kernelSum*/;
		double kernelSum;

		// ipData.resetMinAndMax(); // this is important because "ip.getMin();"
		// returns the smallest displayed(!!) number.
		// int imMin = (int) ipData.getMin();
		long[] mins = new long[2];
		input.min(mins);
		long imageMin = (mins[0] > mins[1] ? mins[1] : mins[0]);
		// subtract the minimum and
		// store square and value of image in integer arrays
		RandomAccess<T> inputRA = input.randomAccess();
		// for (x1 = 0; x1 < imageWidth; x1++) {
		// for (y1 = 0; y1 < imageHeight; y1++) {
		// inputRA.setPosition(new int[] { x1, y1 });
		// im[x1][y1] = inputRA.get().getRealDouble() - imageMin;
		// imSquare[x1][y1] = Math.pow(im[x1][y1], 2);
		//// im[x1][y1]=ipData.getPixel(x1, y1)-imMin; // substraction of the
		// minimum (offset) is necessary for the poisson statistics
		//// imSquare[x1][y1]=im[x1][y1]*im[x1][y1];
		// }
		// }

		Img<T> image = (Img<T>) ops().create().img(input);
		Img<T> imageSqr = (Img<T>) ops().create().img(input);
		RandomAccess<T> imageSqrRA = imageSqr.randomAccess();
		Cursor<T> tempCursor = Views.iterable(image).cursor();
		while (tempCursor.hasNext()) {
			tempCursor.next();
			long[] pos = new long[2];
			tempCursor.localize(pos);
			inputRA.setPosition(pos);
			imageSqrRA.setPosition(pos);
			double value = inputRA.get().getRealDouble() - imageMin;
			tempCursor.get().setReal(value);
			imageSqrRA.get().setReal(Math.pow(value, 2));

		}

		Img<T> value = (Img<T>) ops().create().img(input);
		Img<T> criterion = (Img<T>) ops().create().img(input);
		Img<T> result = (Img<T>) ops().create().img(input);
		Img<T> resultTemp = (Img<T>) ops().create().img(input);
		Img<T> resultCriterion = (Img<T>) ops().create().img(input);
		Img<T> resultCriterionTemp = (Img<T>) ops().create().img(input);

		// setFloatArray(result, 0);
		// setFloatArray(resultCriterion, Float.MAX_VALUE);

		int i = 0;
		// loop through the different line orientations
		for (RandomAccessibleInterval<T> kernel : kernels) {
			// short[] pixelsKernel = (short[]) imsKernels.getPixels(iKernel+1);
			// convolve2(im, imSquare, imSum, imSumOfSquares, pixelsKernel);
			// convolved1 = imSum, convolved2 = imSumOfSquares

			RandomAccessibleInterval<T> convolved1 = ops().filter().convolve(Views
				.interval(Views.extendMirrorDouble(image), image), kernel);
			RandomAccessibleInterval<T> convolved2 = ops().filter().convolve(Views
				.interval(Views.extendMirrorDouble(imageSqr), imageSqr), kernel);
			System.out.println("kernel " + i);
			kernelSum = kernelSum(kernel);
			System.out.println("Sum:" + kernelSum);

			i++;
			// TODO: change parameters for criterion calculation to randomAccess
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
		Img<T> output = (Img<T>) ops().create().img(input);
		putFloat2Image(output, result, imageMin); // add also the minimum back
		// to
		// the
		// image to avoid that the offset
		// shifts between images of a stack
		return output;
		// ipData.resetMinAndMax(); // display the full range.
	}

	// void convolve2(int[][] im1, int[][] im2, int[][] im1Conv, int[][]
	// im2Conv,
	// short[] pixelsKernel)
	// {
	//
	// int x1min, x1max, y1min, y1max;
	// int x2min, x2max, y2min, y2max;
	//
	// x1min = (kernelWidth - 1) / 2;
	// x1max = imageWidth - (kernelWidth - 1) / 2 - 1;
	// y1min = (kernelHeight - 1) / 2;
	// y1max = imageHeight - (kernelHeight - 1) / 2 - 1;
	//
	// int sum1 = 0, sum2 = 0, n = 0, i = 0;
	// double number;
	//
	// for (int x1 = x1min; x1 <= x1max; x1++) {
	// for (int y1 = y1min; y1 <= y1max; y1++) {
	// x2min = x1 - (kernelWidth - 1) / 2;
	// x2max = x1 + (kernelWidth - 1) / 2;
	// y2min = y1 - (kernelHeight - 1) / 2;
	// y2max = y1 + (kernelHeight - 1) / 2;
	// sum1 = 0;
	// sum2 = 0;
	// n = 0;
	// i = 0;
	// for (int y2 = y2min; y2 <= y2max; y2++) {
	// for (int x2 = x2min; x2 <= x2max; x2++) {
	// sum1 += im1[x2][y2] * pixelsKernel[i];
	// sum2 += im2[x2][y2] * pixelsKernel[i];
	// i++;
	// } // y2
	// } // x2
	// im1Conv[x1][y1] = sum1;
	// im2Conv[x1][y1] = sum2;
	// } // y1
	// } // x1
	// }

	// TODO fix arrayIndexOutOfBounds in 2nd kernel ->
	// rotation/interpolation/extension

	private double kernelSum(RandomAccessibleInterval<T> kernel) {
		double kernelSum = 0;
		Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
		int i = 0;
		// while (kernelCursor.hasNext()) {
		for (i = 0; i < kernelWidth * kernelHeight; i++) {
//			System.out.println(i);
			T value = kernelCursor.next();
			kernelSum += value.getRealDouble();
			// System.out.println(i);
			// i++;
		}
		return (kernelSum);
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

	private void setFloatArray(double[][] array, float val) {
		for (int x1 = 0; x1 < imageWidth; x1++) {
			for (int y1 = 0; y1 < imageHeight; y1++) {
				array[x1][y1] = val;
			}
		}
	}

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
