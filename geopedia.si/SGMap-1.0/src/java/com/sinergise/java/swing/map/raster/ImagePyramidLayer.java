package com.sinergise.java.swing.map.raster;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.pyramid.TileProviderJava;
import com.sinergise.java.raster.ui.RasterGUIUtils;
import com.sinergise.java.swing.map.PaintOperation;
import com.sinergise.java.swing.map.layer.LayerPerformanceInfo;
import com.sinergise.java.swing.map.layer.OrLayerImpl;

public class ImagePyramidLayer extends OrLayerImpl {
	private boolean debug = false;
	TileProviderJava tileSource;
	private LayerPerformanceInfo perfInfo = new LayerPerformanceInfo() {
		@Override
		public long timeToRender(DisplayCoordinateAdapter dca) {
			return estimateRenderTime();
		}
		
		@Override
		public long maxTimeToRender() {
			return 100;
		}
		
		@Override
		public double updateFreq() {
			return 0;
		}
	};
	private HashMap<String, OffsetBufferedImage> cache = new HashMap<String, OffsetBufferedImage>();
	
	public ImagePyramidLayer(TileProviderJava tileSource) {
		super();
		this.tileSource = tileSource; 
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	long estimateRenderTime() {
		return 100;
	}
	
	@Override
	public LayerPerformanceInfo getPerformanceInfo() {
		return perfInfo;
	}
	
	@Override
	public Envelope getBounds() {
		return tileSource.getTiledCRS().getBounds().mbr;
	}
	
	public void clearCache() {
		cache.clear();
	}
	
	@Override
	public void paintLayer(final Graphics2D g, final DisplayCoordinateAdapter dca, final PaintOperation mgr) {
		if (dca == null || tileSource == null) {
			g.drawString("Set coordinate space", 10, 10);
			return;
		}
		final int level = Math.min(tileSource.getTiledCRS().zoomLevels.nearestZoomLevelPix(dca.worldLengthPerPix), tileSource.getMaxLevelId());
		
		final HashSet<String> toRemove = new HashSet<String>(cache.keySet());
		try {
			g.setColor(Color.BLACK);
			EnvelopeI tilesToRender = tileSource.getTiledCRS().tilesInEnvelope(dca.worldFromPix.rect(0, 0, dca.pixDisplaySize.w(), dca.pixDisplaySize.h()), level);
			for (PointI p : tilesToRender) {
				if (mgr.isCancelled()) {
					return;
				}
				paintTile(g, dca, level, p, toRemove);
			}
		} finally {
			for (String tileName : toRemove) {
				cache.remove(tileName);
			}
		}
	}
	
	private void paintTile(final Graphics2D g, final DisplayCoordinateAdapter dca, final int level, PointI tileIndex, final HashSet<String> toRemove) {
		TiledCRS space = tileSource.getTiledCRS();
		final int col = tileIndex.x;
		final int row = tileIndex.y;
		String tName = TileUtilGWT.tileNameForColRow(space, level, col, row);
		toRemove.remove(tName);
		try {
			Envelope envWorld = space.tileWorldBounds(level, col, row);
			EnvelopeI rct = dca.pixFromWorld.rectInt(envWorld);
			try {
				if (!tileSource.hasTile(level, col, row)) {
					if (debug) paintEmptyTile(g, level, col, row, rct);
					return;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (debug) {
				paintTileBG(g, level, col, row, rct);
			}
			
			OffsetBufferedImage bImg = null;
			if (cache.containsKey(tName)) {
				bImg = cache.get(tName);
			}
			if (bImg == null) {
				bImg = tileSource.getTile(level, col, row);
				if (bImg != null) cache.put(tName, bImg);
				else {
					return;
				}
			}
			
			double pixW = space.zoomLevels.worldPerPix(level);
			double wx0 = envWorld.getMinX() + bImg.offX * pixW;
			double wx1 = wx0 + bImg.bi.getWidth() * pixW;
			double wy1 = envWorld.getMaxY() - bImg.offY * pixW;
			double wy0 = wy1 - bImg.bi.getHeight() * pixW;
			
			EnvelopeI picEnv = dca.pixFromWorld.rectInt(wx0, wy0, wx1, wy1);
			g.drawImage(bImg.bi, picEnv.minX(), picEnv.minY(), picEnv.getWidth()-1, picEnv.getHeight()-1, null);

//			paintEmptyTile(g, level, col, row, rct);
		} catch (IOException e) {
		}
		return;
	}


	private static Paint GRID_PAINT = null;
	
	private void paintTileBG(Graphics2D g, int level, int col, int row, EnvelopeI rct) {
		if (GRID_PAINT == null) {
			GRID_PAINT = RasterGUIUtils.createGridPaint(20, 0x80808080, 0xFFFFFFFF);
		}
		g.setPaint(GRID_PAINT);
		g.fillRect(rct.minX()+1, rct.minY()+1, rct.getWidth()-2, rct.getHeight()-2);
	}

	protected void paintEmptyTile(Graphics2D g, int level, int col, int row, EnvelopeI rct) {
		g.setColor(Color.BLACK);
		g.drawString(tileSource.getTiledCRS().tileName(level, col, row), rct.minX()+4, rct.minY()+16);
		g.drawString(col+","+row, rct.minX()+4, rct.minY()+30);
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(rct.minX(), rct.minY(), rct.getWidth()-2, rct.getHeight()-2);
	}
}
