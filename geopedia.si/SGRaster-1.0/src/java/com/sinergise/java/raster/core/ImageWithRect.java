package com.sinergise.java.raster.core;

import java.io.Closeable;

import com.sinergise.common.util.geom.EnvelopeI;


public class ImageWithRect implements Closeable {
	public SGRenderedImage image;
	public EnvelopeI positionRect;
	
	public ImageWithRect(SGRenderedImage image, EnvelopeI posRect) {
		this.image = image;
		this.positionRect = posRect;
	}

	public double getFactY() {
		return positionRect.getHeight()/getSourceHeight();
	}
	private double getSourceHeight() {
		return image.getHeight();
	}

	public double getFactX() {
		return positionRect.getWidth()/getSourceWidth();
	}

	private double getSourceWidth() {
		return image.getWidth();
	}

	public int getOffX() {
		return positionRect.minX();
	}

	public int getOffY() {
		return positionRect.minY();
	}

	public void dispose() {
		RasterIO.dispose(image);
	}
	
	@Override
	public void close() {
		dispose();
	}
	
	public boolean origSizeEquals(int w, int h) {
		return image.getWidth() == w && image.getHeight() == h;
	}

	public int getWidth() {
		return positionRect.getWidth();
	}

	public int getHeight() {
		return positionRect.getHeight();
	}
}
