package com.sinergise.java.raster.pyramid;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.TileSpec;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.index.QuadIdxBuilder;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.java.raster.io.PyramidIndexIO;
import com.sinergise.java.raster.misc.UpdateDOF;
import com.sinergise.java.raster.pyramid.RasterProcessor.ImageOutput;

public class DeltaWriter {
	private static Tile DELTA_EMPTY = new Tile() {
		@Override
		public BufferedImage getData() {
			return null;
		}

		@Override
		public boolean isDelta() {
			return true;
		}
	};

	// In original version there was a tile that got removed
	private static Tile EMPTY = new Tile() {
		@Override
		public BufferedImage getData() {
			return null;
		}
	};


	private abstract static class Tile {
		public boolean isDelta() {
			return false;
		}

		public abstract BufferedImage getData();

		public boolean isWaitingForData() {
			return false;
		}
	}

	private static class DataTile extends Tile {
		protected BufferedImage data;

		public DataTile(BufferedImage tile) {
			this.data = tile;
		}

		@Override
		public BufferedImage getData() {
			if (data == null) {
				throw new IllegalStateException("Should have data");
			}
			return data;
		}

		@Override
		public boolean isWaitingForData() {
			return data == null;
		}
	}

	private static class DeltaTile extends DataTile {
		public DeltaTile(BufferedImage tile) {
			super(tile);
		}

		@Override
		public boolean isDelta() {
			return true;
		}
	}

	private static class WholeTile extends DataTile {
		public WholeTile() {
			super(null);
		}

		public WholeTile(BufferedImage data) {
			super(data);
		}

		public void dataArrived(BufferedImage newData) {
			if (!isWaitingForData()) {
				throw new IllegalStateException("Can't set data on a tile that already has it");
			}
			this.data = newData;
		}

		@Override
		public BufferedImage getData() {
			if (data == null) {
				throw new IllegalStateException("Waiting for data! can't use...");
			}
			return super.getData();
		}
	}

	private static class PendingTile {
		Tile[] children = new Tile[4];
		int childrenMask = 0;

		public void addChild(int i, Tile tile) {
			childrenMask |= (1 << i);
			children[i] = tile;
		}

		public boolean hasAllData() {
			if (childrenMask != 0xF) {
				return false;
			}
			for (Tile c : children) {
				if (c.isWaitingForData()) {
					return false;
				}
			}
			return true;
		}
	}

	private File deltaDir;
	private File deltaCombinedDir;
	private File deltaBaseDir = null;

	private TiledCRS cs;
	private TileCache tcLarge;
	private TileCache tcSmall;

	private QuadIdxBuilder index;
	private QuadIdxBuilder indexCombined;
	private HashMap<TileSpec, PendingTile> pendingTiles = new HashMap<TileSpec, DeltaWriter.PendingTile>();
	private HashMap<TileSpec, PendingTile> pendingCombinedTiles = new HashMap<TileSpec, DeltaWriter.PendingTile>();
	private HashMap<TileSpec, WholeTile> waitingForData = new HashMap<TileSpec, DeltaWriter.WholeTile>();

	private ImageOutput output;
	private ImageOutput outputCombined;
	private RasterSubsamplingStrategy subsampler;

	private TilesIndex oldDeltaIndex;

	private RasterProcessor rp;


	public DeltaWriter(RasterProcessor rp, File deltaDir, Appendable console) {
		this.deltaDir = deltaDir;
		this.cs = rp.cs;
		this.tcLarge = rp.tilesCacheLarge;
		this.tcSmall = rp.tilesCacheSmall;
		this.subsampler = rp.subsampler;
		index = new QuadIdxBuilder(cs.getMinLevelId(), cs.getMaxLevelId());
		this.output = new ImageOutput("DeltaWriter", UpdateDOF.createDirsForTypes(deltaDir, rp.out.outTypes, true),
			rp.out.outTypes, cs, console);
	}

	public DeltaWriter(RasterProcessor rp, File deltaDir, File deltaCombinedDir, File deltaBaseDir, Appendable console) {
		this.rp = rp;
		this.deltaDir = deltaDir;
		this.deltaCombinedDir = deltaCombinedDir;
		this.deltaBaseDir = deltaBaseDir;
		this.cs = rp.cs;
		rp.checkTileCaches();
		this.tcLarge = rp.tilesCacheLarge;
		this.tcSmall = rp.tilesCacheSmall;
		this.subsampler = rp.subsampler;
		index = new QuadIdxBuilder(cs.getMinLevelId(), cs.getMaxLevelId());
		this.output = new ImageOutput("DeltaWriter", UpdateDOF.createDirsForTypes(deltaDir, rp.out.outTypes, true),
			rp.out.outTypes, cs, console);
		if (deltaBaseDir != null) {
			indexCombined = new QuadIdxBuilder(cs.getMinLevelId(), cs.getMaxLevelId());
			try {
				File oldDeltaIndexFile = PyramidIndexIO.getIndexFile(deltaBaseDir);
				oldDeltaIndex = PyramidIndexIO.loadIndexFile(oldDeltaIndexFile);
				this.outputCombined = new ImageOutput("DeltaWriter", UpdateDOF.createDirsForTypes(deltaCombinedDir,
					rp.out.outTypes, true), rp.out.outTypes, cs, console);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}


	public synchronized void pushWholeTileUsed(int scale, int x, int y, BufferedImage tile) {
		index.setFullPyramid(scale, y, x);

		if (scale == cs.getMinLevelId()) {
			return;
		}
		Tile t = new WholeTile(tile);
		pushTile(scale, x, y, t, output, pendingTiles);

		// for combined version only the new tile is used, if any old delta file got copied we remove it here
		if (deltaBaseDir != null) {
			indexCombined.setFullPyramid(scale, y, x);
			pushTile(scale, x, y, t, outputCombined, pendingCombinedTiles);
			outputCombined.remove(scale, x, y);
		}


	}


	public synchronized void pushPartiallyUsed(int scale, int x, int y, BufferedImage tile, BufferedImage combinedTile) {
		
		if(outputCombined != null) outputCombined.remove(scale, x, y);
		
		index.setFullPyramid(scale, y, x);
		Tile t = new DeltaTile(tile);
		pushTile(scale, x, y, t, output, pendingTiles);

		if (indexCombined != null) {
			indexCombined.setFullPyramid(scale, y, x);
			if (combinedTile != null) {
				Tile t2 = new DeltaTile(combinedTile);
				pushTile(scale, x, y, t2, outputCombined, pendingCombinedTiles);
			} else {
				pushTile(scale, x, y, t, outputCombined, pendingCombinedTiles);
			}
		}
	}

	public synchronized void pushNoDelta(int scale, int x, int y) {
		pushTile(scale, x, y, DELTA_EMPTY, output, pendingTiles);

		// check if old delta contains this tile and fix index (file gets copied in the UpdateDOF)
		if (oldDeltaIndex != null) {
			if (oldDeltaIndex.hasTile(scale, y, x)) {
				try {
					Tile t = new DeltaTile(rp.getTileOrigDelta(scale, x, y).bi);
					pushTile(scale, x, y, t, outputCombined, pendingCombinedTiles);
					pushOldIndexForEmpty(scale, x, y);
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				pushTile(scale, x, y, DELTA_EMPTY, outputCombined, pendingCombinedTiles);
			}
		}
	}


	// tiles that are only in the old delta should be indexed also in finer grain
	private synchronized void pushOldIndexForEmpty(int scale, int x, int y) throws IOException {

		indexCombined.set(scale, y, x);

		int subScale = scale + 1;
		if (subScale > cs.getMaxLevelId())
			return;

		int xx = 2 * x;
		int yy = 2 * y;

		if (oldDeltaIndex.hasTile(subScale, yy, xx))
			pushOldIndexForEmpty(subScale, xx, yy);
		if (oldDeltaIndex.hasTile(subScale, yy, xx + 1))
			pushOldIndexForEmpty(subScale, xx + 1, yy);
		if (oldDeltaIndex.hasTile(subScale, yy + 1, xx))
			pushOldIndexForEmpty(subScale, xx, yy + 1);
		if (oldDeltaIndex.hasTile(subScale, yy + 1, xx + 1))
			pushOldIndexForEmpty(subScale, xx + 1, yy + 1);

	}

	public synchronized void pushEmptyTile(int scale, int x, int y) {
		TileSpec subTS = new TileSpec(scale, x, y);
		WholeTile tileToUpdate = waitingForData.get(subTS);
		if (tileToUpdate != null) {
			waitingForData.remove(subTS);
		}

		pushTile(scale, x, y, EMPTY, output, pendingTiles);

		if (outputCombined != null) {
			pushTile(scale, x, y, EMPTY, outputCombined, pendingCombinedTiles);
		}
	}

	private void pushTile(int scale, int x, int y, Tile tile, ImageOutput out, HashMap<TileSpec, PendingTile> cache) {
		if (tile.isDelta()) {
			writeDeltaTile(scale, x, y, tile.getData(), out);
		}
		
		int pScale = scale - 1;
		int pX = x >>> 1;
		int pY = y >>> 1;
		TileSpec parentSpec = new TileSpec(pScale, pX, pY);

		PendingTile parentTile = cache.get(parentSpec);
		if (parentTile == null) {
			parentTile = new PendingTile();
			cache.put(parentSpec, parentTile);
		}
		parentTile.addChild(2 * (y & 0x1) + (x & 0x1), tile);
		if (parentTile.hasAllData()) {
			cache.remove(parentSpec);
			
			Tile parent = reduce(parentSpec, parentTile);
			if (parent instanceof WholeTile && outputCombined!=null && out==outputCombined) {
				out.remove(pScale, pX, pY);
			}
			pushTile(pScale, pX, pY, parent, out, cache);
		}
	}


	@SuppressWarnings("static-method")
	private void writeDeltaTile(int scale, int x, int y, BufferedImage data, ImageOutput out) {

		if (data == null) {
			try {
				out.writeEmpty(scale, x, y);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			out.write(scale, x, y, data);
		}
	}

	public void shutdown() {
		output.shutdown();
		if (outputCombined != null)
			outputCombined.shutdown();
		try {
			PyramidIndexIO.saveIndexFileForBase(index.createPacked(), deltaDir);
			if (indexCombined != null)
				PyramidIndexIO.saveIndexFileForBase(indexCombined.createPacked(), deltaCombinedDir);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Tile reduce(TileSpec tileSpec, PendingTile tileData) {
		if (ArrayUtil.countSame(tileData.children, EMPTY) == 4) {
			return EMPTY;
		}
		if (ArrayUtil.countSame(tileData.children, DELTA_EMPTY) == 4) {
			return DELTA_EMPTY;
		}

		int countDelta = 0;
		for (Tile t : tileData.children) {
			if (t.isDelta()) {
				countDelta++;
			}
		}
		if (countDelta > 0) {
			BufferedImage shrunk = shrink(tileData.children);
			if (shrunk == null) {
				return DELTA_EMPTY;
			}
			return new DeltaTile(shrunk);
		}
		WholeTile ret = new WholeTile();
		waitingForData.put(tileSpec, ret);
		return ret;
	}

	private BufferedImage shrink(Tile[] children) {
		BufferedImage[] subs = new BufferedImage[4];
		for (int i = 0; i < 4; i++) {
			subs[i] = children[i].getData();
		}
		return RasterProcessor.shrinkSubTiles(subs, subsampler, tcLarge, tcSmall, cs);
	}

	public synchronized void pushSubData(int scale, int xx, int yy, BufferedImage bi) {
		TileSpec subTS = new TileSpec(scale, xx, yy);
		WholeTile tileToUpdate = waitingForData.get(subTS);
		if (tileToUpdate != null) {
			tileToUpdate.dataArrived(bi);
			waitingForData.remove(subTS);
			pushTile(scale, xx, yy, tileToUpdate, output, pendingTiles);
			if(outputCombined!=null) pushTile(scale, xx, yy, tileToUpdate, outputCombined, pendingCombinedTiles);
		}
	}


}
