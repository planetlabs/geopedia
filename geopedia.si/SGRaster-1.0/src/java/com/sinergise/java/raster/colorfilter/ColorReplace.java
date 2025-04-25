package com.sinergise.java.raster.colorfilter;

import static com.sinergise.common.util.math.ColorUtil.argbFromRGBA;
import static com.sinergise.common.util.math.ColorUtil.fromIntArray;

import java.awt.Color;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.util.state.State;


public class ColorReplace extends ColorFilter {
	protected static final String	TYPE_COLOR_REPLACE	= "ColorReplace";

	public final int[]				cFrom				= new int[]{0, 0, 0, 0};
	public final int[]				cTo					= new int[]{0, 0, 0, 0};
	public boolean					matchSrcAlpha		= false;
	public boolean					applyDestAlpha		= true;
	public int						toleranceSq			= 0;
	public boolean					smooth				= false;

	private static final String		STKEY_FROM_COLOR	= "from";
	private static final String		STKEY_TO_COLOR		= "to";
	private static final String		STKEY_TOLERANCE		= "tolerance";
	private static final String		STKEY_SMOOTH		= "smooth";
	private static final String		STKEY_MATCH_ALPHA	= "matchAlpha";
	private static final String		STKEY_APPLY_ALPHA	= "applyAlpha";


	public ColorReplace() {
		super(TYPE_COLOR_REPLACE);
	}
	
	public ColorReplace(final int srcARGB, final int tgtARGB) {
		this();
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(srcARGB), this.cFrom);
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(tgtARGB), this.cTo);
	}
	
	@Override
	public ColorReplace clone() {
		ColorReplace ret = new ColorReplace(argbFromRGBA(fromIntArray(cFrom)),argbFromRGBA(fromIntArray(cTo)));
		copyInto(ret);
		return ret;
	}
	
	@Override
	protected void copyInto(ColorFilter copy) {
		super.copyInto(copy);
		ColorReplace crc = (ColorReplace)copy;
		crc.applyDestAlpha = applyDestAlpha;
		crc.matchSrcAlpha = matchSrcAlpha;
		crc.smooth = smooth;
		crc.toleranceSq = toleranceSq;
	}

	public ColorReplace(final int srcARGB, final int tgtARGB, final boolean matchSrcAlpha, final boolean applyTgtAlpha) {
		this();
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(srcARGB), this.cFrom);
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(tgtARGB), this.cTo);
		this.matchSrcAlpha = matchSrcAlpha;
		this.applyDestAlpha = applyTgtAlpha;
	}

	public Color getFromColor() {
		return ColorUtil.colorFromIntArray(cFrom);
	}

	public void setFromColor(final Color c) {
		ColorUtil.toIntArray(c, cFrom);
	}

	public Color getToColor() {
		return ColorUtil.colorFromIntArray(cTo);
	}

	public void setToColor(final Color c) {
		ColorUtil.toIntArray(c, cTo);
	}

	public ColorReplace setSmooth(final boolean smooth) {
		this.smooth = smooth;
		return this;
	}

	public ColorReplace setTolerance(final int tolerance) {
		this.toleranceSq = tolerance * tolerance;
		return this;
	}

	/**
	 * @param matchAlpha Flag which controls whether alpha component is used when comparing with the "from" color.
	 * @return this
	 */
	public ColorReplace setMatchAlpha(final boolean matchAlpha) {
		this.matchSrcAlpha = matchAlpha;
		return this;
	}

	/**
	 * If set to true, the resulting color's opacity will equal the "to" color's opacity. If set to false, the resulting color's opacity
	 * will equal the opacity of the source pixel. In the latter case, alpha of the "to" color controls the blending opacity ("strength" of
	 * the replacement operation)
	 * 
	 * @param applyAlpha Flag which controls whether the "to" color's alpha component is used in the result.
	 * @return this
	 */
	public ColorReplace setApplyAlpha(final boolean applyAlpha) {
		this.applyDestAlpha = applyAlpha;
		return this;
	}

	@Override
	public boolean filter(final int[] rgba) {
		int deltaSq = 0;
		if ((deltaSq += ColorUtil.sqr(rgba[0] - cFrom[0])) > toleranceSq) {
			return false;
		}
		if ((deltaSq += ColorUtil.sqr(rgba[1] - cFrom[1])) > toleranceSq) {
			return false;
		}
		if ((deltaSq += ColorUtil.sqr(rgba[2] - cFrom[2])) > toleranceSq) {
			return false;
		}
		if (matchSrcAlpha) {
			if ((deltaSq += ColorUtil.sqr(rgba[3] - cFrom[3])) > toleranceSq) {
				return false;
			}
		}
		if (smooth && toleranceSq > 0) {
			double invDeltaFact = deltaSq == 0 ? 0 : Math.sqrt((double)deltaSq / toleranceSq);
			double deltaFact = 1.0 - invDeltaFact;
			if (deltaFact < 1) {
				//System.out.println(deltaFact+" "+deltaSq);
			}
			if (applyDestAlpha) {
				final double dstA = (rgba[3] * (1 - deltaFact) + deltaFact * cTo[3]) / 255;
				rgba[0] = (int)((rgba[0] * rgba[3] * invDeltaFact + cTo[0] * cTo[3] * deltaFact) / 255 / dstA);
				rgba[1] = (int)((rgba[1] * rgba[3] * invDeltaFact + cTo[1] * cTo[3] * deltaFact) / 255 / dstA);
				rgba[2] = (int)((rgba[2] * rgba[3] * invDeltaFact + cTo[2] * cTo[3] * deltaFact) / 255 / dstA);
				rgba[3] = (int)(dstA * 255);
			} else {
				deltaFact = deltaFact * cTo[3] / 255;
				invDeltaFact = 1 - deltaFact;
				rgba[0] = (int)(rgba[0] * invDeltaFact + cTo[0] * deltaFact);
				rgba[1] = (int)(rgba[1] * invDeltaFact + cTo[1] * deltaFact);
				rgba[2] = (int)(rgba[2] * invDeltaFact + cTo[2] * deltaFact);
			}
		} else {
			rgba[0] = cTo[0];
			rgba[1] = cTo[1];
			rgba[2] = cTo[2];
			if (applyDestAlpha) {
				rgba[3] = cTo[3];
			}
		}
		return true;
	}

	@Override
	public void setState(final State state) {
		super.setState(state);
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(state.getColor(STKEY_FROM_COLOR).getRGB()), cFrom);
		ColorUtil.toIntArray(ColorUtil.rgbaFromARGB(state.getColor(STKEY_TO_COLOR).getRGB()), cTo);
		setTolerance(state.getInt(STKEY_TOLERANCE, 0));
		setSmooth(state.getBoolean(STKEY_SMOOTH, false));
		setMatchAlpha(state.getBoolean(STKEY_MATCH_ALPHA, false));
		setApplyAlpha(state.getBoolean(STKEY_APPLY_ALPHA, true));
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		if (srcNumComponents >= 4 || cTo[3] == 255) {
			return srcNumComponents;
		}
		return cTo[3] == 255 ? 3 : 4;
	}

	@Override
	public State getState() {
		final State st = super.getState();
		st.putColor(STKEY_FROM_COLOR, new Color(ColorUtil.argbFromRGBA(ColorUtil.fromIntArray(cFrom)), true));
		st.putColor(STKEY_TO_COLOR, new Color(ColorUtil.argbFromRGBA(ColorUtil.fromIntArray(cTo)), true));
		if (toleranceSq > 0) {
			st.putInt(STKEY_TOLERANCE, (int)Math.sqrt(toleranceSq));
		}
		if (smooth) {
			st.putBoolean(STKEY_SMOOTH, true);
		}
		if (matchSrcAlpha) {
			st.putBoolean(STKEY_MATCH_ALPHA, true);
		}
		if (!applyDestAlpha) {
			st.putBoolean(STKEY_APPLY_ALPHA, false);
		}
		return st;
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type);
		out.append(applyDestAlpha ? 'T' : 'F');
		out.append(matchSrcAlpha ? 'T' : 'F');
		out.append(smooth ? 'T' : 'F');
		out.append(Integer.toHexString(cFrom[3] << 24 | cFrom[0] << 16 | cFrom[1] << 8 | cFrom[2]));
		out.append(Integer.toHexString(cTo[3] << 24 | cTo[0] << 16 | cTo[1] << 8 | cTo[2]));
		out.append(toleranceSq);
	}
}
