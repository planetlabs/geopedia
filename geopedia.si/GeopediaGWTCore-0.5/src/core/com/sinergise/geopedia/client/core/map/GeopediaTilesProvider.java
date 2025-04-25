package com.sinergise.geopedia.client.core.map;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.config.IsURLProvider;

public class GeopediaTilesProvider implements TilesProvider {

	private TiledCRS crs;
	private class PediaThemeSpec {
		String specString;
		int zoomLevel;
		@Override
		public boolean equals(Object obj)
		{
			PediaThemeSpec pts = (PediaThemeSpec)obj;
			if (specString!=null && specString.equals(pts.specString) && zoomLevel==pts.zoomLevel) {
				return true;
			} 
			return false;
		}
	}
	private MapLayers mapLayers;
	private IsURLProvider tilesBaseURLProvider;
	
	public GeopediaTilesProvider(MapLayers mapLayers) {
		Configuration conf = ClientGlobals.configuration;
		this.mapLayers=mapLayers;
		crs = ClientGlobals.getMainCRS();
		tilesBaseURLProvider = conf.publicRenderers;
	}
	@Override
	public TiledCRS getTiledCRS(CRS mapCRS) {
		return crs;
	}

	@Override
	public String createTileURL(Object themeSpec, int row, int column) {
		if (themeSpec==null)
			return null;

		PediaThemeSpec spec = (PediaThemeSpec)themeSpec;
		
		return tilesBaseURLProvider.getBaseURL() +
				TileUtilGWT.tileNameForColRow(crs, spec.zoomLevel, column, row) + '/'+spec.specString;	
	}

	@Override
	public Object createThemeSpec(boolean doTrans, int zoomLevel,
			DisplayCoordinateAdapter dca) {
		
		
		if (mapLayers == null)
			throw new IllegalArgumentException("Can't get string for null layers");
		StringBuffer buf = new StringBuffer();
		mapLayers.appendThemeLayers(buf);
		if (buf.length()==0)
			return null;
		buf.append(ClientSession.additionalTimestamp);
		// sid
		String sidParam = null;
		String session = ClientSession.getSessionValue();
		if (session != null && session.length() > 0) {
			sidParam = "?sid=" + session;
			buf.append(sidParam);
		}

		
		if (!doTrans) {
			if (sidParam==null) {
				buf.append("?");
			} else {
				buf.append("&");
			}
			buf.append("opaque=1");
		}
		
		PediaThemeSpec spec = new PediaThemeSpec();
		spec.specString = buf.toString();
		spec.zoomLevel=zoomLevel;
		return spec;
	}

	@Override
	public boolean hasAnything(Object themeSpec) {
		PediaThemeSpec spec = (PediaThemeSpec)themeSpec;
		if (themeSpec==null  || spec.specString==null || spec.specString.length()==0)
			return false;
		return true;
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
		return 1; // TODO: fix!!
	}

}
