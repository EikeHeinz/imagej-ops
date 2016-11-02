package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;
import net.imagej.ops.OpMethod;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Plugin;


@Plugin(type = Namespace.class)
public class PixelFeatureNamespace extends AbstractNamespace {

	@Override
	public String getName() {
		return "pixelfeatures";
	}

	// -- difference of gaussian --
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.DoGPixelFeature.class)
	public <T extends RealType<T>> CompositeIntervalView<T, RealComposite<T>> doG(
		final RandomAccessibleInterval<T> in, final double minSigma,
		final double maxSigma)
	{
		@SuppressWarnings("unchecked")
		final CompositeIntervalView<T, RealComposite<T>> result =
			(CompositeIntervalView<T, RealComposite<T>>) ops().run(
		net.imagej.ops.features.pixelfeatures.DoGPixelFeature.class, in,
				minSigma, maxSigma);
		return result;
	}

	// -- gaussian --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.GaussPixelFeature.class)
	public <T extends RealType<T>> CompositeIntervalView<T, RealComposite<T>> gaussian(
	final RandomAccessibleInterval<T> in, final double minSigma,
		final double maxSigma)
	{
		@SuppressWarnings("unchecked")
       final CompositeIntervalView<T, RealComposite<T>> result =
			(CompositeIntervalView<T, RealComposite<T>>) ops().run(
				net.imagej.ops.features.pixelfeatures.GaussPixelFeature.class, in,
				minSigma, maxSigma);
			return result;
		}

		// -- gaussian gradient magnitude --
		
		@OpMethod(
			op = net.imagej.ops.features.pixelfeatures.GaussianGradientMagnitudePixelFeature.class)
		public <T extends RealType<T>> RandomAccessibleInterval<T>
			gaussianGradientMagnitude(final RandomAccessibleInterval<T> in,
				final double sigma)
		{
			@SuppressWarnings("unchecked")
			final RandomAccessibleInterval<T> result =
				(RandomAccessibleInterval<T>) ops().run(
					net.imagej.ops.features.pixelfeatures.GaussianGradientMagnitudePixelFeature.class,
					in, sigma);
			return result;
		}

	  //-- hessian --
		
		@OpMethod(
			op = net.imagej.ops.features.pixelfeatures.HessianPixelFeature.class)
		public <T extends RealType<T>> CompositeIntervalView<T, RealComposite<T>> hessian(
			final RandomAccessibleInterval<T> in, final double minSigma, final double maxSigma)
		{
			@SuppressWarnings("unchecked")
			final CompositeIntervalView<T, RealComposite<T>> result =
				(CompositeIntervalView<T, RealComposite<T>>) ops().run(
					net.imagej.ops.features.pixelfeatures.HessianPixelFeature.class, in, minSigma, maxSigma);
			return result;
		}

		//-- laplacian of gaussian --
		
		@OpMethod(op = net.imagej.ops.features.pixelfeatures.LoGPixelFeature.class)
		public <T extends RealType<T>> RandomAccessibleInterval<T> loG(
			final RandomAccessibleInterval<T> in, final double sigma)
		{
			@SuppressWarnings("unchecked")
			final RandomAccessibleInterval<T> result =
				(RandomAccessibleInterval<T>) ops().run(
					net.imagej.ops.features.pixelfeatures.LoGPixelFeature.class, in, sigma);
			return result;
		}

		//-- max --
		
		@OpMethod(op = net.imagej.ops.features.pixelfeatures.MaxPixelFeature.class)
		public <T extends RealType<T>> RandomAccessibleInterval<T> max(
			final RandomAccessibleInterval<T> in, final int span)
		{
			@SuppressWarnings("unchecked")
			final RandomAccessibleInterval<T> result =
				(RandomAccessibleInterval<T>) ops().run(
					net.imagej.ops.features.pixelfeatures.MaxPixelFeature.class, in, span);
			return result;
		}
		
		//-- mean --

		@OpMethod(op = net.imagej.ops.features.pixelfeatures.MeanPixelFeature.class)
		public <T extends RealType<T>> RandomAccessibleInterval<T> mean(
			final RandomAccessibleInterval<T> in, final int span)
		{
			@SuppressWarnings("unchecked")
			final RandomAccessibleInterval<T> result =
				(RandomAccessibleInterval<T>) ops().run(
					net.imagej.ops.features.pixelfeatures.MeanPixelFeature.class, in, span);
			return result;
		}
		
		//-- min --

		@OpMethod(op = net.imagej.ops.features.pixelfeatures.MinPixelFeature.class)
		public <T extends RealType<T>> RandomAccessibleInterval<T> min(
			final RandomAccessibleInterval<T> in, final int span)
		{
			@SuppressWarnings("unchecked")
			final RandomAccessibleInterval<T> result =
				(RandomAccessibleInterval<T>) ops().run(
					net.imagej.ops.features.pixelfeatures.MinPixelFeature.class, in, span);
			return result;
		}

}
