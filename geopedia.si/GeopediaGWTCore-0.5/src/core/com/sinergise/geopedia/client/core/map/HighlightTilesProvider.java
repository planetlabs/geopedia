package com.sinergise.geopedia.client.core.map;

import java.util.Iterator;
import java.util.Map;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.config.IsURLProvider;
import com.sinergise.geopedia.core.entities.Feature;

public class HighlightTilesProvider implements TilesProvider {

	private TiledCRS crs;

	private FeatureHashMap featureMap = new FeatureHashMap();
	String hiliteBasePath = ClientGlobals.configuration.publicFeatureHighlightBasePath;
	private IsURLProvider tilesBaseURLProvider;

	private class PediaThemeSpec {
		String specString;
		int zoomLevel;

		@Override
		public boolean equals(Object obj) {
			PediaThemeSpec pts = (PediaThemeSpec) obj;
			if (specString != null && specString.equals(pts.specString) && zoomLevel == pts.zoomLevel) {
				return true;
			}
			return false;
		}
	}

	public HighlightTilesProvider() {
		Configuration conf = ClientGlobals.configuration;
		crs = ClientGlobals.getMainCRS();
		tilesBaseURLProvider = conf.publicRenderers;
	}

	@Override
	public TiledCRS getTiledCRS(CRS mapCRS) {
		return crs;
	}

	@Override
	public String createTileURL(Object themeSpec, int row, int column) {
		if (themeSpec == null)
			return null;

		PediaThemeSpec spec = (PediaThemeSpec) themeSpec;
		if (spec.specString == null || spec.specString.length() == 0)
			return null;
		if (!featureMap.isVisible(crs, spec.zoomLevel, column, row))
			return null;
		return tilesBaseURLProvider.getHost() + hiliteBasePath + "/" + TileUtilGWT.tileNameForColRow(crs, spec.zoomLevel, column, row) + '/'
				+ spec.specString;
	}
	
	public void appendHighlightFeatures(StringBuffer buf) {
		Iterator<Map.Entry<Integer, TableFeatures>> tkeys = featureMap.getTableIterator();
		boolean first=true;
		if (tkeys != null) {
			while (tkeys.hasNext()) {
				java.util.Map.Entry<Integer, TableFeatures> e = (java.util.Map.Entry<Integer, TableFeatures>) tkeys.next();
				if (!first) {
					buf.append(';');
				}
				first=false;
				((TableFeatures) e.getValue()).getUrlPart(buf);
			}
		}
	}

	@Override
	public Object createThemeSpec(boolean doTrans, int zoomLevel, DisplayCoordinateAdapter dca) {

		StringBuffer buf = new StringBuffer();
		appendHighlightFeatures(buf);
		
		if (buf.length()==0) return null;
		PediaThemeSpec spec = new PediaThemeSpec();
		String sidParam = null;
		String session = ClientSession.getSessionValue();
		if (session != null && session.length() > 0) {
			sidParam = "?sid=" + session;
			buf.append(sidParam);
		}
		spec.specString = buf.toString();
		spec.zoomLevel = zoomLevel;
		return spec;
	}

	@Override
	public boolean hasAnything(Object themeSpec) {
		PediaThemeSpec spec = (PediaThemeSpec) themeSpec;
		if (themeSpec == null || spec.specString == null || spec.specString.length() == 0)
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

	public void addFeat(Feature feature, int tableId) {
		featureMap.addFeature(feature, tableId);
	}

	public void clearFeats() {
		featureMap.clearFeatures();
	}

	public void removeFeat(int tableId, int featId) {
		featureMap.removeFeature(tableId, featId);

	}

	public boolean isEmpty() {
		return featureMap.isEmpty();
	}
}
