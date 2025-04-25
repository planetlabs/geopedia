package com.sinergise.common.raster.core;

import java.util.Arrays;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.collections.BitSet2D;
import com.sinergise.common.util.geom.EnvelopeI;


public class PyramidIndex implements TilesIndex.Mutable {
	public static final int DEFAULT_MAX_TILES_IN_INDEX = 67108864; // 2^26 ~ 12 levels; 8.3 MB
	
	public static final TilesIndex createDefault(TiledCRS crs) {
		return createDefault(crs, DEFAULT_MAX_TILES_IN_INDEX);
	}
	
	public static final TilesIndex createDefault(TiledCRS crs, int maxTilesInIndex) {
		int lOff = crs.getMinLevelId();
		int lMax = lOff - 1;
		int cnt = 1;
		while (lMax < crs.getMaxLevelId() && cnt <= maxTilesInIndex) {
			cnt = crs.tileMatrixWidth(lMax + 1) * crs.tileMatrixHeight(lMax + 1);
			lMax++;
		}
		return new PyramidIndex(crs, lMax);
	}
	
	public static final PyramidIndex createWithData(int minTilesIndex, int maxTilesIndex, BitSet2D[] data) {
		return new PyramidIndex(minTilesIndex, maxTilesIndex, data);
	}
	
	BitSet2D[] indxs;
	final int lOff;
	int lMax;
	
	public PyramidIndex(TiledCRS crs, int maxIndexedLevel) {
		this(crs, crs.getMinLevelId(), maxIndexedLevel);
	}
	
	public PyramidIndex(TiledCRS crs, int minIndexedLevel, int maxIndexedLevel) {
		this(minIndexedLevel, maxIndexedLevel, new BitSet2D[maxIndexedLevel - minIndexedLevel + 1]);
		constructBits(crs);
	}
	
	protected PyramidIndex(int minIndexedLevel, int maxIndexedLevel, BitSet2D[] data) {
		this.lOff = minIndexedLevel;
		this.lMax = maxIndexedLevel;
		this.indxs = data;
	}
	
	protected void constructBits(TiledCRS tCRS) {
		for (int i = 0; i < indxs.length; i++) {
			indxs[i] = new BitSet2D(tCRS.tileMatrixWidth(lOff + i), tCRS.tileMatrixHeight(lOff + i));
		}
	}
	
	@Override
	public void set(int zoomLevel, int row, int col) {
		BitSet2D lvl = indxs[zoomLevel - lOff];
		EnvelopeI lvlEnv = lvl.getDataEnvelope();
		if (!lvlEnv.contains(row, col)) {
			BitSet2D full = new BitSet2D(lvl.getWidth(), lvl.getHeight());
			full.setFrom(lvl);
			lvl = full;
			indxs[zoomLevel - lOff] = full;
		}
		lvl.set(row, col);
	}
	
	@Override
	public int maxTileRow(int zoomLevel) {
		return indxs[zoomLevel - lOff].getHeight()-1;
	}

	@Override
	public int maxTileColumn(int zoomLevel) {
		return indxs[zoomLevel - lOff].getWidth()-1;
	}
	
	@Override
	public int getMaxIndexedLevel() {
		return lMax;
	}
	
	@Override
	public int getMinIndexedLevel() {
		return lOff;
	}
	
	@Override
	public boolean hasTile(int zoomLevel, int row, int col) {
		return indxs[zoomLevel - lOff].isSet(row, col);
	}
	
	@Override
	public void clear(int zoomLevel, int row, int col) {
		indxs[zoomLevel - lOff].clear(row, col);
	}
	
	public void clearAll() {
		for (int i = 0; i < indxs.length; i++) {
			indxs[i].clearAll();
		}
	}
	
	public BitSet2D bitSetForLevel(int zoomLevel) {
		return indxs[zoomLevel - lOff];
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(indxs);
		result = prime * result + lMax;
		result = prime * result + lOff;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PyramidIndex other = (PyramidIndex) obj;
		if (!ArrayUtil.equals(indxs, other.indxs)) return false;
		if (lMax != other.lMax) return false;
		if (lOff != other.lOff) return false;
		return true;
	}

	@Override
	public void expandTo(int maxLevel) {
		if (maxLevel == lMax) {
			return;
		}
		if (maxLevel<lMax) {
			throw new IllegalArgumentException("Cannot expand to smaller range than already existing");
		}
		BitSet2D[] newIndxs = new BitSet2D[maxLevel-lOff+1];
		ArrayUtil.arraycopy(indxs, 0, newIndxs, 0, indxs.length);
		for (int i = indxs.length; i < newIndxs.length; i++) {
			int size = 1 << i;
			newIndxs[i] = new BitSet2D(size, size);
		}
		indxs = newIndxs;
		lMax = maxLevel;
	}

	public void pack() {
		if (indxs == null) return;
		for (int i = 0; i < indxs.length; i++) {
			indxs[i] = indxs[i].pack();
		}
	}
}
