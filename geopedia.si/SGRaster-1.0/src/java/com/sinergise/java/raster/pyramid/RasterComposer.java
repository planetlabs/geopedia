package com.sinergise.java.raster.pyramid;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.RasterUtilJava.CheckResult;
import com.sinergise.java.raster.pyramid.RasterProcessor.ImageOutput;
import com.sinergise.java.raster.pyramid.TileProviderJava.AbstractTileProvider;

public class RasterComposer {
	public static enum ResultProduced {EMPTY, BACK, FORE, COMPOSED}
	
	public TileProviderJava back;
	public AbstractTileProvider fore;

	private ImageOutput out;
	private TiledCRS cs;
	
	public RasterComposer() {
	}
	
	public void go(TiledCRS crs, File outDir, String outType) throws IOException {
		try {
			this.cs = crs;
			this.out = new ImageOutput("RasterComposer", new File[] {outDir}, new String[] {outType}, crs, System.out);
			go();
		} finally {
			out.shutdown();
		}
	}
	
	private void go() throws IOException {
		goAndWrite(cs.getMinLevelId(), 0, 0);
	}

	private void goAndWrite(int level, int col, int row) throws IOException {
		composeAndWrite(level, col, row); 
		
		if (level < cs.getMaxLevelId()) {
			int subLevel = level+1;
			goAndWrite(subLevel, 2*col, 2*row);
			goAndWrite(subLevel, 2*col + 1, 2*row);
			goAndWrite(subLevel, 2*col, 2*row+1);
			goAndWrite(subLevel, 2*col + 1, 2*row+1);
		}
	}

	private ResultProduced composeAndWrite(int level, int col, int row) throws IOException {
		OffsetBufferedImage fgTile = fore.getTile(level, col, row);
		CheckResult fgType = CheckResult.FULLY_TRANSPARENT;
		if (fgTile != null && fgTile.bi != null) {
			fgType = RasterUtilJava.checkImage(fgTile.bi);
		}
		
		if (fgType == CheckResult.FULLY_TRANSPARENT) {
			return useBackground(level, col, row);
		}
		if (fgType == CheckResult.FULLY_OPAQUE) {
			return useForeground(level, col, row);
		}
		
		OffsetBufferedImage bgTile = back.getTile(level, col, row);
		if (bgTile == null) {
			return useForeground(level, col, row); 
		}
		OffsetBufferedImage outTile = compose(bgTile, fgTile);
		if (outTile == fgTile) {
			return useForeground(level, col, row);
		}
		writeComposed(outTile, level, col, row);
		return ResultProduced.COMPOSED;
	}

	private void writeComposed(OffsetBufferedImage outTile, int level, int col, int row) {
		out.write(level, col, row, outTile.bi);
	}

	private ResultProduced useForeground(int level, int col, int row) throws IOException {
		fore.copyTile(level, col, row, out.outDirs[0]);
		return ResultProduced.FORE;
	}

	private static ResultProduced useBackground(int level, int col, int row) {
		return ResultProduced.BACK;
	}

	private static OffsetBufferedImage compose(OffsetBufferedImage bgTile, OffsetBufferedImage fgTile) {
		Graphics2D g = bgTile.bi.createGraphics();
		g.drawImage(fgTile.bi, null, 0, 0);
		return bgTile;
	}
}
