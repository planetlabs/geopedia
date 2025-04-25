package com.sinergise.common.geometry.tiles;

import java.util.Iterator;

import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.geom.RectSideOffsetsI;
import com.sinergise.common.util.math.Interval;

public class TiledRegion implements Iterable<TiledRegion.PartOfTile>{
	public static class PartOfTile {
		EnvelopeI pixelsInTile;
		PointI tileIndex;
		int level;
		
		public PartOfTile(int level, PointI tileIndex, EnvelopeI pixelsInTile) {
			this.level = level;
			this.tileIndex = tileIndex;
			this.pixelsInTile = pixelsInTile;
		}
		
		public EnvelopeI getPixelsInTile() {
			return pixelsInTile;
		}
		
		public PointI getTileIndex() {
			return tileIndex;
		}
		
		public Envelope getDataEnvelope(TiledCRS cs) {
			Envelope tileBnds = cs.tileWorldBounds(level, tileIndex.x, tileIndex.y);
			double pxSize = cs.zoomLevels.worldPerPix(level);

			double minX = cs.colSign.tileMinWorld(pixelsInTile.minX(), tileBnds.getMinX(), pxSize);
			double maxX = cs.colSign.tileMinWorld(pixelsInTile.maxX()+1, tileBnds.getMinX(), pxSize);

			double minY = cs.rowSign.tileMinWorld(pixelsInTile.minY(), tileBnds.getMinY(), pxSize);
			double maxY = cs.rowSign.tileMinWorld(pixelsInTile.maxY()+1, tileBnds.getMinY(), pxSize);
			
			return new Envelope(minX, minY, maxX, maxY);
		}
		
		public int getLevel() {
			return level;
		}
	}

	static class PartOfTileIterator implements Iterator<PartOfTile> {
		private int idx = 0;
	
		private final TiledRegion region;
		private final Interval<Integer> wholeTileX;
		private final Interval<Integer> wholeTileY;
		private final int tilesW;
	
		private int tilesH;
		
		public PartOfTileIterator(TiledRegion region) {
			this.region = region;
			wholeTileX = Interval.closed(0, region.normalTileSize.w()-1);
			wholeTileY = Interval.closed(0, region.normalTileSize.h()-1);
			tilesW = region.tiles.getWidth();
			tilesH = region.tiles.getHeight();
		}
		
		@Override
		public PartOfTile next() {
			int col = idx % tilesW;
			int tileCol = region.tiles.minX() + col;
	
			int row = idx / tilesW;
			int tileRow = region.tiles.minY() + row;
			
			idx++;
			
			return new PartOfTile(region.level, new PointI(tileCol, tileRow), EnvelopeI.createClosed(intervalForX(col), intervalForY(row)));
		}
	
		private Interval<Integer> intervalForX(int col) {
			Interval<Integer> pixCols = wholeTileX;
			if (col == 0) {
				int maxX = region.normalTileSize.w() - 1;
				pixCols = pixCols.intersection(Interval.closed(maxX - region.bndTilePixels.l(), maxX));
			}
			if (col == tilesW - 1) {
				pixCols = pixCols.intersection(Interval.closed(0, region.bndTilePixels.r()-1));
			}
			return pixCols;
		}
		
		private Interval<Integer> intervalForY(int row) {
			Interval<Integer> pixRows = wholeTileY;
			if (row == 0) {
				int maxY = region.normalTileSize.h() - 1;
				pixRows = pixRows.intersection(Interval.closed(maxY - region.bndTilePixels.b(), maxY));
			}
			if (row == tilesH - 1) {
				pixRows = pixRows.intersection(Interval.closed(0, region.bndTilePixels.t()-1));
			}
			return pixRows;
		}
		
		@Override
		public boolean hasNext() {
			return idx < region.tiles.getArea();
		}
	
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public final EnvelopeI tiles;
	public final RectSideOffsetsI bndTilePixels;
	public int level;
	public DimI normalTileSize;

	public TiledRegion() {
		this(-1, DimI.EMPTY, EnvelopeI.EMPTY, RectSideOffsetsI.EMPTY);
	}

	public TiledRegion(int level, DimI tileSize, EnvelopeI env, RectSideOffsetsI bndTilePixels) {
		this.level = level;
		this.tiles = env;
		this.normalTileSize = tileSize;
		this.bndTilePixels = bndTilePixels;
	}

	public static TiledRegion createEmpty() {
		return new TiledRegion();
	}
	
	@Override
	public Iterator<TiledRegion.PartOfTile> iterator() {
		return new TiledRegion.PartOfTileIterator(this);
	}
}