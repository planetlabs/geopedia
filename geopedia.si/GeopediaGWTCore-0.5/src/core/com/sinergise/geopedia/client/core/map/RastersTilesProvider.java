package com.sinergise.geopedia.client.core.map;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.core.entities.baselayers.TiledBaseLayer;

public class RastersTilesProvider implements TilesProvider {

	TiledBaseLayer dataset;
	TiledCRS crs ;
	
	private class RasterSpecs {
		public int zoomLevel;
		public int tiledBaseLayerId;
		
		@Override
		public boolean equals(Object obj) {
			RasterSpecs rs = (RasterSpecs)obj;
			if (tiledBaseLayerId==rs.tiledBaseLayerId && zoomLevel==rs.zoomLevel)
				return true;
			return false;
		}
	}
	
	public RastersTilesProvider (TiledBaseLayer dataset) {
		this.dataset = dataset;
		WithBounds fromCRS = ClientGlobals.getMainCRS();
		if (dataset!=null) {
			crs = dataset.getTileProvider().getDatasetProperties().tiledCRSForMaxScale(fromCRS);			
		} else {
			crs=fromCRS;
		}
	}
	
	@Override
	public TiledCRS getTiledCRS(CRS mapCRS) {
		return crs;
	}

	@Override
	public String createTileURL(Object themeSpec, int row, int column) {
		if (themeSpec==null)
			return null;
		
		if (dataset == null)
			return null;
		RasterSpecs specs = (RasterSpecs) themeSpec;
		
		if (specs.zoomLevel > dataset.getMaxScaleLevel())
			specs.zoomLevel = dataset.getMaxScaleLevel();
		
		return dataset.getTileProvider().getTileURL(crs, specs.zoomLevel, column, row);
		
	}

	@Override
	public Object createThemeSpec(boolean doTrans, int zoomLevel,
			DisplayCoordinateAdapter dca) {
		
		if (dataset==null)
			return null;
		// TODO: check if anything is visible at all
		RasterSpecs specs = new RasterSpecs();
		specs.tiledBaseLayerId = dataset.id;
		specs.zoomLevel = zoomLevel;
	    return specs;		
	}

	@Override
	public boolean hasAnything(Object themeSpec) {
		return themeSpec!=null;
	}

	@Override
	public boolean isCompletelyOpaque(Object themeSpec) {
		return false;
	}

	@Override
	public double getPreferredScaleRatio(boolean doTrans, double scale) {
		return 0.5;
	}

	@Override
	public int getNumSimultaneousDownloads() {
		  return 10;
	}
	
}
