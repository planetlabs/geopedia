package com.sinergise.common.raster.index;

import com.sinergise.common.raster.core.TilesIndex;

public abstract class AbstractQuadIdx implements TilesIndex {
	public static final int factor(int depth) {
		return 1 << (depth - 1);
	}
	
	public static int childIdx(int row, int col, final int factor) {
		return 2 * (row / factor) + (col / factor);
	}
	
	protected int maxLevel;
	protected int rootLevel;
	
	public AbstractQuadIdx(int minIndexedLevel, int maxIndexedLevel) {
		this.rootLevel = minIndexedLevel;
		this.maxLevel = maxIndexedLevel;
	}
	
	@Override
	public int getMaxIndexedLevel() {
		return maxLevel;
	}
	@Override
	public int getMinIndexedLevel() {
		return rootLevel;
	}
	@Override
	public boolean hasTile(int zoomLevel, int row, int col) {
		assert zoomLevel >= rootLevel && zoomLevel <= maxLevel : zoomLevel + " " + row +" "+col;
		return hasTileRelativeToRootLevel(zoomLevel - rootLevel, row, col);
	}
	
	protected abstract boolean hasTileRelativeToRootLevel(int depth, int row, int col);
	
	private int maxIndex(int zoomLevel) {
		return (1 << (zoomLevel - rootLevel)) - 1;
	}
	@Override
	public int maxTileColumn(int zoomLevel) {
		return maxIndex(zoomLevel);
	}
	@Override
	public int maxTileRow(int zoomLevel) {
		return maxIndex(zoomLevel);
	}

}
