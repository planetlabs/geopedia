/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import com.sinergise.java.util.state.State;


public class SubtractWhiteFilter extends ColorFilter {
	protected static final String	TYPE	= "SubtractWhite";
	public double					gamma	= 1;
	public double					amount	= 1;

	protected SubtractWhiteFilter(final String filterType) {
		super(filterType);
	}

	public SubtractWhiteFilter() {
		this(TYPE);
	}

	@Override
	public boolean filterBinary(final boolean white, final int[] dstRGBA) {
		if (white) {
			dstRGBA[0] = 255;
			dstRGBA[1] = 255;
			dstRGBA[2] = 255;
			dstRGBA[3] = 0;
		} else {
			dstRGBA[0] = 0;
			dstRGBA[1] = 0;
			dstRGBA[2] = 0;
			dstRGBA[3] = 255;
		}
		return true;
	}

	@Override
	public boolean filterGray(final int srcGray, final int[] dstRGBA) {
		dstRGBA[0] = 0;
		dstRGBA[1] = 0;
		dstRGBA[2] = 0;
		dstRGBA[3] = 255 - srcGray;
		return true;
	}

	@Override
	public boolean filter(final int[] rgba) {
		final int r = rgba[0];
		final int g = inputHasColor ? rgba[1] : 255;
		final int b = inputHasColor ? rgba[2] : 255;
		final int a = inputHasAlpha ? inputHasColor ? rgba[3] : rgba[1] : 255;

		int min = r < g ? (r < b ? r : b) : (g < b ? g : b);
		if (amount < 1) min = (int)Math.round(amount * min);

		if (gamma > 1) {
			if (gamma == 2) min = (int)((double)min * min / 255);
			else if (gamma == 3) min = (int)((double)min * min * min / 255 / 255);
			else min = (int)(255 * Math.pow((double)min / 255, gamma));
		}
		if (min == 255 || a == 0) {
			rgba[3] = 0;
			return true;
		}
		final double invA = 1.0 / (1.0 - min / 255.0);
		rgba[3] = (int)(a / invA);
		rgba[0] = (int)((r - min) * invA);
		rgba[1] = (int)((g - min) * invA);
		rgba[2] = (int)((b - min) * invA);
		return true;
	}

	@Override
	public int filterInt(final int srcARGB) {
		final int b = srcARGB & 0xFF;
		final int g = (srcARGB >>> 8) & 0xFF;
		final int r = (srcARGB >>> 16) & 0xFF;
		final int a = (srcARGB >>> 24) & 0xFF;

		int min = r < g ? (r < b ? r : b) : (g < b ? g : b);
		if (amount < 1) min = (int)Math.round(amount * min);

		if (gamma > 1) {
			if (gamma == 2) min = (int)((double)min * min / 255);
			else if (gamma == 3) min = (int)((double)min * min * min / 255 / 255);
			else min = (int)(255 * Math.pow((double)min / 255, gamma));
		}
		if (min == 255 || a == 0) return 0;
		
		final double invA = 255.0 / (255 - min);
		return ((int)((b - min) * invA)) | ((int)((g - min) * invA) << 8) | ((int)((r - min) * invA) << 16) | ((int)(a / invA) << 24);
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		if (srcNumComponents < 4) return 4;
		return srcNumComponents;
	}

	@Override
	public State getState() {
		final State ret = super.getState();
		if (gamma != 1) ret.putDouble("gamma", gamma);
		if (amount != 1) ret.putDouble("amount", amount);
		return ret;
	}

	@Override
	public void setState(final State state) {
		gamma = state.getDouble("gamma", 1);
		amount = state.getDouble("amount", 1);
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type).append(amount).append(gamma);
	}
}
