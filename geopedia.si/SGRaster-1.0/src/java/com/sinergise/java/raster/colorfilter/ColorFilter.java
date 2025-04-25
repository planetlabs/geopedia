/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.sinergise.common.util.lang.SGCloneable;
import com.sinergise.java.raster.core.SGRenderedImage;
import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateOriginator;
import com.sinergise.java.util.state.impl.DefaultState;


public abstract class ColorFilter implements SGCloneable, StateOriginator {
	public static final Color					C_TRANSPARENT		= new Color(0, 0, 0, 0);
	public static final int[]					WHITE				= new int[]{255, 255, 255, 255};
	public static final int[]					BLACK				= new int[]{0, 0, 0, 255};
	public static final int[]					TRANSPARENT			= new int[]{0, 0, 0, 0};

	protected static final String				STKEY_TYPE			= "type";
	private static HashMap<String, ColorFilter>	filterPrototypes;

	protected int								inputSampleSize		= -1;
	protected int								outputSampleSize	= -1;
	protected boolean							inputHasColor		= false;
	protected boolean							inputHasAlpha		= false;
	protected boolean							enabled				= true;

	protected final ThreadLocal<int[]>			tmp					= new ThreadLocal<int[]>() {
																		@Override
																		protected int[] initialValue() {
																			return new int[4];
																		}
																	};

	public static ColorFilter createFilter(final State st) {
		
		final String type = st.getString(ColorFilter.STKEY_TYPE, null);
		
		if (type == null) throw new IllegalArgumentException("Filter type not specified in state");
		if (filterPrototypes == null) throw new IllegalStateException("No filter prototypes found");

		final ColorFilter proto = filterPrototypes.get(type);
		if (proto == null) return null;

		final ColorFilter ret = proto.clone();
		ret.setState(st);
		return ret;
	}

	public void setInputSampleSize(final int inputSampleSize) {
		this.inputSampleSize = inputSampleSize;
		this.outputSampleSize = getNumComponents(inputSampleSize);
		inputHasColor = inputSampleSize >= 3;
		inputHasAlpha = inputSampleSize % 2 == 0;
	}

	public static void registerColorFilter(final ColorFilter prototype) {
		if (filterPrototypes == null) {
			filterPrototypes = new HashMap<String, ColorFilter>();
		}
		filterPrototypes.put(prototype.type, prototype);
	}

	static {
		registerColorFilter(new BandRotate());
		registerColorFilter(new BrightnessContrast(0, 0));
		registerColorFilter(new ColorReplace());
		registerColorFilter(new CompositeFilter(null));
		registerColorFilter(new ExclusiveCompositeFilter(null));
		registerColorFilter(new FadeCorrection(0));
		registerColorFilter(new BandTo2Colors(ColorFilter1D.BAND_GRAY, null, null));
		registerColorFilter(new BandToColorMap());
		registerColorFilter(new SubtractWhiteFilter());
		registerColorFilter(new SubtractBgFilter());
	}

	public static SGRenderedImage transform(final SGRenderedImage img, final ColorFilter filter) {
		//        if (IndexedColorFilterer.canTransform(img)) return new IndexedColorFilterer(img, filter);
		return transformGeneric(img, filter);
	}

	public static SGRenderedImage transformGeneric(final SGRenderedImage img, final ColorFilter filter) {
		if (ColorFiltererRImg.canHandle(img, filter)) {
			return new ColorFiltererRImg(img, filter);
		}
		return new ColorFilterer(img.unwrap(), filter);
	}

	protected final String	type;

	protected ColorFilter(final String uniqueType) {
		this.type = uniqueType;
	}

	@Override
	public ColorFilter clone() {
		try {
			ColorFilter ret = (ColorFilter)super.clone();
			copyInto(ret);
			return ret;
		} catch(final Exception e) {
			throw new RuntimeException("Clone failed.", e);
		}
	}
	
	protected void copyInto(ColorFilter copy) {
		copy.enabled = enabled;
		copy.inputHasAlpha = inputHasAlpha;
		copy.inputHasColor = inputHasColor;
		copy.inputSampleSize = inputSampleSize;
		copy.outputSampleSize = outputSampleSize;
		
	}

	public boolean filterGray(final int srcGray, final int[] dstValue) {
		switch (inputSampleSize) {
			case 4:
				dstValue[3] = 255; //$FALL-THROUGH$
			case 3:
				dstValue[2] = srcGray;
				dstValue[1] = srcGray;
				dstValue[0] = srcGray;
				break;
			case 2: // Gray+alpha
				dstValue[1] = 255; //$FALL-THROUGH$
			case 1:
				dstValue[0] = srcGray;
				break;
		}
		return filter(dstValue);
	}

	public boolean filterBinary(final boolean white, final int[] dstValue) {
		final int val = white ? 255 : 0;
		switch (inputSampleSize) {
			case 4:
				dstValue[3] = 255; //$FALL-THROUGH$
			case 3:
				dstValue[2] = val;
				dstValue[1] = val;
				dstValue[0] = val;
				break;
			case 2: // Grey+alpha
				dstValue[1] = 255; //$FALL-THROUGH$
			case 1:
				dstValue[0] = val;
				break;
		}
		return filter(dstValue);
	}

	public abstract int getNumComponents(int inputSampSize);

	public abstract boolean filter(int[] rgba);

	public boolean filterBytes(final byte[] data, final int off, final byte[] outData, final int outOff, final int[] byteOrder) {
		final int[] t = this.tmp.get();
		for (int i = 0; i < inputSampleSize; i++) {
			int idx = i;
			if (byteOrder != null) idx = byteOrder[i];
			t[idx] = 0xFF & data[off + i];
		}
		if (!filter(t)) {
			return false;
		}
		for (int i = 0; i < outputSampleSize; i++) {
			int idx = i;
			if (byteOrder != null) idx = byteOrder[i];
			outData[outOff + i] = (byte)t[idx];
		}
		return true;
	}

	@Override
	public State getState() {
		final State ret = new DefaultState();
		ret.putString(STKEY_TYPE, type);
		ret.putBoolean("enabled", enabled);
		return ret;
	}

	@Override
	public void setState(final State state) {
		enabled = state.getBoolean("enabled", true);
	}

	/**
	 * @param src ARGB integer
	 * @return
	 */
	public int filterInt(final int src) {
		final int[] t = this.tmp.get();
		t[0] = src >>> 16 & 0xFF;
		t[1] = src >>> 8 & 0xFF;
		t[2] = src & 0xFF;
		t[3] = src >>> 24 & 0xFF;

		if (!filter(t)) return src;
		
		return t[2] + (t[1] << 8) + (t[0] << 16) + (t[3] << 24);
	}

	public abstract void appendIdentifier(StringBuffer out);

	public static ColorFilterer transformInPlace(final BufferedImage bi, final ColorFilter filterToApply) {
		return new ColorFilterer(bi, filterToApply, ColorFilterer.getCompatibleBIType(bi, filterToApply));
	}


	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}


}
