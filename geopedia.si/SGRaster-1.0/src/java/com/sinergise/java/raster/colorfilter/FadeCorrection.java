/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.util.state.State;


public class FadeCorrection extends ColorFilter {
	public static final String	TYPE_FADE_CORRECTION	= "FadeCorrection";
	public static final String	STKEY_AMOUNT			= "amount";
	private static final String	STKEY_LIGHTEN			= "lighten";
	private static final String	STKEY_PRESERVE_GRAY		= "preserveGray";
	protected double			amount					= 0.5;
	protected double			preserveGray			= 0.5;
	protected double			lighten					= 0;

	protected transient double	ss1;
	protected transient double	ff;
	protected transient double	ds255;
	protected transient double	cond1;
	protected transient double	cond2;


	public FadeCorrection(final double amount) {
		this(TYPE_FADE_CORRECTION, amount);
	}

	protected FadeCorrection(final String type, final double amount) {
		super(type);
		setAmount(amount);
	}

	public void setAmount(final double amount) {
		this.amount = amount;
		updateTransient();
	}

	protected void updateTransient() {
		final double ss = 0.5 * amount;
		ss1 = 1 - ss;
		//        preserveGray=ColorUtils.clip(preserveGray, 0.0, 1.0);
		//        lighten=ColorUtils.clip(lighten, 0, 1);
		final double lght = 0.5 * (1 + lighten);
		ff = 0.5 * preserveGray;
		final double ff1 = 1 - ff;
		final double ffss = ff * ss;
		final double ds = (1 - lght) * ss;
		ds255 = 255 * ds;
		final double ds1 = lght * ss;
		cond1 = 255 * (ff == 0 ? 0 : (ds / (ff1 + ffss)));
		cond2 = 255 * (ff == 0 ? 1 : (1.0 - ds1 / (ff1 + ffss)));
	}

	@Override
	public boolean filter(final int[] rgba) {
		if (amount == 0 && lighten == 0) {
			return false;
		}
		final int cSamples = outputSampleSize > 2 ? 3 : 1;
		for (int i = 0; i < cSamples; i++) {
			final int t255 = rgba[i];
			if (t255 <= cond1) {
				rgba[i] = ColorUtil.clipInt(ff * t255, 0, 255);
			} else if (t255 >= cond2) {
				rgba[i] = ColorUtil.clipInt(255.0 + ff * (t255 - 255), 0, 255);
			} else {
				rgba[i] = ColorUtil.clipInt((t255 - ds255) / ss1, 0, 255);
			}
		}
		return true;
	}

	public boolean filter(final byte[] data, final int off, final byte[] outData, final int outOff) {
		if (amount == 0 && lighten == 0) {
			return false;
		}
		final int cSamples = outputSampleSize > 2 ? 3 : 1;
		for (int i = 0; i < cSamples; i++) {
			final int t255 = 0xFF & data[off + i];
			if (t255 <= cond1) {
				outData[outOff + i] = (byte)ColorUtil.clipInt(ff * t255, 0, 255);
			} else if (t255 >= cond2) {
				outData[outOff + i] = (byte)ColorUtil.clipInt(255.0 + ff * (t255 - 255), 0, 255);
			} else {
				outData[outOff + i] = (byte)ColorUtil.clipInt((t255 - ds255) / ss1, 0, 255);
			}
		}
		return true;
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		return srcNumComponents;
	}

	@Override
	public void setState(final State state) {
		lighten = state.getDouble(STKEY_LIGHTEN, 0);
		preserveGray = state.getDouble(STKEY_PRESERVE_GRAY, 0.5);
		setAmount(state.getDouble(STKEY_AMOUNT, 0)); // will update transient
	}

	@Override
	public State getState() {
		final State ret = super.getState();
		if (amount != 0) {
			ret.putDouble(STKEY_AMOUNT, amount);
		}
		if (lighten != 0) {
			ret.putDouble(STKEY_LIGHTEN, lighten);
		}
		if (preserveGray != 0.5) {
			ret.putDouble(STKEY_PRESERVE_GRAY, preserveGray);
		}
		return ret;
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type);
		out.append(amount);
		out.append(lighten);
		out.append(preserveGray);
	}

}
