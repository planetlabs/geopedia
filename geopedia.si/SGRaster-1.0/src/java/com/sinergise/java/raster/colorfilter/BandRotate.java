package com.sinergise.java.raster.colorfilter;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.java.util.state.State;


public class BandRotate extends ColorFilter {
	protected static final String	TYPE_BAND_ROTATE	= "BandRotate";
	private static final String		STKEY_BANDS			= "bands";
	private final int[]				bands				= new int[]{0, 1, 2, 3};

	public BandRotate() {
		super(TYPE_BAND_ROTATE);
	}

	public BandRotate(final int[] bands) {
		super(TYPE_BAND_ROTATE);
		setBands(bands);
	}

	@Override
	public boolean filter(final int[] rgba) {
		final int r = rgba[bands[0]];
		final int g = rgba[bands[1]];
		final int b = rgba[bands[2]];
		final int a = rgba[bands[3]];
		rgba[0] = r;
		rgba[1] = g;
		rgba[2] = b;
		rgba[3] = a;
		return true;
	}

	@Override
	public int filterInt(final int srcARGB) {
		final int r = (srcARGB >>> (8 * bands[0])) & 0xFF;
		final int g = (srcARGB >>> (8 * bands[1])) & 0xFF;
		final int b = (srcARGB >>> (8 * bands[2])) & 0xFF;
		final int a = (srcARGB >>> (8 * bands[3])) & 0xFF;
		return b | (g << 8) | (r << 16) | (a << 24);
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		return bands.length;
	}

	@Override
	public void setState(final State state) {
		setBands(state.getIntSeq(STKEY_BANDS));
	}

	public void setBands(final int[] bnds) {
		for (int i = 0; i < 4; i++) {
			bands[i] = i;
		}
		if (bnds.length == 1) {
			for (int i = 0; i < 3; i++) {
				bands[i] = bnds[0];
			}
		} else {
			for (int i = 0; i < bnds.length; i++) {
				bands[i] = bnds[i];
			}
		}
	}

	@Override
	public State getState() {
		final State ret = super.getState();
		if (bands != null) {
			ret.putIntSeq(STKEY_BANDS, bands);
		}
		return ret;
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(TYPE_BAND_ROTATE);
		if (bands != null && bands.length > 0) {
			for (final int band : bands) {
				out.append(band);
			}
		}
	}
	
	@Override
	public ColorFilter clone() {
		BandRotate ret = new BandRotate(ArrayUtil.arraycopy(bands));
		copyInto(ret);
		return ret;
	}

	@Override
	protected void copyInto(ColorFilter copy) {
		super.copyInto(copy);
	}
}
