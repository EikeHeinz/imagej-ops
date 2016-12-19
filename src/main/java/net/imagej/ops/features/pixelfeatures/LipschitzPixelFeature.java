package net.imagej.ops.features.pixelfeatures;

import java.util.ArrayList;
import java.util.List;

import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Pixelfeatures.LipschitzPixFeature;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

import org.scijava.plugin.Plugin;

@Plugin(type = Ops.Pixelfeatures.LipschitzPixFeature.class)
public class LipschitzPixelFeature<T extends RealType<T>>
		extends AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>>
		implements LipschitzPixFeature {

	private boolean m_Down = true; //

	private boolean m_TopHat = true; // lower Lipschitz cover

	private double m_Slope = 5; // slope

	@Override
	public CompositeIntervalView<T, RealComposite<T>> calculate(RandomAccessibleInterval<T> input) {
		// m_stack = m_imp.getStack();
		// m_scount = m_stack.getSize();
		// m_roi = ip.getRoi();
		ImageHeight = (int) input.dimension(0);
		ImageWidth = (int) input.dimension(1);
		// m_channels = ip instanceof ColorProcessor ? 3 : 1;
		// m_short = ip instanceof ShortProcessor;
		// pixel = new int[m_channels];

		// Get channel(s) to process
		// ImagePlus[] channels = extractChannels(originalImage);

		// ImagePlus[] results = new ImagePlus[ channels.length ];

		// for(int ch=0; ch < channels.length; ch++)
		// {
		// ImageProcessor result = channels[ ch
		// ].getProcessor().duplicate().convertToByte(true);
		List<RandomAccessibleInterval<T>> filtered = new ArrayList<>();
		for (int slope = 5; slope <= 25; slope += 5) {
			m_Slope = slope;
			filtered.add(Lipschitz2D(Views.interval(Views.extendMirrorDouble(input), input)));
		}

		return Views.collapseReal(Views.stack(filtered));
	}





	static int ImageHeight;
	static int ImageWidth;
	static int m_channels = 1;

	// TODO verify results
	// TODO change calculation to fit image size

	private RandomAccessibleInterval<T> Lipschitz2D(RandomAccessibleInterval<T> input) {
		float slope, slope1, p, p1, p2, p3, p4, maxz;

//		int[][] destPixels = new int[m_channels][ImageHeight * ImageWidth];
//		int[][] srcPixels = new int[m_channels][ImageHeight * ImageWidth];
//		byte[][] tmpBytePixels = new byte[m_channels][ImageHeight * ImageWidth];
//		short[][] tmpShortPixels = new short[m_channels][ImageHeight * ImageWidth];

		RandomAccessibleInterval<T> tmpPixelsOps = (RandomAccessibleInterval<T>) ops().create().img(input);
		RandomAccessibleInterval<T> srcPixelsOps = (RandomAccessibleInterval<T>) ops().create().img(input);
		RandomAccessibleInterval<T> destPixelsOps = (RandomAccessibleInterval<T>) ops().create().img(input);
		RandomAccess<T> srcPixelsOpsRA = srcPixelsOps.randomAccess();
		RandomAccess<T> destPixelsOpsRA = destPixelsOps.randomAccess();
		Cursor<T> tmpPixelsOpsCursor = Views.iterable(tmpPixelsOps).cursor();
		Cursor<T> inputCursor = Views.iterable(input).cursor();

		// if (m_channels == 1) {
		// if (m_short)
		// {
		// tmpShortPixels[0] = (short []) ip.getPixels();
		// }
		// else
		// {
		// tmpBytePixels[0] = (byte[]) ip.getPixels();
		// }

		// }
		// else
		// {
		// ColorProcessor cip = (ColorProcessor) ip;
		// cip.getRGB(tmpBytePixels[0], tmpBytePixels[1], tmpBytePixels[2]);
		// }

		int sign = (m_Down ? 1 : -1);
		int topdown = (m_Down ? 0 : 255);
		for (int ii = 0; ii < m_channels; ii++) {
			while (inputCursor.hasNext()) {
				T value = inputCursor.next();
				long[] position = new long[2];
				srcPixelsOpsRA.setPosition(position);
				destPixelsOpsRA.setPosition(position);
				int tmpValue = sign * ((int) value.getRealFloat() & 0xff);
				srcPixelsOpsRA.get().setReal(tmpValue);
				destPixelsOpsRA.get().setReal(tmpValue);
			}
			// for (int ij=0; ij< ImageHeight * ImageWidth; ij++)
			// {
			// srcPixels[ii][ij] = (m_short? sign *(tmpShortPixels[ii][ij] &
			// 0xffff):sign *(tmpBytePixels[ii][ij] & 0xff));
			// destPixels[ii][ij] = srcPixels[ii][ij];
			//
			// }
		}

		slope = (int) (m_Slope);
		slope1 = (int) (slope * Math.sqrt(2.0));
		maxz = m_channels;

		// for (int y = m_roi.y; y < m_roi.y + m_roi.height; y++) // rows
		for (int y = 0; y < ImageHeight; y++) {
			// IJ.showProgress(y , 2 * ImageHeight);
			for (int z = 0; z < m_channels; z++) {
				p2 = sign * (topdown + (sign) * slope);
				p3 = sign * (topdown + (sign) * slope1);
				// for (int x = m_roi.x; x < m_roi.x + m_roi.width; x++) //
				// columns
				for (int x = 0; x < ImageWidth; x++) {
					p = (p2 - slope);
					p1 = (p3 - slope1);
					if (p1 > p)
						p = p1;
					// destPixelsOpsRA.setPosition(new int[] { z, x + ImageWidth
					// * (Math.max(y - 1, 0)) });
					int pos = x + ImageWidth * (Math.max(y - 1, 0));
					int realPosX = Math.floorDiv(pos, ImageWidth);
					int realPosY = pos % ImageWidth;
					System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
					destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
					p3 = destPixelsOpsRA.get().getRealFloat();
					// p3 = destPixels[z][x + ImageWidth * (Math.max(y - 1,
					// 0))];
					p1 = p3 - slope;
					if (p1 > p)
						p = p1;

//					destPixelsOpsRA.setPosition(
//							new int[] { z, Math.min(x + 1, ImageWidth - 1) + ImageWidth * (Math.max(y - 1, 0)) });
//					destPixelsOpsRA.setPosition(
//							new int[] { Math.min(x + 1, ImageWidth - 1) , ImageWidth * (Math.max(y - 1, 0)) });
					pos = Math.min(x + 1, ImageWidth - 1) + ImageWidth * (Math.max(y - 1, 0));
					realPosX = Math.floorDiv(pos, ImageWidth);
					realPosY = pos % ImageWidth;
					System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
					destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
					p4 = destPixelsOpsRA.get().getRealFloat();
					// p4 = destPixels[z][Math.min(x + 1, ImageWidth - 1) +
					// ImageWidth * (Math.max(y - 1, 0))];
					p1 = p4 - slope1;
					if (p1 > p)
						p = p1;

//					srcPixelsOpsRA.setPosition(new int[] { z, x + ImageWidth * y });
					srcPixelsOpsRA.setPosition(new int[] { x , ImageWidth * y });
					pos = x + ImageWidth * y;
					realPosX = Math.floorDiv(pos, ImageWidth);
					realPosY = pos % ImageWidth;
					System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
					srcPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
					p2 = srcPixelsOpsRA.get().getRealFloat();
					// p2 = srcPixels[z][x + ImageWidth * y];
					if (p > p2) {
//						destPixelsOpsRA.setPosition(new int[] { z, x + ImageWidth * y });
						destPixelsOpsRA.setPosition(new int[] { x , ImageWidth * y });
						pos = x + ImageWidth * y;
						realPosX = Math.floorDiv(pos, ImageWidth);
						realPosY = pos % ImageWidth;
						System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
						destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
						destPixelsOpsRA.get().setReal(p);
						// destPixels[z][x + ImageWidth * y] = p;
						p2 = p;
					}
				}
			}
		}

		System.out.println("begin 2nd loop");
		// for (int y = m_roi.y + m_roi.height - 1; y >= m_roi.y; y--) // rows
		for (int y = ImageHeight - 1; y >= 0; y--) {
			// IJ.showProgress(2 * ImageHeight - y - 1, 2 * ImageHeight);
			for (int z = 0; z < maxz; z++) {
				p2 = sign * (topdown + (sign) * slope);
				p3 = sign * (topdown + (sign) * slope1);
				// for (int x = m_roi.x + m_roi.width - 1; x >= m_roi.x; x--) //
				// columns
				for (int x = ImageWidth - 1; x >= 0; x--) {
					p = (p2 - slope);
					p1 = (p3 - slope1);
					if (p1 > p)
						p = p1;

//					destPixelsOpsRA.setPosition(new int[] { z, x + ImageWidth * (Math.min(y + 1, ImageHeight - 1)) });
					int pos = x + ImageWidth * (Math.min(y + 1, ImageHeight - 1));
					int realPosX = Math.floorDiv(pos, ImageWidth);
					int realPosY = pos % ImageWidth;
					System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
					destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
					p3 = destPixelsOpsRA.get().getRealFloat();
					// p3 = destPixels[z][x + ImageWidth * (Math.min(y + 1,
					// ImageHeight - 1))];
					p1 = p3 - slope;
					if (p1 > p)
						p = p1;

//					destPixelsOpsRA.setPosition(
//							new int[] { z, Math.max(x - 1, 0) + ImageWidth * (Math.min(y + 1, ImageHeight - 1)) });
					pos = Math.max(x - 1, 0) + ImageWidth * (Math.min(y + 1, ImageHeight - 1));
					realPosX = Math.floorDiv(pos, ImageWidth);
					realPosY = pos % ImageWidth;
					System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
					destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
					p4 = destPixelsOpsRA.get().getRealFloat();
					// p4 = destPixels[z][Math.max(x - 1, 0) + ImageWidth *
					// (Math.min(y + 1, ImageHeight - 1))];
					p1 = p4 - slope1;
					if (p1 > p)
						p = p1;

//					destPixelsOpsRA.setPosition(new int[] { z, x + ImageWidth * y });
					pos = x + ImageWidth * y;
					realPosX = Math.floorDiv(pos, ImageWidth);
					realPosY = pos % ImageWidth;
					System.out.println("pos:"+pos+"|realX:"+realPosX+"|"+"realY:"+realPosY);
					destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
					p2 = destPixelsOpsRA.get().getRealFloat();
					// p2 = destPixels[z][x + ImageWidth * y];
					if (p > p2) {
//						destPixelsOpsRA.setPosition(new int[] { z, x + ImageWidth * y });
						pos = x + ImageWidth * y;
						realPosX = Math.floorDiv(pos, ImageWidth);
						realPosY = pos % ImageWidth;
						destPixelsOpsRA.setPosition(new int[] { realPosX, realPosY });
						destPixelsOpsRA.get().setReal(p);
						// destPixels[z][x + ImageWidth * y] = p;
						p2 = p;
					}
				}
			}
		}

		for (int ii = 0; ii < m_channels; ii++) {
			// for (int ij = 0; ij < ImageHeight * ImageWidth; ij++) {
			while (tmpPixelsOpsCursor.hasNext()) {
				tmpPixelsOpsCursor.next();
				srcPixelsOpsRA.setPosition(tmpPixelsOpsCursor);
				destPixelsOpsRA.setPosition(tmpPixelsOpsCursor);
				if (m_TopHat) {
					byte value = (m_Down
							? (byte) (srcPixelsOpsRA.get().getRealFloat() - destPixelsOpsRA.get().getRealFloat() + 255)
							: (byte) (destPixelsOpsRA.get().getRealFloat() - srcPixelsOpsRA.get().getRealFloat()));
					tmpPixelsOpsCursor.get().setReal(value);
					// tmpBytePixels[ii][ij] = (m_Down ? (byte)
					// (srcPixels[ii][ij] - destPixels[ii][ij] + 255)
					// : (byte) (destPixels[ii][ij] - srcPixels[ii][ij]));
				} else {
					// if (m_short) {
					// tmpShortPixels[ii][ij] = (short) ((sign *
					// destPixels[ii][ij] & 0xffff));
					// } else {
					tmpPixelsOpsCursor.get().setReal((byte) (sign * destPixelsOpsRA.get().getRealFloat()));
					// tmpBytePixels[ii][ij] = (byte) (sign *
					// destPixels[ii][ij]);
					// }
				}
			}
		}

		// if (m_channels == 1) {
		// if (m_short) {
		// ShortProcessor sip = (ShortProcessor) ip;
		// sip.setPixels(tmpShortPixels[0]);
		// } else {
		// ByteProcessor bip = (ByteProcessor) ip;
		// bip.setPixels(tmpBytePixels[0]);
		// }
		//
		// }
		// else
		// {
		// ColorProcessor cip = (ColorProcessor) ip;
		// cip.setRGB(tmpBytePixels[0],tmpBytePixels[1],tmpBytePixels[2]);
		// }

		return tmpPixelsOps;

	}

	// public void runLipschitz(RandomAccessibleInterval<T> input) {
	// if (IJ.escapePressed()) return;
	// breaked = false;
	// Date d1, d2;
	// d1 = new Date();

	// IJ.showStatus("Initializing...");
	// List<RandomAccessibleInterval<T>> outList = new ArrayList<>();
	// m_stack_out = m_imp.createEmptyStack();
	// ImagePlus imp2 = null;

	// for (int i = 0; i < m_scount; i++) {
	// if (m_scount > 1) {
	// ip = m_stack.getProcessor(i + 1);
	// }
	// iptmp = ip.createProcessor(ImageWidth, ImageHeight);
	// RandomAccessibleInterval<T> tmp = (RandomAccessibleInterval<T>)
	// ops().create().img(input);
	// ops().copy().img((Img<T>) tmp, (Img<T>) input);
	// iptmp.copyBits(ip, 0, 0, Blitter.COPY);
	//
	// IJ.showStatus("Filtering " + (i + 1) + "/" + m_scount + " slice.");
	//
	// Lipschitz2D(iptmp);
	//
	// m_stack_out.addSlice(m_imp.getShortTitle() + " " + (i + 1) + "/" +
	// m_scount, iptmp);
	//
	// if (breaked = IJ.escapePressed())
	// IJ.beep();
	// }

	// return list -> compositeview
	// imp2 = new ImagePlus(m_imp.getShortTitle()+" Filtered (Lipschitz)
	// Slope:"+m_Slope+" "+((m_Down)?" -Down":" ")+" "+((m_TopHat)?"
	// -TopHat":" ")+((breaked)?" -INTERUPTED":""), m_stack_out);
	// imp2.show();
	// imp2.updateAndDraw();
	// IJ.showProgress(1.0);

	// } // end of 'runLipschitz' method

}
