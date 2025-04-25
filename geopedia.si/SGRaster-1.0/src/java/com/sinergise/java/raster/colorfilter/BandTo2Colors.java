/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import java.awt.Color;

import com.sinergise.java.util.state.State;


public class BandTo2Colors extends ColorFilter1D {

	private static final String		STKEY_COLORMIN	= "colorMin";
	private static final String		STKEY_COLORMAX	= "colorMax";
	protected Color					colMin;
	protected Color					colMax;
	protected int					cMin0;
	protected int					cMin1;
	protected int					cMin2;
	protected int					cMin3;
	protected int					cMax0;
	protected int					cMax1;
	protected int					cMax2;
	protected int					cMax3;

	protected static final String	TYPE_BAND2C		= "BandTo2C";

	protected BandTo2Colors(final String filterType, final Color colorMin, final Color colorMax) {
		super(filterType);
		setColorMin(colorMin);
		setColorMax(colorMax);
	}

	public BandTo2Colors(final int bandNo, final Color colorMax, final Color colorMin) {
		this(TYPE_BAND2C, colorMin, colorMax);
		band = bandNo;
	}

	@Override
	public boolean filterGray(final int a, final int[] dstRGBA) {
		final int invA = 255 - a;
		final int dstA = (a * cMax3 + invA * cMin3 - 1) / 255 + 1;
		if (dstA == 0) {
			switch (outputSampleSize) {
				case 4:
					dstRGBA[3] = 0; //$FALL-THROUGH$
				case 3:
					dstRGBA[2] = 0; //$FALL-THROUGH$
				case 2:
					dstRGBA[1] = 0; //$FALL-THROUGH$
				case 1:
					dstRGBA[0] = 0; //$FALL-THROUGH$
				default:
					break;
			}
		} else {
			switch (outputSampleSize) {
				case 4:
					dstRGBA[3] = dstA; //$FALL-THROUGH$
				case 3:
					dstRGBA[2] = (a * cMax2 + invA * cMin2) / dstA;
					dstRGBA[1] = (a * cMax1 + invA * cMin1) / dstA;
					dstRGBA[0] = (a * cMax0 + invA * cMin0) / dstA;
					break;
				case 2:
					dstRGBA[1] = dstA; //$FALL-THROUGH$
				case 1:
					dstRGBA[0] = a;
					break;
				default:
					break;
			}
		}
		return true;
	}

	@Override
	public int filterGrayInt(final int a) {
		final int invA = 255 - a;
		final int dstA = (a * cMax3 + invA * cMin3 - 1) / 255 + 1;
		if (dstA == 0) {
			return 0;
		}
		int ret = 0;
		switch (outputSampleSize) {
			case 4:
				ret = dstA << 24; //$FALL-THROUGH$
			case 3:
				ret |= (((a * cMax0 + invA * cMin0) / dstA) << 16);
				ret |= (((a * cMax1 + invA * cMin1) / dstA) << 8);
				ret |= ((a * cMax2 + invA * cMin2) / dstA);
				return ret;
			case 2:
				ret = dstA << 8; //$FALL-THROUGH$
			case 1:
				ret = ret | a;
				return ret;
		}
		return ret;
	}

	@Override
	public boolean filterGray(final int a, final byte[] dst, final int dstOff) {
		final int invA = 255 - a;
		final int dstA = (a * cMax3 + invA * cMin3 - 1) / 255 + 1;
		if (dstA == 0) {
			switch (outputSampleSize) {
				case 4:
					dst[dstOff + 3] = 0; //$FALL-THROUGH$
				case 3:
					dst[dstOff + 2] = 0; //$FALL-THROUGH$
				case 2:
					dst[dstOff + 1] = 0; //$FALL-THROUGH$
				case 1:
					dst[dstOff] = 0; //$FALL-THROUGH$
				default:
					break;
			}
		} else {
			switch (outputSampleSize) {
				case 4:
					dst[dstOff + 3] = (byte)(dstA); //$FALL-THROUGH$
				case 3:
					dst[dstOff + 2] = (byte)((a * cMax2 + invA * cMin2) / dstA);
					dst[dstOff + 1] = (byte)((a * cMax1 + invA * cMin1) / dstA);
					dst[dstOff] = (byte)((a * cMax0 + invA * cMin0) / dstA);
					break;
				case 2:
					dst[dstOff + 1] = (byte)(dstA); //$FALL-THROUGH$
				case 1:
					dst[dstOff] = (byte)(a);
					break;
				default:
					break;
			}
		}
		return true;
	}

	@Override
	public State getState() {
		final State ret = super.getState();
		ret.putColor(STKEY_COLORMIN, colMin);
		ret.putColor(STKEY_COLORMAX, colMax);
		return ret;
	}

	@Override
	public void setState(final State state) {
		super.setState(state);
		setColorMin(state.getColor(STKEY_COLORMIN));
		setColorMax(state.getColor(STKEY_COLORMAX));
	}

	private void setColorMax(Color clr) {
		if (clr == null) {
			clr = C_TRANSPARENT;
		}
		colMax = clr;
		final double a = (double)clr.getAlpha() / 255;
		this.cMax0 = (int)(clr.getRed() * a);
		this.cMax1 = (int)(clr.getGreen() * a);
		this.cMax2 = (int)(clr.getBlue() * a);
		this.cMax3 = clr.getAlpha();
	}

	private void setColorMin(Color clr) {
		if (clr == null) {
			clr = Color.WHITE;
		}
		colMin = clr;
		final double a = (double)clr.getAlpha() / 255;
		this.cMin0 = (int)(clr.getRed() * a);
		this.cMin1 = (int)(clr.getGreen() * a);
		this.cMin2 = (int)(clr.getBlue() * a);
		this.cMin3 = clr.getAlpha();
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		super.appendIdentifier(out);
		if (colMin != null) {
			out.append(Integer.toHexString(colMin.getRGB()));
		}
		if (colMax != null) {
			out.append(Integer.toHexString(colMax.getRGB()));
		}
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		if (srcNumComponents == 4) {
			return 4;
		}
		if (colMin.getAlpha() == 255 && colMax.getAlpha() == 255) {
			return 3;
		}
		return 4;
	}

	public Color getColorMin() {
		return colMin;
	}

	public Color getColorMax() {
		return colMax;
	}
}
