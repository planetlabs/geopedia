package com.sinergise.gwt.gis.map.ui.overlays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter.PixFromWorld;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.util.html.CSS;


/**
 * Contains and displays tiles for a single "display layer" - a collection of Gisopedia layers set to be displayed
 * together.
 * 
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public class TiledOverlay extends OverlayComponent<TileRenderInfo> {
	private static final class TilePos implements Comparable<TilePos> {
		public PointI pos;
		public String tileUrl;
		public double distFromCenter;
		public Tile tile;

		public TilePos(PointI pos, String tileUrl,double distFromCenter) {
			this.pos = pos;
			this.distFromCenter = distFromCenter;
			this.tileUrl = tileUrl;
		}

		@Override
		public int compareTo(TilePos o) {
			double od = o.distFromCenter;
			if (distFromCenter < od)
				return -1;
			if (od == distFromCenter)
				return 0;
			return 1;
		}

		public boolean isContainedIn(EnvelopeI tBounds) {
			return tBounds.contains(pos);
		}

		public void release() {
			if (tile != null) {
				tile.release();
			}
		}

		@Override
		public int hashCode() {
			return ((pos == null) ? 0 : pos.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof TilePos)) {
				return false;
			}
			return Util.safeEquals(pos, ((TilePos)obj).pos);
		}
		
		
	}

	public static final Object SPEC_HIDDEN = "@@hidden@@";
	private int numOuterTiles = 0;

	private HashMap<PointI, TilePos> currTilePos = new HashMap<PointI, TilePos>();

	public TilesProvider provider;

	private TileRenderInfo lastRenderInfo = new TileRenderInfo();

	private int[] pixPosX = new int[0];
	private int[] pixPosY = new int[0];
	private int[] pixSizeW = new int[0];
	private int[] pixSizeH = new int[0];

	private Element mapDiv;
	private TiledCRS sets;
	private CRS mapCRS;
	private int maxTileFetchRetry = 4;

	protected void ensureRedraw() {
		removeAllTiles();
		lastRenderInfo = new TileRenderInfo();
	}

	public TiledOverlay(CRS mapCRS, TilesProvider provider) {
		super();
		this.mapDiv = DOM.createDiv();
		this.mapCRS = mapCRS;
		setTilesProvider(provider);
		CSS.position(mapDiv, CSS.POS_ABSOLUTE);
		DOM.appendChild(getElement(), mapDiv);
		MouseHandler.preventContextMenu(getElement());
		
		try {
			if(!sets.isColumnLeftToRight()){
				throw new Exception("NEGATIVE signed x-axis tiles not supported!");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void setTilesProvider(TilesProvider provider) {
		this.provider = provider;
		sets = provider.getTiledCRS(mapCRS);
	}

	public void setMaxTileFetchRetry(int retry) {
		maxTileFetchRetry = retry;
	}

	@Override
	public TileRenderInfo prepareToRender(DisplayCoordinateAdapter dca, boolean trans, boolean quick) {
		final int zoomLevel = calcZoomLevel(dca, trans);
		Object themeSpec = provider.createThemeSpec(trans, zoomLevel, dca);
		return new TileRenderInfo(dca, trans, quick, themeSpec, zoomLevel, hasAnything(themeSpec));
	}

	private boolean hasAnything(Object themeSpec) {
		return provider.hasAnything(themeSpec);
	}

	private int calcZoomLevel(DisplayCoordinateAdapter dca, boolean trans) {
		return sets.zoomLevels.optimalZoomLevelPix(dca.worldLengthPerPix, provider.getPreferredScaleRatio(trans, dca.worldLengthPerPix));
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		UIObject.setVisible(mapDiv, visible);
	}

	@Override
	public void reposition(TileRenderInfo tri) {
		try {
			if (!willRender(tri)) {
				return;
			}
			tri.prepare(sets, numOuterTiles);
			CSS.leftTop(mapDiv, tri.pixOffX, tri.pixOffY);

			if (shouldJustMove(tri)) {
				return;
			} else if (shouldRemoveAll(tri)) {
				removeAllTiles();
			}
			
			updateGrid(tri);

			ArrayList<TilePos> toSet = getNeededTiles(tri);
			Collections.sort(toSet); // Sort so we load the center tiles first
			positionTiles(tri.isTransparent, tri.tilesEnv, toSet);

			removeObsolete(tri.tilesEnv);
		} finally {
			lastRenderInfo = tri;
		}
	}

	private boolean willRender(TileRenderInfo tri) {
		return isVisible() && tri.hasAnything;
	}

	private boolean shouldJustMove(TileRenderInfo curInfo) {
		return curInfo.tileSubsetEquals(lastRenderInfo);
	}

	private boolean shouldRemoveAll(TileRenderInfo curInfo) {
		return !curInfo.hasAnything || !curInfo.themeAndScaleEquals(lastRenderInfo);
	}

	private void positionTiles(boolean transPNG, EnvelopeI tBounds, ArrayList<TilePos> toSet) {
		for (TilePos tPos : toSet) {
			final int cOff = tPos.pos.x - tBounds.minX();
			final int rOff = tPos.pos.y - tBounds.minY();
			if (tPos.tile == null) {
				tPos.tile = new Tile(maxTileFetchRetry);
				tPos.tile.setTransPNG(transPNG);
				DOM.appendChild(mapDiv, tPos.tile.getElement());
				tPos.tile.setSrc(tPos.tileUrl);
			}
			tPos.tile.setSize(pixSizeW[cOff], pixSizeH[rOff]);
			tPos.tile.position(pixPosX[cOff], pixPosY[rOff]);
		}
	}

	private void removeObsolete(EnvelopeI tBounds) {
		for (Iterator<TilePos> it = currTilePos.values().iterator(); it.hasNext();) {
			TilePos tPos = it.next();
			if (!tPos.isContainedIn(tBounds)) {
				it.remove();
				tPos.tile.release();
			}
		}
	}

	private ArrayList<TilePos> getNeededTiles(TileRenderInfo tri) {
		EnvelopeI tBounds = tri.tilesEnv;
		ArrayList<TilePos> toSet = new ArrayList<TilePos>(tBounds.getArea());
		// Need for distance from center
		double centerRow = sets.dTileRow(tri.dca.worldCenterY, tri.zoomLevel);
		double centerCol = sets.dTileColumn(tri.dca.worldCenterX, tri.zoomLevel);

		// Determine the needed tiles
		for (PointI tidx : tBounds) {
			TilePos toAdd = currTilePos.get(tidx);
			if (toAdd == null) {
				String tileUrl = provider.createTileURL(tri.themeSpec, tidx.y, tidx.x);
				if (tileUrl == null) {
					continue;
				}
				double distFromCenter = GeomUtil.distanceSq(centerCol, centerRow, tidx.x + 0.5, tidx.y + 0.5);
				toAdd = new TilePos(tidx, tileUrl, distFromCenter);
				currTilePos.put(tidx, toAdd);
			}
			toSet.add(toAdd);
		}
		return toSet;
	}

	private void updateGrid(TileRenderInfo tri) { 
		EnvelopeI tEnv = tri.tilesEnv;
		int zoomLevel = tri.zoomLevel;
		PixFromWorld pixFromWorld = tri.dca.pixFromWorld;
		int pixOffX = tri.pixOffX;
		int pixOffY = tri.pixOffY;
		
		final int numX = tEnv.getWidth();
		final int minCol = tEnv.minX();
		if (pixPosX.length < numX) {
			pixPosX = new int[numX];
		}
		if (pixSizeW.length < numX) {
			pixSizeW = new int[numX];
		}
		//TODO: This probably won't work if COLUMN orientation is negative
		double curLeft = sets.tileLeft(zoomLevel, minCol);
		int curX = pixFromWorld.xInt(curLeft) - pixOffX;
		for (int i = 0; i < numX; i++) {
			int nextX = pixFromWorld.xInt(sets.tileRight(zoomLevel, minCol + i)) - pixOffX;
			pixPosX[i] = curX;
			pixSizeW[i] = nextX - curX;
			curX = nextX;
		}
		
		final int numY = tEnv.getHeight();
		int minRow = tEnv.minY();
		if (pixPosY.length < numY) {
			pixPosY = new int[numY];
		}
		for (int i = 0; i < numY; i++) {
			int curY = pixFromWorld.yInt(sets.tileTop(zoomLevel, minRow + i)) - pixOffY;
			pixPosY[i] = curY;
		}
		if (pixSizeH.length < numY) {
			pixSizeH = new int[numY];
		}
		if (sets.isRowBottomToTop()) {
			int bottomY = pixFromWorld.yInt(sets.tileBottom(zoomLevel, minRow)) - pixOffY;
			pixSizeH[0] = bottomY - pixPosY[0]; 
			for (int i = 1; i < numY; i++) {
				pixSizeH[i] = pixPosY[i-1] - pixPosY[i];
			}
		} else {
			for (int i = 0; i < numY-1; i++) {
				pixSizeH[i] = pixPosY[i+1] - pixPosY[i];
			}
			int bottomY = pixFromWorld.yInt(sets.tileBottom(zoomLevel, tEnv.maxY())) - pixOffY;
			pixSizeH[numY-1] = bottomY - pixPosY[numY-1]; 
		}
	}

	protected void removeAllTiles() {
		for (TilePos tl : currTilePos.values()) {
			tl.release();
		}
		currTilePos.clear();
	}
}
