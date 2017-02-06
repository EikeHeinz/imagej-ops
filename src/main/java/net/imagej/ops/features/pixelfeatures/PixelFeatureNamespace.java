
package net.imagej.ops.features.pixelfeatures;

import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;
import net.imagej.ops.OpMethod;
import net.imagej.ops.features.pixelfeatures.LinearKuwaharaPixelFeature.KuwaharaCriterionMethod;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Plugin;

@Plugin(type = Namespace.class)
public class PixelFeatureNamespace extends AbstractNamespace {

	@Override
	public String getName() {
		return "pixelfeatures";
	}

	// -- bilateral --
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.BilateralPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> bilateral(final RandomAccessibleInterval<T> in) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.BilateralPixelFeature.class, in);
		return result;
	}

	// -- difference of gaussian --
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.DoGPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> doG(final RandomAccessibleInterval<T> in,
			final double minSigma, final double maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.DoGPixelFeature.class, in, minSigma, maxSigma);
		return result;
	}
	
	// -- entropy --
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.EntropyPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> entropy(final RandomAccessibleInterval<T> in,
			final double minSigma, final double maxSigma, final int radius) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.EntropyPixelFeature.class, in, minSigma, maxSigma, radius);
		return result;
	}
	
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.EntropyRegion.class)
	public <T extends RealType<T>> RealType<T> entropyRegion(final RealType<T> out, final IterableInterval<T> in, final int binSize) {
		final RealType<T> result =
			(RealType<T>) ops().run(net.imagej.ops.features.pixelfeatures.EntropyRegion.class, out, in, binSize);
		return result;
	}

	// -- gaussian --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.GaussPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> gaussian(final RandomAccessibleInterval<T> in,
			final double minSigma, final double maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.GaussPixelFeature.class, in, minSigma, maxSigma);
		return result;
	}

	// -- hessian --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.HessianPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> hessian(final RandomAccessibleInterval<T> in,
			final double minSigma, final double maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.HessianPixelFeature.class, in, minSigma, maxSigma);
		return result;
	}

	// -- kuwahara

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.KuwaharaPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> kuwahara(final RandomAccessibleInterval<T> in, final int maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.KuwaharaPixelFeature.class, in, maxSigma);
		return result;
	}

	// -- laplacian of gaussian --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.LoGPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> loG(final RandomAccessibleInterval<T> in,
			final double minSigma, final double maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.LoGPixelFeature.class, in, minSigma, maxSigma);
		return result;
	}

	// -- max --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.RectangleMaxPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> max(final RandomAccessibleInterval<T> in,
			final int span) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.RectangleMaxPixelFeature.class, in, span);
		return result;
	}

	// -- mean --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.RectangleMeanPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> mean(final RandomAccessibleInterval<T> in,
			final int span) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.RectangleMeanPixelFeature.class, in, span);
		return result;
	}

	// -- median --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.RectangleMedianPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> median(final RandomAccessibleInterval<T> in,
			final int span) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.RectangleMedianPixelFeature.class, in, span);
		return result;
	}

	// -- membrane projections --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.MembraneProjection.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> membraneProjections(final RandomAccessibleInterval<T> in,
			final int membraneSize, final int patchSize) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.MembraneProjection.class, in, membraneSize, patchSize);
		return result;
	}

	// -- min --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.RectangleMinPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> min(final RandomAccessibleInterval<T> in,
			final int span) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.RectangleMinPixelFeature.class, in, span);
		return result;
	}

	// -- neighbors --
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.NeighborsPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> neighbors(final RandomAccessibleInterval<T> in,
			final int minSigma, final int maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.NeighborsPixelFeature.class, in, minSigma, maxSigma);
		return result;
	}
	
	// -- sobel feature --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.SobelPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> sobel(
			final RandomAccessibleInterval<T> in, final double minSigma, final double maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops().run(
				net.imagej.ops.features.pixelfeatures.SobelPixelFeature.class, in, minSigma,
				maxSigma);
		return result;
	}
	
	// -- manual Sobel --
	@OpMethod(op = net.imagej.ops.features.pixelfeatures.Sobel.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> manualSobel(final RandomAccessibleInterval<T> in) {
		final RandomAccessibleInterval<T> result =
			(RandomAccessibleInterval<T>) ops().run(net.imagej.ops.features.pixelfeatures.Sobel.class, in);
		return result;
	}

	// -- structure tensor --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.StructureTensorEigenvaluesPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> structureTensor(final RandomAccessibleInterval<T> in,
			final double minSigma, final double maxSigma) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.StructureTensorEigenvaluesPixelFeature.class, in, minSigma, maxSigma);
		return result;
	}

	// -- variance --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.RectangleVariancePixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> variance(final RandomAccessibleInterval<T> in,
			final int span) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.RectangleVariancePixelFeature.class, in, span);
		return result;
	}

	// -- Lipschitz --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.LipschitzPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> lipschitzFeature(final RandomAccessibleInterval<T> in) {
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops()
				.run(net.imagej.ops.features.pixelfeatures.LipschitzPixelFeature.class, in);
		return result;
	}

	// -- linear kuwahara --

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.LinearKuwaharaPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> linearKuwaharaFilter(
			final RandomAccessibleInterval<T> in, final int kernelSize, final int numberOfAngles) {
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops().run(
				net.imagej.ops.features.pixelfeatures.LinearKuwaharaPixelFeature.class, in, kernelSize, numberOfAngles);
		return result;
	}

	@OpMethod(op = net.imagej.ops.features.pixelfeatures.LinearKuwaharaPixelFeature.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> linearKuwaharaFilter(
			final RandomAccessibleInterval<T> in, final int kernelSize, final int numberOfAngles,
			final KuwaharaCriterionMethod criterionMethod) {
		final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>) ops().run(
				net.imagej.ops.features.pixelfeatures.LinearKuwaharaPixelFeature.class, in, kernelSize, numberOfAngles,
				criterionMethod);
		return result;
	}
}
