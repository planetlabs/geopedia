package com.sinergise.common.raster.index;

import java.awt.image.DataBuffer;


public class PackedQuadIdx extends AbstractQuadIdx {
	static final int ROOT_ID = 2;
	static final int EMPTY_ID = 0;
	static final int FULL_ID = 1;

	DataBuffer db;
	
	public PackedQuadIdx(int minLevel, int maxLevel, DataBuffer data) {
		super(minLevel, maxLevel);
		this.db = data;
	}
	
	@Override
	protected boolean hasTileRelativeToRootLevel(int depth, int row, int col) {
		return hasTileRelativeTo(ROOT_ID, depth, row, col);
	}

	private boolean hasTileRelativeTo(int nodeId, int depth, int row, int col) {
		if (nodeId == EMPTY_ID) {
			return false;
		}
		if (nodeId == FULL_ID) {
			return true;
		}
		if (depth == 0) {
			return true;
		}
		int factor = factor(depth);
		return hasTileRelativeTo(getChildId(nodeId, childIdx(row, col, factor)), depth-1, row % factor, col % factor);
	}

	private int getChildId(int nodeId, int childIdx) {
		return db.getElem(getNodeStart(nodeId) + childIdx);
	}

	public DataBuffer getData() {
		return db;
	}
	
	public static int getNodeStart(int nodeId) {
		return 4*(nodeId - 2) + 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((db == null) ? 0 : db.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PackedQuadIdx)) {
			return false;
		}
		PackedQuadIdx other = (PackedQuadIdx)obj;
		if (db.getSize() != other.db.getSize()) {
			return false;
		}
		for (int i = 0; i < db.getSize(); i++) {
			if (db.getElem(i) != other.db.getElem(i)) {
				return false;
			}
		}
		return true;
	}
	
	
}
