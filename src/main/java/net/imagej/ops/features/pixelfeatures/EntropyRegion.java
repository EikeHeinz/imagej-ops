package net.imagej.ops.features.pixelfeatures;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.IterableInterval;
import net.imglib2.histogram.BinMapper1d;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.numeric.RealType;

@Plugin(type = Ops.Pixelfeatures.EntropyRegionFeature.class)
public class EntropyRegion<T extends RealType<T>> extends AbstractUnaryComputerOp<IterableInterval<T>, T> implements Ops.Pixelfeatures.EntropyRegionFeature{

	@Parameter
	private int binSize;
	
	@Override
	public void compute(IterableInterval<T> input, T output) {
		BinMapper1d<T> binMapper = new Real1dBinMapper<>(0, 255, binSize, false);
		Histogram1d<T> histogram = new Histogram1d<>(binMapper);
		histogram.addData(input);
		double totalCount = 0;
		for (int i = 0; i < histogram.getBinCount(); i++) {
			totalCount += histogram.frequency(i);
		}

		double entropy = 0;
		for (int i = 0; i < histogram.getBinCount(); i++) {
			if (histogram.frequency(i) > 0) {
				double probability = histogram.frequency(i) / totalCount;
				entropy += -probability * Math.log(probability) / Math.log(2.0);
			}
		}
		output.setReal(entropy);
		// TODO Auto-generated method stub
		
	}

}
