/*
 *
 */
package com.sinergise.gwt.gis.geopedia;

import java.util.ArrayList;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.common.gis.geopedia.GeopediaLayer;
import com.sinergise.common.gis.geopedia.GeopediaLayersSource;


public class GeopediaLayersRenderer implements TilesProvider {
    private static class GPThemeSpec {
        public GPThemeSpec() {}
		String suffix;
        int zoomLevel;
    }
    private ArrayList<GeopediaLayer> layers;
    
    @Override
	public Object createThemeSpec(boolean doTrans, int zoomLevel, DisplayCoordinateAdapter dca) {
        if (layers == null) return null;
        if (layers.isEmpty()) return null;
        StringBuffer buf = new StringBuffer();
        boolean first=true;
        for (GeopediaLayer lyr : layers) {
            if (lyr.deepOn()) {
                if (!first) buf.append(',');
                GeopediaLayersSource.appendLayerSpec(lyr, buf);
                first=false;
            }
        }
        if (first) return null;
        if (!doTrans) {
            buf.append("?opaque=1");
        }
        GPThemeSpec ts=new GPThemeSpec();
        ts.suffix=buf.toString();
        ts.zoomLevel=zoomLevel;
        return ts;
    }
    
    @Override
	public double getPreferredScaleRatio(boolean doTrans, double scale) {
        return 0.2;
    }

    @Override
	public String createTileURL(Object themeSpec, int row, int column) {
        int rnd=(int)(Math.random()*5)+1;
        GPThemeSpec spc=(GPThemeSpec)themeSpec;
        return "http://pr"+rnd+".geopedia.si/rp/"+TileUtilGWT.tileNameForColRow(GeopediaLayersSource.GP_TILES, spc.zoomLevel, column, row)+"/"+spc.suffix;
    }

    @Override
	public boolean hasAnything(Object themeSpec) {
        if (themeSpec==null) return false;
        return true;
    }
    
    @Override
	public TiledCRS getTiledCRS(CRS mapCRS) {
        return GeopediaLayersSource.GP_TILES;
    }
    
    @Override
	public int getNumSimultaneousDownloads() {
        return 5;
    }

    public void addLayer(GeopediaLayer lyr) {
        if (layers==null) layers=new ArrayList<GeopediaLayer>();
        layers.add(lyr);
    }

    @Override
	public boolean isCompletelyOpaque(Object themeSpec) {
        return false;
    }

}
