
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
public class LipschitzPixelFeature<T extends RealType<T>> extends
	AbstractUnaryFunctionOp<RandomAccessibleInterval<T>, CompositeIntervalView<T, RealComposite<T>>>
	implements LipschitzPixFeature
{

	private boolean m_Down = true; //

	private boolean m_TopHat = true; // lower Lipschitz cover

	private double m_Slope = 5; // slope

	@Override
	public CompositeIntervalView<T, RealComposite<T>> calculate(
		RandomAccessibleInterval<T> input)
	{

		ImageHeight = (int) input.dimension(0);
		ImageWidth = (int) input.dimension(1);

		List<RandomAccessibleInterval<T>> filtered = new ArrayList<>();
		for (int slope = 5; slope <= 5; slope += 5) {
			m_Slope = slope;
			filtered.add(Lipschitz2D(Views.interval(Views.extendMirrorDouble(input),
				input)));
		}

		return Views.collapseReal(Views.stack(filtered));
	}

	static int ImageHeight;
	static int ImageWidth;
	static int m_channels = 1;

	// TODO verify results

	private RandomAccessibleInterval<T> Lipschitz2D(
		RandomAccessibleInterval<T> input)
	{
		float slope, slope1, p, p1, p2, p3, p4, maxz;

		RandomAccessibleInterval<T> tmpPixelsOps =
			(RandomAccessibleInterval<T>) ops().create().img(input);
		RandomAccessibleInterval<T> srcPixelsOps =
			(RandomAccessibleInterval<T>) ops().create().img(input);
		RandomAccessibleInterval<T> destPixelsOps =
			(RandomAccessibleInterval<T>) ops().create().img(input);
		RandomAccess<T> srcPixelsOpsRA = srcPixelsOps.randomAccess();
		RandomAccess<T> destPixelsOpsRA = destPixelsOps.randomAccess();
		Cursor<T> tmpPixelsOpsCursor = Views.iterable(tmpPixelsOps).cursor();
		Cursor<T> inputCursor = Views.iterable(input).cursor();

		int sign = (m_Down ? 1 : -1);
		int topdown = (m_Down ? 0 : 255);
		for (int ii = 0; ii < m_channels; ii++) {
			while (inputCursor.hasNext()) {
				T value = inputCursor.next();
				srcPixelsOpsRA.setPosition(inputCursor);
				destPixelsOpsRA.setPosition(inputCursor);
				float tmpValue = sign * (value.getRealFloat());
				srcPixelsOpsRA.get().setReal(tmpValue);
				destPixelsOpsRA.get().setReal(tmpValue);
			}

		}

		slope = (int) (m_Slope);
		slope1 = (int) (slope * Math.sqrt(2.0));
		maxz = m_channels;

		// top -> bottom
		for (int y = 0; y < ImageHeight; y++) { // iterating through rows

			for (int z = 0; z < m_channels; z++) {
				p2 = sign * (topdown + (sign) * slope);
				p3 = sign * (topdown + (sign) * slope1);

				for (int x = 0; x < ImageWidth; x++) { // iterating through columns

					p = (p2 - slope);
					p1 = (p3 - slope1);

					if (p1 > p) p = p1;

					int xPos = x;
					int yPos = (y == 0) ? 0 : y - 1;

					destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
					p3 = destPixelsOpsRA.get().getRealFloat();

					p1 = p3 - slope;

					if (p1 > p) p = p1;

					xPos = (x + 1 == ImageWidth) ? x : x + 1;
					yPos = (y == 0) ? 0 : y - 1;
					destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
					p4 = destPixelsOpsRA.get().getRealFloat();

					p1 = p4 - slope1;
					if (p1 > p) p = p1;

					xPos = x;
					yPos = y;
					srcPixelsOpsRA.setPosition(new int[] { xPos, yPos });
					p2 = srcPixelsOpsRA.get().getRealFloat();

					// is never accessed (with current test img)
					if (p > p2) {
						xPos = x;
						yPos = y;
						destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
						destPixelsOpsRA.get().setReal(p);
						p2 = p;
					}
				}
			}
		}

		System.out.println("begin 2nd loop");
		// bottom -> top
		for (int y = ImageHeight - 1; y >= 0; y--) { // rows

			for (int z = 0; z < maxz; z++) {
				p2 = sign * (topdown + (sign) * slope);
				p3 = sign * (topdown + (sign) * slope1);

				for (int x = ImageWidth - 1; x >= 0; x--) { // columns
					p = (p2 - slope);
					p1 = (p3 - slope1);
					if (p1 > p) p = p1;

					int xPos = x;
					int yPos = (y + 1 == ImageHeight) ? y : y + 1;
					destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
					p3 = destPixelsOpsRA.get().getRealFloat();
					p1 = p3 - slope;
					if (p1 > p) p = p1;

					xPos = (x == 0) ? 0 : x - 1;
					yPos = (y + 1 == ImageHeight) ? y : y + 1;
					destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
					p4 = destPixelsOpsRA.get().getRealFloat();
					p1 = p4 - slope1;
					if (p1 > p) p = p1;

					xPos = x;
					yPos = y;
					destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
					p2 = destPixelsOpsRA.get().getRealFloat();

					// is never accessed (with current test img)
					if (p > p2) {
						xPos = x;
						yPos = y;
						destPixelsOpsRA.setPosition(new int[] { xPos, yPos });
						destPixelsOpsRA.get().setReal(p);
						p2 = p;
					}
				}
			}
		}

		for (int ii = 0; ii < m_channels; ii++) {
			while (tmpPixelsOpsCursor.hasNext()) {
				tmpPixelsOpsCursor.next();
				srcPixelsOpsRA.setPosition(tmpPixelsOpsCursor);
				destPixelsOpsRA.setPosition(tmpPixelsOpsCursor);
				if (m_TopHat) {
					float value = (m_Down ? (srcPixelsOpsRA.get().getRealFloat() -
						destPixelsOpsRA.get().getRealFloat() + 255) : (destPixelsOpsRA.get()
							.getRealFloat() - srcPixelsOpsRA.get().getRealFloat()));
					tmpPixelsOpsCursor.get().setReal(value);

				}
				else {

					tmpPixelsOpsCursor.get().setReal((sign * destPixelsOpsRA.get()
						.getRealFloat()));

				}
			}
		}

		return tmpPixelsOps;

	}

}
