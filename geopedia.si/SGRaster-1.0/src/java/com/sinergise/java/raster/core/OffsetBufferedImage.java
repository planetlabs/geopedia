package com.sinergise.java.raster.core;

import java.awt.image.BufferedImage;

public class OffsetBufferedImage {
	public final BufferedImage bi;
	public final int offX;
	public final int offY;
	
	public OffsetBufferedImage(BufferedImage bi) {
		this(bi, 0, 0);
	}

	public OffsetBufferedImage(BufferedImage bi, int offX, int offY) {
		this.bi = bi;
		this.offX = offX;
		this.offY = offY;
	}
}
