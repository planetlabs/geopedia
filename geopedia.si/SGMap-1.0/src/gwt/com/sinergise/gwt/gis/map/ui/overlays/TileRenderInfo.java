package com.sinergise.gwt.gis.map.ui.overlays;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;

final class TileRenderInfo extends RenderInfo {
	double scale;
	int zoomLevel;
	Object themeSpec;
	EnvelopeI tilesEnv;
	Envelope worldRectForTiles;
	int pixOffX;
	int pixOffY;
	
	public TileRenderInfo() {
		super();
		themeSpec = TiledOverlay.SPEC_HIDDEN;
	}

	public TileRenderInfo(DisplayCoordinateAdapter dca, boolean trans, boolean quick, Object themeSpec, int zoomLevel, boolean hasAnything) {
		super(dca, trans, quick);
		super.hasAnything = hasAnything;
		this.themeSpec = themeSpec;
		this.zoomLevel = zoomLevel;
		this.scale = dca.worldLengthPerPix;
	}
	
	public boolean themeAndScaleEquals(TileRenderInfo other) {
		return (scale == other.scale) //
			&& (isTransparent == other.isTransparent) //
			&& (themeSpec.equals(other.themeSpec)) //
			&& (hasAnything == other.hasAnything);
	}

	public boolean tileSubsetEquals(TileRenderInfo other) {
		return themeAndScaleEquals(other) && tilesEnv.equals(other.tilesEnv);
	}

	public void prepare(TiledCRS sets, int numOuterTiles) {
		tilesEnv = sets.tilesInEnvelope(dca.worldRect, zoomLevel, numOuterTiles);
		worldRectForTiles = tilesEnv.isEmpty() ? Envelope.getEmpty() : sets.tileWorldBounds(zoomLevel, tilesEnv);
		pixOffX = dca.pixFromWorld.xInt(worldRectForTiles.getMinX());
		pixOffY = dca.pixFromWorld.yInt(worldRectForTiles.getMaxY());
	}
}