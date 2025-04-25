package com.sinergise.java.raster.colorfilter;

import com.sinergise.java.util.state.State;

public abstract class ColorFilter1D extends ColorFilter {
	private static final String	STKEY_BAND		= "band";

	public static final double	GRAY_FACTOR_B	= 0.114;
	public static final double	GRAY_FACTOR_G	= 0.587;
	public static final double	GRAY_FACTOR_R	= 0.299;

	public static final int		BAND_GRAY		= -1;

	protected int				band;

	protected ColorFilter1D(final String type) {
		super(type);
	}

	public boolean filterGray(final int a, final byte[] dst, final int dstOff) {
		final int[] tmpArr = this.tmp.get();
		if (!filterGray(a, tmpArr)) return false;
		for (int i = 0; i < outputSampleSize; i++) {
			dst[dstOff + i] = (byte)tmpArr[i];
		}
		return true;
	}

	@Override
	public abstract boolean filterGray(int a, int[] dstRGBA);

	public int filterGrayInt(final int a) {
		final int[] tmpArr = this.tmp.get();
		if (!filterGray(a, tmpArr)) return a;
		return tmpArr[2] + (tmpArr[1] << 8) + (tmpArr[0] << 16) + (tmpArr[3] << 24);
	}

	@Override
	public int filterInt(final int src) {
		if (band == -1) {

			final double val = (inputSampleSize < 3) ? (0xFF & src) : ((0xFF & (src >>> 16)) * GRAY_FACTOR_R + (0xFF & (src >>> 8))
					* GRAY_FACTOR_G + (0xFF & src) * GRAY_FACTOR_B);

			final double a = !inputHasAlpha ? 1.0 : ((inputSampleSize == 2)
					? (double)(0xFF & src >> 8) / 255
					: (double)(0xFF & src >>> 24) / 255);

			return filterGrayInt((int)(a * val + (1 - a) * 255));
			
		}
		return filterGrayInt(0xFF & (src >>> (8 * ((2 - band) % 4))));
	}

	@Override
	public boolean filter(final int[] rgba) {
		if (band == -1) {
			
			final double val = (inputSampleSize < 3) ? (rgba[0]) : (rgba[0] * GRAY_FACTOR_R + rgba[1] * GRAY_FACTOR_G + rgba[2]
					* GRAY_FACTOR_B);
			
			if (inputSampleSize % 2 == 0) {
				final double a = (double)rgba[inputSampleSize - 1] / 255;
				return filterGray((int)(val * a + (1 - a) * 255 + 0.5), rgba);
			}
			
			return filterGray((int)(val + 0.5), rgba);
		}
		return filterGray(rgba[band], rgba);
	}

	@Override
	public boolean filterBytes(final byte[] data, final int off, final byte[] outData, final int outOff, final int[] byteOrder) {
		if (band == -1) {
			final double val = (inputSampleSize < 3) ? (0xFF & data[off]) : ((0xFF & data[off]) * GRAY_FACTOR_R + (0xFF & data[off + 1])
					* GRAY_FACTOR_G + (0xFF & data[off + 2]) * GRAY_FACTOR_B);
			if (inputSampleSize % 2 == 0) {
				final double a = (double)(0xFF & data[off + inputSampleSize - 1]) / 255;
				return filterGray((int)(val * a + (1 - a) * 255 + 0.5), outData, outOff);
			}
			return filterGray((int)(val + 0.5), outData, outOff);
		}
		return filterGray(0xFF & data[off + band], outData, outOff);
	}

	@Override
	public void setState(final State state) {
		super.setState(state);
		this.band = state.getInt(STKEY_BAND, BAND_GRAY);
	}

	@Override
	public State getState() {
		final State st = super.getState();
		if (band != BAND_GRAY) st.putInt(STKEY_BAND, band);
		return st;
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type).append(band);
	}

}
