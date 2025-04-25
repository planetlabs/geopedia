package com.sinergise.java.util.math;

import java.awt.AlphaComposite;
import java.awt.Composite;

public class BlendMode {
	public static final BlendMode SRC = new BlendMode(AlphaComposite.Src);
	private final Composite       awtC;
	
	public BlendMode(final Composite awtC) {
		this.awtC = awtC;
	}
	
	public Composite getAwtComposite() {
		return awtC;
	}
	
	public static final int[] srcOver(final int[] srcRGBA, final int[] tgtRGBA) {
		final double sa = srcRGBA[3] / 255.0;
		final double ta = tgtRGBA[3] / 255.0;
		final double outA = sa + ta - sa * ta;
		final double st = (1.0 - sa) * ta;
		return new int[]{(int)Math.round((srcRGBA[0] * sa + tgtRGBA[0] * st) / outA), (int)Math.round((srcRGBA[1] * sa + tgtRGBA[1] * st) / outA),
		        (int)Math.round((srcRGBA[2] * sa + tgtRGBA[2] * st) / outA), (int)Math.round((255.0 * outA))};
	}
}
