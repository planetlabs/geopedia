package com.sinergise.java.raster.pyramid;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.core.RasterIoJiio;
import com.sinergise.java.raster.dataraster.io.DataRasterIO;
import com.sinergise.java.raster.io.PyramidIndexIO;
import com.sinergise.java.raster.pyramid.TileProviderJava.AbstractTileProvider;
import com.sinergise.java.raster.ui.DEMRenderer;
import com.sinergise.java.raster.ui.DEMRenderer.DEMRenderSettings;
import com.sinergise.java.raster.ui.ElevationColors;
import com.sinergise.java.raster.ui.ElevationColors.ColorSpec;
import com.sinergise.java.util.io.FileUtilJava;


public class GPTilesProvider extends AbstractTileProvider implements Closeable {
	//TODO: Make suffix secondary - otherwise we need to store index at runtime for each image type in the pyramid 
	
	public static class WithDelegate extends GPTilesProvider {
		private static final Logger logger = LoggerFactory.getLogger(WithDelegate.class);
		GPTilesProvider delegate = null;
		TilesIndex myIndex = null;
		boolean noIndex = false;
		
		public WithDelegate(File baseDir, TiledCRS crs, String imageType, GPTilesProvider delegate) throws IOException {
			super(baseDir, crs, imageType);
			this.delegate = delegate;
		}
		
		protected void checkIndex() throws IOException {
			if (noIndex || myIndex != null) {
				return;
			}
			synchronized (this) {
				if (myIndex == null) {
					myIndex = loadIndex(baseDir);
					if (myIndex == null) {
						noIndex = true;
					}
				}
			}
		}
		
		public static TilesIndex loadIndex(File indexBaseDir) throws IOException {
			try {
				// Use parent dir, because baseDir is appended with type
				return PyramidIndexIO.loadReadOnlyPyramid(indexBaseDir.getParentFile());
			} catch (FileNotFoundException e) {
				logger.debug("No index found in parent dir of the typed tile provider, going to original: "+indexBaseDir.getParentFile());
			}
			try {
				return PyramidIndexIO.loadReadOnlyPyramid(indexBaseDir);
			} catch (FileNotFoundException e) {
				logger.info("No index found in base dir of the typed tile provider, going to 'noIndex' mode: "+indexBaseDir);
			}
			return null;
		}

		@SuppressWarnings("unused")
		@Override
		public void copyTile(int scale, int x, int y, File otherBase) throws IOException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public File getTileFile(int scale, int x, int y, boolean onlyIfExists) throws IOException {
			if (!onlyIfExists) throw new UnsupportedOperationException("onlyIfExists can only be true");
			checkIndex();
			File f = super.getTileFile(scale, x, y, true);
			if (f != null) return f;
			if (noIndex || myIndex.hasTile(scale, y, x)) {
				return delegate.getTileFile(scale, x, y, true);
			}
			return null;
		}
		
		@Override
		public boolean hasTile(int scale, int col, int row, boolean useDelegate) throws IOException {
			if (!useDelegate) {
				return super.hasTile(scale, col, row, false);
			}
			checkIndex();
			if (noIndex) {
				if (super.hasTile(scale, col, row, true)) return true;
				return delegate.hasTile(scale, col, row, true);
			}
			return myIndex.hasTile(scale, row, col);
		}
		
		@Override
		public void moveTile(int scale, int x, int y, File otherBase) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public File getFileForTilePath(String tileRelativePath, boolean onlyIfExists) throws IOException {
			if (!onlyIfExists) throw new UnsupportedOperationException("onlyIfExists can only be true");
			checkIndex();
			File f = super.getFileForTilePath(tileRelativePath, true);
			if (f != null || delegate==null) return f;
			if (noIndex) return delegate.getFileForTilePath(tileRelativePath, true);

			//Parse the path to get indices
			int slashIdx = Math.max(tileRelativePath.lastIndexOf('/'),tileRelativePath.lastIndexOf('\\'));
			int dotIdx = tileRelativePath.lastIndexOf('.');
			PointI pos = new PointI();
			int level = TileUtilGWT.parseTileSpec(cs, tileRelativePath.substring(slashIdx+1,dotIdx), pos);
			if (myIndex.hasTile(level, pos.y, pos.x)) {
				return delegate.getFileForTilePath(tileRelativePath, true);
			}
			return null;
		}
		
		@Override
		public void close() {
			super.close();
			myIndex = null;
		}
	}

	File baseDir;
	String suffix;
	Map<Integer, Set<String>> firstLevelDirs;
	
	public GPTilesProvider(File baseDir, String fileType) throws IOException {
		this(baseDir, TileUtilJava.resolveCRS(Arrays.asList(new File[] {baseDir, baseDir.getParentFile()}), null), fileType);
	}
	
	public GPTilesProvider(File baseDir, TiledCRS cs, String fileType) throws IOException {
		super(cs);
		File dirWithType = new File(baseDir, fileType);
		if (dirWithType.isDirectory()) baseDir = dirWithType;
		this.baseDir = baseDir;
		this.suffix = '.' + fileType;
		setBaseDir(baseDir);
	}
	
	@SuppressWarnings("unused")
	public void setBaseDir(File baseDir) throws IOException {
		this.baseDir = baseDir;
		firstLevelDirs = scan(baseDir, cs);
	}
	
	private static Map<Integer, Set<String>> scan(File baseDir, TiledCRS cs) {
		HashMap<Integer, Set<String>> ret = new HashMap<Integer, Set<String>>();
		for (int scale = cs.zoomLevels.getMinLevelId(); scale <= cs.zoomLevels.getMaxLevelId(); scale++) {
			if (TileUtilGWT.numCharsPerOrdinateForLevel(scale, cs.zoomLevels.getMinLevelId()) < 2) {
				ret.put(Integer.valueOf(scale), new HashSet<String>());
			} else {
				String scaleDir = cs.getTilePrefixChar() + "" + TileUtilGWT.tileLevelCharFromZoomLevel(scale);
				File scaleBase = new File(baseDir, scaleDir);
				if (!(scaleBase.isDirectory() && scaleBase.exists())) continue;
				String[] dirs = scaleBase.list();
				ret.put(Integer.valueOf(scale), new HashSet<String>(Arrays.asList(dirs)));
			}
		}
		return ret;
	}
	
	@Override
	public OffsetBufferedImage getTile(int scale, int x, int y) throws IOException {
		if (!hasTile(scale, x, y)) {
			return null;
		}
		File f = getTileFile(scale, x, y, true);
		if (f == null || (f.isFile() && f.length() == 0)) {
			DimI tSize = cs.tileSizeInPix(scale);
			return new OffsetBufferedImage(new BufferedImage(tSize.w(), tSize.h(), BufferedImage.TYPE_4BYTE_ABGR));
		}
		try {
			if (".sdm".equals(suffix)) {
				return getDEMTile(scale, x, y, f);
			}
			BufferedImage img = RasterIoJiio.readBuffered(f);
			return img == null ? null : new OffsetBufferedImage(img);
		} catch(IOException e) {
			//TODO: Logging
			System.err.println("Error reading " + f + ": " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	protected OffsetBufferedImage getDEMTile(int scale, int x, int y, File f) throws IOException, MalformedURLException {
		ShortDataBank sdb = DataRasterIO.load(f.toURI().toURL());
		
		
	    ElevationColors COLORS=new ElevationColors(new ColorSpec[] {
		    new ColorSpec(-5, 0x00c2e7fa),
		    new ColorSpec(0, 0xffc2e7fa),
		    new ColorSpec(1, 0xfffefdf4),
		    new ColorSpec(365, 0xfffff0cf),
		    new ColorSpec(1050, 0xfffac594)
	    }, 0xffc2e7fa);
//		ElevationColors COLORS = ElevationColors.Historic.JOHN_BARTHOLOMEW_JR.scaleTo(3000);
	    long t = System.nanoTime();
	    try {
	    	return sdb==null ? null : DEMRenderer.renderTile(sdb, cs.tileWorldBounds(scale, x, y), new DEMRenderSettings(COLORS, new double[] {-1,1,3}));
	    } finally {
	    	System.out.println("DEM rendered in "+(System.nanoTime() - t)/1e6+" ms");
	    }
	}
	
	@Override
	public boolean hasTile(int scale, int col, int row) throws IOException {
		return hasTile(scale, col, row, true);
	}

	/**
	 * @param useDelegate whether to look in delegate pyramids for the tile; if this is set to false, only local tiles will be considered 
	 */
	@SuppressWarnings("unused")
	public boolean hasTile(int scale, int col, int row, boolean useDelegate) throws IOException {
		Set<String> dirs = firstLevelDirs.get(Integer.valueOf(scale));
		if (dirs == null) return false;
		String nm = TileUtilGWT.tileNameForColRow(cs, scale, col, row);
		if (TileUtilGWT.numCharsPerOrdinateForLevel(scale, cs.zoomLevels.getMinLevelId()) >= 2) {
			String dirName = nm.substring(2, 4);
			if (!dirs.contains(dirName)) return false;
		}
		File f = PyramidUtil.fileFor(cs, baseDir, scale, col, row, suffix);
		return f.exists();
	}
	
	public void moveTile(int scale, int x, int y, File otherBase) throws IOException {
		String fRel = PyramidUtil.fileForRelative(cs, scale, x, y, suffix); 
		File f = new File(baseDir, fRel);
		if (f.exists()) {
			if (otherBase == null) {
				FileUtilJava.forceDelete(f);
			} else {
				File tgt = new File(otherBase, fRel);
				FileUtilJava.forceMkDirs(tgt.getParentFile());
				FileUtilJava.forceRename(f, tgt);
			}
		}
	}
	
	@Override
	public void copyTile(int scale, int x, int y, File otherBase) throws IOException {
		String fRel = PyramidUtil.fileForRelative(cs, scale, x, y, suffix); 
		File f = new File(baseDir, fRel);
		if (f.exists()) {
			File tgt = new File(otherBase, fRel);
			tgt.getParentFile().mkdirs();
			FileUtilJava.copyFile(f,tgt);
		}
	}
	
	public File getBaseDir() {
		return baseDir;
	}
	
	public File getTileFile(int scale, int x, int y) throws IOException {
		return getTileFile(scale, x, y, true);
	}
	
	@SuppressWarnings("unused")
	public File getTileFile(int scale, int x, int y, boolean onlyIfExists) throws IOException {
		File f = PyramidUtil.fileFor(cs, baseDir, scale, x, y, suffix);
		if (!onlyIfExists || f.exists()) {
			return f;
		}
		return null;
	}

	public String getFileType() {
		return suffix.substring(1);
	}

	public void setFileType(String newSuffix) {
		if (newSuffix == null) throw new NullPointerException("suffix");
		if (newSuffix.length()<2) throw new IllegalArgumentException("suffix too short");
		if (newSuffix.charAt(0)!='.') newSuffix = "."+newSuffix;
		if (newSuffix.equalsIgnoreCase(suffix)) return;
		suffix = newSuffix;
	}

	@SuppressWarnings("unused")
	public File getFileForTilePath(String relTilePath, boolean onlyIfExists) throws IOException {
		File f = new File(baseDir, relTilePath);
		if (!onlyIfExists || f.exists()) return f;
		return null;
	}

	public String getSuffix() {
		return suffix;
	}
	
	@Override
	public void close() {
		firstLevelDirs.clear();
	}
}
