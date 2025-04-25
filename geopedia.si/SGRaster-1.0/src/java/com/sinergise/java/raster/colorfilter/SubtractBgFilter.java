/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import java.awt.Color;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.util.math.BlendMode;
import com.sinergise.java.util.state.State;


public class SubtractBgFilter extends ColorFilter {
	protected static final String	TYPE	= "SubtractBackground";
	public double					gamma	= 1;
	public double					amount	= 1;
	public final int[]				bgColor	= new int[]{255, 255, 255, 255};
	public transient double			br;
	public transient double			bg;
	public transient double			bb;

	protected SubtractBgFilter(final String filterType) {
		super(filterType);
	}

	public SubtractBgFilter() {
		this(TYPE);
	}

	public SubtractBgFilter(final Color bgColor, final double gamma, final double amount) {
		this();
		this.gamma = gamma;
		this.amount = amount;
		setBGColor(bgColor);
	}
	
	@Override
	public ColorFilter clone() {
		int bgCol = ColorUtil.argbFromRGBA(ColorUtil.fromIntArray(bgColor));
		SubtractBgFilter ret = new SubtractBgFilter(new Color(bgCol, true), gamma, amount);
		return ret;
	}

	@Override
	public boolean filter(final int[] rgba) {
		final double sr = rgba[0] / 255.0;
		final double sg = inputHasColor ? rgba[1] / 255.0 : 1.0;
		final double sb = inputHasColor ? rgba[2] / 255.0 : 1.0;
		final double sa = inputHasAlpha ? inputHasColor ? rgba[3] / 255.0 : rgba[1] / 255.0 : 1.0;

		final double ar = sr == br ? 0 : sr > br ? (sr - br) / (1 - br) : (br - sr) / br;
		final double ag = sg == bg ? 0 : sg > bg ? (sg - bg) / (1 - bg) : (bg - sg) / bg;
		final double ab = sb == bb ? 0 : sb > bb ? (sb - bb) / (1 - bb) : (bb - sb) / bb;

		double ta = Math.max(ar, Math.max(ag, ab));
		if (ta < 1 && amount < 1) {
			ta = 1 - (1 - ta) * amount;
		}
		if (gamma > 1) {
			if (gamma == 2) {
				ta = 1 - (1 - ta) * (1 - ta);
			} else if (gamma == 3) {
				ta = 1 - (1 - ta) * (1 - ta) * (1 - ta);
			} else {
				ta = 1 - Math.pow(1 - ta, gamma);
			}
		}

		if (ta <= 0) {
			rgba[3] = 0;

		} else if (ta >= 1) {
			return false;

		} else {
			rgba[3] = ColorUtil.clipInt(255 * ta * sa, 0, 255);
			rgba[0] = ColorUtil.clipInt((255 * (br * (ta - 1) + sr) / ta), 0, 255);
			rgba[1] = ColorUtil.clipInt((255 * (bg * (ta - 1) + sg) / ta), 0, 255);
			rgba[2] = ColorUtil.clipInt((255 * (bb * (ta - 1) + sb) / ta), 0, 255);
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
		updateTransient();
		//        bgGray = ColorUtils.RGBToGray(bgColor[0], bgColor[1], bgColor[2]);
		//        bgWhite = bgGray >= 128;
	}

	protected void updateTransient() {
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

	public static void main(final String[] args) {
		final SubtractBgFilter sbf = new SubtractBgFilter();
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
