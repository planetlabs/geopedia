package com.sinergise.java.raster.colorfilter;

import com.sinergise.java.util.math.ColorMap;
import com.sinergise.java.util.state.State;


public class BandToColorMap extends ColorFilter1D {
	private static final String	TYPE_BAND_TO_CMAP	= "BandToColorMap";
	ColorMap					colorMap;

	public BandToColorMap() {
		super(TYPE_BAND_TO_CMAP);
	}

	@Override
	public int getNumComponents(final int inputSampSize) {
		return colorMap.hasAlpha() ? 4 : 3;
	}

	@Override
	public boolean filterGray(final int a, final int[] dstRGBA) {
		colorMap.color(a, dstRGBA);
		return true;
	}

	@Override
	public State getState() {
		final State ret = super.getState();

		if (colorMap != null) {
			final State cst = colorMap.getState();
			ret.putState("colorMap", cst);
		}
		return ret;
	}

	@Override
	public void setState(final State state) {
		final State cmSt = state.getState("colorMap");
		if (cmSt != null) {
			colorMap = new ColorMap();
			colorMap.setState(cmSt);
		}
		super.setState(state);
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		super.appendIdentifier(out);
		colorMap.appendIdentifier(out);
	}
	
	@Override
	protected void copyInto(ColorFilter copy) {
		((BandToColorMap)copy).colorMap = colorMap.clone();
	}
}
