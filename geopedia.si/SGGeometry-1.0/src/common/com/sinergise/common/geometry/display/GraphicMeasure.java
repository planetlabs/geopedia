package com.sinergise.common.geometry.display;

import java.io.Serializable;


public class GraphicMeasure implements Serializable {
	private static final long serialVersionUID = -385180091273406518L;
	public static final GraphicMeasure ZERO = new GraphicMeasure(0, null);

	private static enum MeasureKind {WORLD, DISPLAY, PIXEL}
	private static double POSTSCRIPT_PT_IN_METRES = (254.0 / 720000);
	private static double POSTSCRIPT_PT_IN_MICRONS = (25400 / 72);
	
	/**
	 * @param sizeInDisplayPoints size in postscript points (1/72 of an inch)
	 * @return
	 */
	public static GraphicMeasure fixedDisplaySize(double sizeInDisplayPoints) {
		return new GraphicMeasure(sizeInDisplayPoints * POSTSCRIPT_PT_IN_METRES, MeasureKind.DISPLAY);
	}

	public static GraphicMeasure fixedWorldSize(double sizeInWorldUnits) {
		return new GraphicMeasure(sizeInWorldUnits, MeasureKind.WORLD);
	}
	
	/**
	 * Note that printing a pixel-defined measure on 1500 DPI will give much different results than displaying it on screen
	 * 
	 * @param displayPixels
	 * @return a measure relative to the pixel grid in the display medium. 
	 */
	public static GraphicMeasure fixedPixels(double sizeInPixels) {
		return new GraphicMeasure(sizeInPixels, MeasureKind.PIXEL);
	}
	
	private double amount;
	private MeasureKind kind;
	
	private GraphicMeasure() {
		//GWT serialization
	}
	
	private GraphicMeasure(double amount, MeasureKind kind) {
		this.amount = amount;
		this.kind = kind;
	}
	
	public double sizeInWorldUnits(DisplayCoordinateAdapter dca) {
		if (amount == 0) return 0;
		switch (kind) {
			case WORLD: return amount;
			case PIXEL: return dca.worldFromPix.length(amount);
			case DISPLAY: return dca.worldFromDisp.length(amount);
		}
		throw new IllegalStateException("Unknown GraphicMeasure kind.");
	}
	
	public double sizeInPixels(DisplayCoordinateAdapter dca) {
		if (amount == 0) return 0;
		switch (kind) {
			case WORLD: return dca.pixFromWorld.length(amount);
			case PIXEL: return amount;
			case DISPLAY: return amount * 1000000 / dca.pixSizeInMicrons;
		}
		throw new IllegalStateException("Unknown GraphicMeasure kind.");
	}
	
	/**
	 * @param dca
	 * @return size (length) in postscript points (1/72 of an inch)
	 */
	public double sizeInDisplayPoints(DisplayCoordinateAdapter dca) {
		if (amount == 0) return 0;
		switch (kind) {
			case WORLD: return dca.dispFromWorld.length(amount) / POSTSCRIPT_PT_IN_METRES;
			case PIXEL: return amount * dca.pixSizeInMicrons / POSTSCRIPT_PT_IN_MICRONS;
			case DISPLAY: return amount / POSTSCRIPT_PT_IN_METRES;
		}
		throw new IllegalStateException("Unknown GraphicMeasure kind.");
	}
}
