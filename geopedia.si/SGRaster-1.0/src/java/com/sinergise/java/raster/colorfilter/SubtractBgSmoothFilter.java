/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import java.awt.Color;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.util.math.BlendMode;
import com.sinergise.java.util.state.State;


public class SubtractBgSmoothFilter extends ColorFilter {
	protected static final String	TYPE		= "SubtractBgOnGray";
	public double					gamma		= 1;
	public double					amount		= 1;
	public final int[]				bgColor		= new int[]{255, 255, 255, 255};
	public transient double			br;
	public transient double			bg;
	public transient double			bb;

	public double					toleranceSq	= 0;
	public boolean					smooth		= false;

	protected SubtractBgSmoothFilter(final String filterType) {
		super(filterType);
	}

	public SubtractBgSmoothFilter() {
		this(TYPE);
	}

	public SubtractBgSmoothFilter(final Color bgColor, final double gamma, final double amount) {
		this();
		this.gamma = gamma;
		this.amount = amount;
		setBGColor(bgColor);
	}
	
	@Override
	public SubtractBgSmoothFilter clone() {
		SubtractBgSmoothFilter ret = new SubtractBgSmoothFilter();
		copyInto(ret);
		return ret;
	}
	
	@Override
	protected void copyInto(ColorFilter copy) {
		super.copyInto(copy);
		int bgCol = ColorUtil.argbFromRGBA(ColorUtil.fromIntArray(bgColor));
		SubtractBgSmoothFilter sc = (SubtractBgSmoothFilter)copy;
		sc.amount = amount;
		sc.gamma = gamma;
		sc.smooth = smooth;
		sc.toleranceSq = toleranceSq;
		sc.setBGColor(new Color(bgCol, true));
	}

	@Override
	public boolean filter(final int[] rgba) {
		final double sr = rgba[0] / 255.0;
		final double sg = inputHasColor ? rgba[1] / 255.0 : sr;
		final double sb = inputHasColor ? rgba[2] / 255.0 : sr;
		final double sa = inputHasAlpha ? inputHasColor ? rgba[3] / 255.0 : rgba[1] / 255.0 : 1.0;

		final double lbr = br;
		final double lbg = bg;
		final double lbb = bb;

		// Preserve GRAY
		double tTr = (lbg == lbr) ? ((sb - sr) / (lbb - lbr)) : ((sg - sr) / (lbg - lbr));
		if (tTr <= 0) {
			return false;
		}

		// Shouldn't go above the ceiling of transparency
		final double tr = sr == lbr ? 1 : sr > lbr ? (1 - sr) / (1 - lbr) : sr / lbr;
		final double tg = sg == lbg ? 1 : sg > lbg ? (1 - sg) / (1 - lbg) : sg / lbg;
		final double tb = sb == lbb ? 1 : sb > lbb ? (1 - sb) / (1 - lbb) : sb / lbb;
		tTr = Math.min(tTr, Math.min(tr, Math.min(tg, tb)));


		if (tTr < 1) {
			if (tTr <= 0) {
				return false;
			}

			if (smooth) {
				if (toleranceSq == 0) {
					if ((sr != lbr) || (sg != lbg) || (sb != lbb)) {
						return false;
					}
				} else {
					double distanceSq = (sr - lbr) * (sr - lbr);
					if (distanceSq >= toleranceSq) {
						return false;
					}
					distanceSq += (sg - lbg) * (sg - lbg);
					if (distanceSq >= toleranceSq) {
						return false;
					}
					distanceSq += (sb - lbb) * (sb - lbb);
					if (distanceSq >= toleranceSq) {
						return false;
					}

					if (smooth && distanceSq > 0) {
						tTr *= Math.sqrt(1.0 - distanceSq / toleranceSq);
					}
				}
			}

			if (tTr <= 0) {
				return false;
			}
		}
		if (amount < 1) {
			tTr = tTr * amount;
		}

		if (tTr <= 0) {
			return false;
		}

		if (gamma > 1) {
			if (gamma == 2) {
				tTr *= tTr;
			} else if (gamma == 3) {
				tTr = tTr * tTr * tTr;
			} else {
				tTr = Math.pow(tTr, gamma);
			}
		}

		if (tTr <= 0) {
			return false;
		}

		if (tTr >= 1) {
			rgba[3] = 0;

		} else {
			final double ta = 1 - tTr;
			final double taD255 = 255.0 / ta;
			rgba[3] = ColorUtil.clipInt(255 * ta * sa, 0, 255);
			rgba[0] = ColorUtil.clipInt((taD255 * (sr - tTr * lbr)), 0, 255);
			rgba[1] = ColorUtil.clipInt((taD255 * (sg - tTr * lbg)), 0, 255);
			rgba[2] = ColorUtil.clipInt((taD255 * (sb - tTr * lbb)), 0, 255);
		}
		return true;
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		if (srcNumComponents < 4) {
			return 4;
		}
		return srcNumComponents;
	}

	@Override
	public State getState() {
		final State ret = super.getState();
		if (gamma != 1) {
			ret.putDouble("gamma", gamma);
		}
		if (amount != 1) {
			ret.putDouble("amount", amount);
		}
		return ret;
	}

	@Override
	public void setState(final State state) {
		setBGColor(state.getColor("bgColor"));
		gamma = state.getDouble("gamma", 1);
		amount = state.getDouble("amount", 1);
	}

	public void setBGColor(final Color clr) {
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(clr.getRGB()), bgColor);
		br = bgColor[0] / 255.0;
		bg = bgColor[1] / 255.0;
		bb = bgColor[2] / 255.0;
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type);
		out.append(amount);
		out.append(gamma);
	}

	public SubtractBgSmoothFilter setTolerance(final int tolerance) {
		this.toleranceSq = (tolerance * tolerance) / (255.0 * 255.0);
		return this;
	}

	public SubtractBgSmoothFilter setSmooth(final boolean smooth) {
		this.smooth = smooth;
		return this;
	}

	public static void main(final String[] args) {
		final SubtractBgSmoothFilter sbf = new SubtractBgSmoothFilter();
		sbf.setBGColor(new Color(0, 0, 0));
		final int[] bg = sbf.bgColor;
		final int[] ret = new int[4];
		final int[] src = new int[]{255, 255, 255, 255};
		sbf.filterBinary(true, ret);
		final int[] trBack = BlendMode.srcOver(ret, sbf.bgColor);
		for (int i = 0; i < ret.length; i++) {
			System.out.println(bg[i] + "\t" + src[i] + "\t" + ret[i] + "\t" + trBack[i]);
		}
	}
}
