package com.sinergise.java.raster.pyramid;

import java.io.File;
import java.io.IOException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.java.raster.core.OffsetBufferedImage;


public interface TileProviderJava
{
	public static abstract class AbstractTileProvider implements TileProviderJava {

		protected TiledCRS cs;

		public AbstractTileProvider(TiledCRS cs) {
			this.cs = cs;
		}

		@Override
		public boolean renderTile(OffsetBufferedImage tgt, int scale, int x, int y) throws IOException {
			if (!hasTile(scale, x, y)) {
				return false;
			}
			OffsetBufferedImage obi = getTile(scale, x, y);
			if (obi==null) {
				return false;
			}
			tgt.bi.createGraphics().drawImage(obi.bi, obi.offX - tgt.offX, obi.offY - tgt.offY, null);
			return true;
		}

		@Override
		public boolean hasData(int scale, int col, int row) throws IOException {
			return hasTile(scale, col, row);
		}

		@Override
		public boolean isOpaque(int scale, int x, int y) throws IOException {
			return hasTile(scale, x, y);
		}

		@Override
		public int getMaxLevelId() {
			return cs.getMaxLevelId();
		}

		@Override
		public TiledCRS getTiledCRS() {
			return cs;
		}

		@Override
		public long estimateNumTiles() throws IOException {
			int level = cs.getMinLevelId()+1;
			int row = 0;
			int col = 0;
			int cnt = 0;
			do {
				col = 2*col;
				row = 2*row;
				cnt = 0;
				if (hasTile(level, col, row)) cnt++;
				if (hasTile(level, col+1, row)) cnt++;
				if (hasTile(level, col, row+1)) cnt++;
				if (hasTile(level, col+1, row+1)) cnt++;
				if (cnt == 0) throw new IllegalStateException("No tiles at level " + level);
				if (cnt > 1) break;
				level++;
			} while (level < getMaxLevelId());
			return cnt * (1 << (getMaxLevelId()-level));
		}

		public abstract void copyTile(int scale, int x, int y, File otherBase) throws IOException;
	}
	
	public boolean renderTile(OffsetBufferedImage tileImg, int scale, int x, int y) throws IOException;
	
	public OffsetBufferedImage getTile(int scale, int x, int y) throws IOException;
	
    public boolean hasTile(int scale, int x, int y) throws IOException;

    public boolean isOpaque(int scale, int x, int y) throws IOException;
    
    public int getMaxLevelId();

    /**
     * @param scale
     * @param x
     * @param y
     * @return true iff this provider has any tiles in the sub-pyramid defined by the provided tile
     * @throws IOException
     */
	public boolean hasData(int scale, int x, int y) throws IOException;
	
	public TiledCRS getTiledCRS();

	public long estimateNumTiles() throws IOException; 
}
