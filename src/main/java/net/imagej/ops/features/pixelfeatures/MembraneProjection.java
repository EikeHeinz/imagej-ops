
package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.MembraneProjections;
import net.imagej.ops.special.chain.RAIs;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
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
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Plugin;

import Jama.Matrix;

@Plugin(type = Ops.Pixelfeatures.MembraneProjections.class)
public class MembraneProjection<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>>
	implements MembraneProjections
{

	private UnaryComputerOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>[] convolveOps;
	private UnaryFunctionOp<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;
	private UnaryFunctionOp<RealComposite, RealType> medianOp;
	private UnaryFunctionOp<RealComposite, RealType> stdDevOp;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernel = (RandomAccessibleInterval<T>) ops()
			.create().img(new int[] { 19, 19 });
		Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
		int counter = 0;
		String values = "";
		while (kernelCursor.hasNext()) {
			T value = kernelCursor.next();
			if (counter == 9) {
				value.setOne();
			}
			else {
				value.setZero();
			}
			values += value + "|";
			counter++;
			if (counter == 19) {
				counter = 0;
				System.out.println(values);
				values = "";
			}
		}
//		List<RandomAccessibleInterval<T>> kernels = new ArrayList<>();
		RandomAccessibleInterval<T>[] kernels = new RandomAccessibleInterval[30];
		kernels[0] = kernel;
		IntervalView<T> translatedkernel = Views.translate(kernel, -9, -9);
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> extendedKernel =
			Views.extendZero(translatedkernel);

		for (int i = 1; i < 30; i++) {
			double currentAngle = i * 6;
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

			MixedTransformView<T> backtranslated = Views.translate(rotated, 9, 9);
			IntervalView<T> rotatedKernel = Views.interval(backtranslated, kernel);
			kernels[i] = rotatedKernel;

			// DEBUG STUFF ------------------
//			System.out.println("Begin-Kernel--------------" + i);
//			Cursor<T> rotatedCursor = Views.iterable(rotatedKernel).cursor();
//			values = "";
//			counter = 0;
//			while (rotatedCursor.hasNext()) {
//				T value = rotatedCursor.next();
//				values += value +"|";
//				counter++;
//				if(counter == 19) {
//					counter = 0;
//					System.out.println(values);
//					values = "";
//				}
//			}
//			System.out.println("End-Kernel--------------");
			// END DEBUG STUFF --------------
		}
		int size = 30;
		convolveOps = new UnaryComputerOp[size];
		for (int i = 0; i < kernels.length; i++) {
			// FIXME
			convolveOps[i] = RAIs.computer(ops(), Ops.Filter.Convolve.class, in(),
				new Object[] { kernels[i] });
		}
		createRAI = RAIs.function(ops(), Ops.Create.Img.class, in());
		medianOp = Functions.unary(ops(), Ops.Stats.Median.class, RealType.class,
			RealComposite.class);
		stdDevOp = Functions.unary(ops(), Ops.Stats.StdDev.class, RealType.class,
			RealComposite.class);

	}

	@Override
	public CompositeIntervalView<T, RealComposite<T>> calculate(
		RandomAccessibleInterval<T> input)
	{
		List<RandomAccessibleInterval<T>> convolvedImgs = new ArrayList<>();
		for (int i = 0; i < convolveOps.length; i++) {
			RandomAccessibleInterval<T> temp = createRAI.calculate(input);
			convolveOps[i].compute(Views.interval(Views.extendMirrorDouble(input),
				input), temp);
			convolvedImgs.add(temp);
		}

		RandomAccess<T>[] outImgsRAs = new RandomAccess[6];
		List<RandomAccessibleInterval<T>> outImgs = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			RandomAccessibleInterval<T> tmp = createRAI.calculate(input);
			outImgsRAs[i] = tmp.randomAccess();
			outImgs.add(tmp);
		}
		CompositeIntervalView<T, RealComposite<T>> compositeConvolved = Views
			.collapseReal(Views.stack(convolvedImgs));
		Cursor<RealComposite<T>> compositeCursor = Views.iterable(
			compositeConvolved).cursor();
		while (compositeCursor.hasNext()) {
			RealComposite<T> composite = compositeCursor.next();
			double sumOfPixels, mean, stddev, median, max, min;
			sumOfPixels = 0.0d;
			min = Double.MAX_VALUE;
			max = -Double.MAX_VALUE;
			double[] values = new double[30];
			int i = 0;
			for (T value : composite) {
				values[i] = value.getRealDouble();
				sumOfPixels += values[i];
				i++;
			}
			Arrays.sort(values);
			min = values[0];
			max = values[values.length - 1];
			mean = sumOfPixels / 30;
			ops().stats().stdDev(composite);
//			median = medianOp.calculate(composite);
			double[] outValues = new double[6];
			outValues[0] = sumOfPixels;
			outValues[1] = mean;
//			outValues[2] = stddev;
			outValues[3] = medianOp.calculate(composite).getRealDouble();
			System.out.println("median:" + outValues[3]);
			outValues[4] = max;
			outValues[5] = min;
		}
		return null;
	}

}
