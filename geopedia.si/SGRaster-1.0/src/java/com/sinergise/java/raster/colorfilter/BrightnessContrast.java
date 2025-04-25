/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.util.state.State;


public class BrightnessContrast extends ColorFilter {
	public static final String	TYPE_BRIGHT_CONTRAST	= "BrightnessContrast";
	public static final String	STKEY_CONTRAST			= "contrast";
	public static final String	STKEY_BRIGHTNESS		= "brightness";
	private double				brightness				= 0;
	private double				contrast				= 0;
	private transient double	contFactor				= 1;

	public BrightnessContrast(final float brightness, final float contrast) {
		this(TYPE_BRIGHT_CONTRAST, brightness, contrast);
	}

	protected BrightnessContrast(final String type, final float brightness, final float contrast) {
		super(type);
		setBrightness(brightness);
		setContrast(contrast);
	}

	public void setBrightness(final float brightness) {
		this.brightness = brightness;
	}

	public void setContrast(final float contrast) {
		this.contrast = contrast;
		contFactor = ColorUtil.contrastFactor(contrast);
	}

	@Override
	public boolean filter(final int[] rgba) {
		boolean ret = false;
		for (int i = 0; i < 3; i++) {
			double v = rgba[i] / 255.0;
			v = (v + brightness - 0.5) * contFactor + 0.5;
			final int val = ColorUtil.clipInt(v * 255, 0, 255);
			if (val != rgba[i]) {
				rgba[i] = val;
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public int getNumComponents(final int srcNumComponents) {
		return srcNumComponents;
	}

	@Override
	public void setState(final State state) {
		setBrightness((float)state.getDouble(STKEY_BRIGHTNESS, 0));
		setContrast((float)state.getDouble(STKEY_CONTRAST, 0));
	}

	@Override
	public State getState() {
		final State ret = super.getState();
		ret.putDouble(STKEY_BRIGHTNESS, brightness);
		ret.putDouble(STKEY_CONTRAST, contrast);
		return ret;
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type);
		out.append(brightness);
		out.append(contrast);
	}

}
