package com.sinergise.common.raster.dataraster.service.average;

import java.io.Serializable;


public class DemDataAverage implements Serializable {

	private static final long serialVersionUID = 1642695852502429117L;
	
	private double minHeight;
	private double maxHeight;
	private double avgHeight;

	private double minSlopeDegrees;
	private double maxSlopeDegrees;
	private double avgSlopeDegrees;
	
	private double avgAzimuthDegrees;
	
	@Deprecated /** Serialization only */
	protected DemDataAverage() {}
	
	public DemDataAverage(double avgHeight, double avgSlopeDegrees, double avgAzimuthDegrees) {
		this.avgHeight = avgHeight;
		this.avgSlopeDegrees = avgSlopeDegrees;
		this.avgAzimuthDegrees = avgAzimuthDegrees;
		if (avgSlopeDegrees == 0) {
			this.avgAzimuthDegrees = 0;
		}
	}
	
	public double getAvgHeight() {
		return avgHeight;
	}
	
	public double getAvgSlope() {
		return avgSlopeDegrees;
	}
	
	public double getAvgAzimuth() {
		return avgAzimuthDegrees;
	}
	
	public boolean hasData() {
		return !Double.isNaN(avgHeight) && !Double.isNaN(avgSlopeDegrees) && !Double.isNaN(avgAzimuthDegrees);
	}

	public static DemDataAverage createEmpty() {
		return new DemDataAverage(Double.NaN, Double.NaN, Double.NaN);
	}

	public void setHeightMinMax(double min, double max) {
		minHeight = min;
		maxHeight = max;
	}

	public void setSlopeMinMax(double min, double max) {
		minSlopeDegrees = min;
		maxSlopeDegrees = max;
	}
	
	public double getMinHeight() {
		return minHeight;
	}
	
	public double getMaxHeight() {
		return maxHeight;
	}
	
	public double getMinSlopeDegrees() {
		return minSlopeDegrees;
	}
	
	public double getMaxSlopeDegrees() {
		return maxSlopeDegrees;
	}
	
}
