package com.sinergise.common.raster.core;


public interface TilesIndex extends HasTiles {
	public static interface Mutable extends TilesIndex {
		void clear(int zoomLevel, int row, int col);
		void expandTo(int maxLevel);
		void set(int zoomLevel, int row, int col);
	}
	
	public static interface SingleLevel {
		int maxTileRow();
		int maxTileColumn();
		boolean hasTile(int row, int col);
	}
	
	public static interface SingleLevelMutable extends SingleLevel {
		void clear(int row, int col);
		void set(int row, int col);
	}
	
	public static class DefaultLevelView implements SingleLevelMutable {
		private final int level;
		private final TilesIndex owner;
		
		public DefaultLevelView(TilesIndex owner, int level) {
			this.owner = owner;
			this.level = level;
		}
		
		@Override
		public void clear(int row, int col) {
			((Mutable)owner).clear(level, row, col);
		}
		
		@Override
		public boolean hasTile(int row, int col) {
			return owner.hasTile(level, row, col);
		}
		
		@Override
		public void set(int row, int col) {
			((Mutable)owner).set(level, row, col);
		}
		
		@Override
		public int maxTileColumn() {
			return owner.maxTileColumn(level);
		}
		
		@Override
		public int maxTileRow() {
			return owner.maxTileRow(level);
		}
	}

	int getMinIndexedLevel();

	int getMaxIndexedLevel();

	int maxTileRow(int zoomLevel);

	int maxTileColumn(int zoomLevel);
}
