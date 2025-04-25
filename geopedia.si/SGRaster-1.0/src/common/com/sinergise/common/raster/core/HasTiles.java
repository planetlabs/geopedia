package com.sinergise.common.raster.core;


public interface HasTiles {
	public interface SingleLevel {
		boolean hasTile(int row, int col);
	}

	boolean hasTile(int zoomLevel, int row, int col);
	
}
