package com.sinergise.java.raster.pyramid;

import static com.sinergise.java.raster.pyramid.PyramidUtil.findFileTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.PyramidIndex;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.core.TilesIndex.Mutable;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.io.PyramidIndexIO;
import com.sinergise.java.raster.pyramid.PyramidUtil.PyramidDirsInfo;
import com.sinergise.java.util.io.FileUtilJava;


public class PyramidManager extends GPTilesProvider {
	private static final int KEEP = -1;
	private static final int REBUILD = -2;

	private TilesIndex.Mutable index;
	private boolean dirty = true;
	
	public PyramidManager(File baseDir, TiledCRS crs, String imageType) throws IOException {
		super(baseDir, crs, imageType);
	}
	
	public void rebuildIndex() throws IOException {
		index = (Mutable)PyramidIndexIO.buildFromDirs(cs, baseDir, suffix);
		dirty = true;
		if (index == null) {
			index = new PyramidIndex(cs, cs.getMinLevelId(), cs.getMaxLevelId());
		}
		saveIndex();
	}
	
	@Override
	public void setBaseDir(File baseDir) throws IOException {
		super.setBaseDir(baseDir);
		if (!baseDir.exists()) {
			throw new FileNotFoundException("Pyramid directory '"+baseDir+"' does not exist.");
		}		
		try {
			reloadIndex();
		} catch (FileNotFoundException e) {
			rebuildIndex();
		}
		PyramidDirsInfo dirInfo = PyramidUtil.getDirInfo(baseDir);
		if (dirInfo != null) {
			setMaxLevel(dirInfo.maxLevel);
		}
	}

	private void setMaxLevel(int maxLevel) {
		cs = cs.createWithMaxLevel(maxLevel);
	}

	public void expandIndex(int maxLevel) {
		if (index.getMaxIndexedLevel() >= maxLevel) {
			return;
		}
		index.expandTo(maxLevel);
		dirty = true;
		setMaxLevel(maxLevel);
	}
	
	public void saveIndex() throws IOException {
		if (dirty) {
			PyramidIndexIO.saveIndexFileForBase(index, baseDir);
			dirty = false;
		}
	}
	
	public void reloadIndex() throws IOException {
		index = (Mutable)PyramidIndexIO.loadPyramidFromBase(baseDir);
		dirty = false;
		if (index.getMaxIndexedLevel() < cs.getMaxLevelId()) {
			index.expandTo(cs.getMaxLevelId());
			dirty = true;
		}
		setMaxLevel(index.getMaxIndexedLevel());
	}
	
	@Override
	public int getMaxLevelId() {
		return super.getMaxLevelId();
	}
	
	@Override
	public OffsetBufferedImage getTile(int scale, int x, int y) throws IOException {
		if (!hasTile(scale, x, y)) return null;
		return super.getTile(scale, x, y);
	}
	
	@Override
	public boolean hasTile(int scale, int x, int y) throws IOException {
		if (scale <= index.getMaxIndexedLevel()) {
			return index.hasTile(scale, y, x);
		}
		return super.hasTile(scale, x, y);
	}
	
	public boolean hasTileInIndex(int scale, int x, int y) {
		if (scale <= index.getMaxIndexedLevel()) {
			return index.hasTile(scale, y, x);
		}
		return false;
	}

	public TilesIndex getIndex() {
		return index;
	}
	
	public boolean deleteSingleTile(int zoomLevel, int x, int y) throws IOException {
		File f = getTileFile(zoomLevel, x, y);
		if (f == null || !f.exists()) {
			return false;
		}
		FileUtilJava.forceDelete(f);
		index.clear(zoomLevel, y, x);
		dirty = true;
		try {
			FileUtilJava.deleteIfEmpty(f.getParentFile(), true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean deleteTile(int zoomLevel, int x, int y, boolean deleteHigherLevels, int maxLowerLevelToDelete) throws IOException {
		boolean deletedAny = false;
		deletedAny |= deleteSingleTile(zoomLevel, x, y);
		Envelope mbr = cs.tileWorldBounds(zoomLevel, x, y);
		if (deleteHigherLevels) {
			for (int i = zoomLevel + 1; i <= cs.getMaxLevelId(); i++) {
				deletedAny |= deleteTilesCovered(i, mbr);
			}
		}
		if (maxLowerLevelToDelete==KEEP) return deletedAny;
		if (maxLowerLevelToDelete==REBUILD) throw new UnsupportedOperationException("Somebody should implement rebuilding");
		if (maxLowerLevelToDelete>=0) {
			double wx = 0.5*(cs.tileLeft(zoomLevel, x)+cs.tileRight(zoomLevel, x));
			double wy = 0.5*(cs.tileBottom(zoomLevel, y)+cs.tileTop(zoomLevel, y));
			int minToDelete = Math.max(index.getMinIndexedLevel(), maxLowerLevelToDelete);
			for (int i = zoomLevel-1; i >= minToDelete; i--) {
				deleteIfNoHigherTiles(i, wx, wy);
			}
		}
		return deletedAny;
	}
	
	private boolean deleteIfNoHigherTiles(int zoomLevel, double wx, double wy) throws IOException {
		if (zoomLevel == index.getMaxIndexedLevel()) throw new IllegalArgumentException("zoomLevel should be lower than maximum");
		int row = cs.tileRow(wy, zoomLevel);
		int col = cs.tileColumn(wx, zoomLevel);
		return deleteIfNoHigherTiles(zoomLevel, col, row);
	}

	private boolean deleteIfNoHigherTiles(int zoomLevel, int col, int row) throws IOException {
		Envelope env = cs.tileWorldBounds(zoomLevel, col, row);
		final int higherZoom = zoomLevel+1;
		EnvelopeI tmbr = cs.tilesInEnvelope(env, higherZoom);
		for (PointI p : tmbr) {
			if (index.hasTile(higherZoom, p.y, p.x)) { 
				return false;
			}
		}
		deleteSingleTile(zoomLevel, col, row);
		return true;
	}
	
	public boolean deleteTilesCovered(final int zoomLevel, final Envelope mbr) throws IOException {
		EnvelopeI tmbr = cs.tilesInEnvelope(mbr, zoomLevel);
		boolean deletedAny = false;
		for (PointI p : tmbr) {
			if (!mbr.contains(cs.tileWorldBounds(zoomLevel, p.x, p.y))) {
				continue;
			}
			if (deleteSingleTile(zoomLevel, p.x, p.y)) {
				deletedAny = true;
			}
		}
		return deletedAny;
	}

	public void tileAdded(int zoom, int col, int row) {
		index.set(zoom, row, col);
		dirty=true;
		setMaxLevel(Math.max(zoom, getMaxLevelId()));
	}

	public long getLastUpdatedTopLevel() throws IOException {
		File f = getTileFile(cs.getMinLevelId(), 0, 0, true);
		if (f==null) return 0;
		return f.lastModified();
	}

	public static PyramidManager createFor(File datasetFileOrDir) throws IOException, TransformerException {
		if (datasetFileOrDir.isDirectory()) {
			TiledCRS tileCS = PyramidUtil.findCRS(datasetFileOrDir);
			String[] imgTypes = findFileTypes(datasetFileOrDir, tileCS.getTilePrefixChar(), tileCS.getMinLevelId());
			String imgType = imgTypes[0];
			return new PyramidManager(datasetFileOrDir, tileCS, imgType);
		}
		throw new UnsupportedOperationException("Only directories currently supported");
	}

	public static void main(String[] args) throws IOException {
		//build index of last
		TilesIndex idx = PyramidIndexIO.buildFromDirs(TiledCRS.GP_SLO, new File("D:\\Data\\GeoData\\slo\\dof\\out\\last\\jpg"), "jpg");
		
		//create prev version dir
		File prevDir = new File("D:\\Data\\GeoData\\slo\\dof\\out\\2007-03");
		FileUtilJava.forceMkDir(prevDir);
		//save index of last to prev version dir
		PyramidIndexIO.saveIndexFileForBase(idx, prevDir);
//		Update last
		
	}
}
