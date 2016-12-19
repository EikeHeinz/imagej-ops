package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.MembraneProjections;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Plugin;

import Jama.Matrix;

@Plugin(type = Ops.Pixelfeatures.MembraneProjections.class)
public class MembraneProjection<T extends RealType<T>> extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>> implements MembraneProjections{

	
	@Override
	public void initialize() {
		RandomAccessibleInterval<T> kernel = (RandomAccessibleInterval<T>) ops().create().img(new int[] {19,19});
		Cursor<T> kernelCursor = Views.iterable(kernel).cursor();
		int counter = 0;
		String values = "";
		while(kernelCursor.hasNext()) {
			T value = kernelCursor.next();
			if(counter == 9) {
				value.setOne();
			} else {
				value.setZero();
			}
			values += value +"|";
			counter++;
			if(counter == 19) {
				counter = 0;
				System.out.println(values);
				values = "";
			}
		}
		List<RandomAccessibleInterval<T>> kernels = new ArrayList<>();
		kernels.add(kernel);
		
		for (int i = 1; i <= 30; i++) {
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

			AffineRandomAccessible<T, AffineGet> rotated = RealViews
					.affine(Views.interpolate(kernel, new NLinearInterpolatorFactory<>()), rotationTransform);

			IntervalView<T> rotatedKernel = Views.interval(Views.extendZero(Views.interval(rotated, kernel)), kernel);
			System.out.println("Begin-Kernel--------------" + i);
			Cursor<T> rotatedCursor = Views.iterable(rotatedKernel).cursor();
			values = "";
			counter = 0;
			while (rotatedCursor.hasNext()) {
//				System.out.println(rotatedCursor.next().getRealDouble());
				T value = rotatedCursor.next();
				values += value +"|";
				counter++;
				if(counter == 19) {
					counter = 0;
					System.out.println(values);
					values = "";
				}
			}
			System.out.println("End-Kernel--------------");
		}
		
	}
	@Override
	public CompositeIntervalView<T, RealComposite<T>> calculate(RandomAccessibleInterval<T> input) {
		
		return null;
	}



}
