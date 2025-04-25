package com.sinergise.common.geometry.service.util;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class GeomOpRequest implements Serializable {

	private double gridSize = 0;
	
	public void setGridSize(double gridSize) {
		this.gridSize = Math.abs(gridSize);
	}
	
	public double getGridSize() {
		return gridSize;
	}
	
	public double getScale() {
		if (gridSize > 0) {
			return 1d/gridSize;
		}
		return 0;
	}
	
}
