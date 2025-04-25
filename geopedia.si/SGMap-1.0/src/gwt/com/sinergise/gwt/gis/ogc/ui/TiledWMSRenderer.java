/*
 *
 */
package com.sinergise.gwt.gis.ogc.ui;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.gwt.gis.ogc.ui.WMSRendererHelper.WMSThemeSpec;
import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;


public class TiledWMSRenderer implements TilesProvider, WMSRenderer {
    private static class WMSSpec {
        int zoomLevel;
        WMSThemeSpec themeSpec;
        public WMSSpec(WMSThemeSpec thSpec, int zoomLevel) {
        	super();
        	this.themeSpec=thSpec;
            this.zoomLevel=zoomLevel;
        }
        @Override
		public int hashCode() {
            final int prime = 31;
            int result = themeSpec.hashCode();
            result = prime * result + zoomLevel;
            return result;
        }
        @Override
		public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            final WMSSpec other = (WMSSpec) obj;
            if (!themeSpec.equals(other.themeSpec)) return false;
            if (zoomLevel != other.zoomLevel)
                return false;
            return true;
        }
        
    }
    
    private final TiledCRS tCRS;
    public final WMSRendererHelper helper;
    public TiledWMSRenderer(TiledCRS crs, WMSLayersSource src) {
        super();
        this.helper=new WMSRendererHelper(src, crs.baseCRS);
        this.tCRS=crs;
    }
    @Override
	public double getPreferredScaleRatio(boolean doTrans, double scale) {
        return 0.2;
    }
    
    @Override
	public int getNumSimultaneousDownloads() {
        return 1;
    }
    
    @Override
	public TiledCRS getTiledCRS(CRS mapCRS) {
        if (mapCRS.equals(tCRS.baseCRS)) return tCRS;
        return null;
    }
    
    @Override
	public WMSLayersSource getService() {
    	return helper.src;
    }
    
    @Override
	public Object createThemeSpec(boolean doTrans, final int zoomLevel, final DisplayCoordinateAdapter dca) {
        WMSThemeSpec wts=helper.createThemeSpec(doTrans, null, tCRS.zoomLevels.scale(zoomLevel, dca.pixSizeInMicrons), dca.pixSizeInMicrons);
        if (wts==null) return null;
        return new WMSSpec(wts, zoomLevel);
    }
    
    @Override
	public boolean hasAnything(Object themeSpec) {
        if (themeSpec==null) return false;
        return true;
    }
    
    @Override
	public String createTileURL(Object themeSpec, int row, int column) {
        WMSSpec wSpec=(WMSSpec)themeSpec;
        int zoomLevel=wSpec.zoomLevel;
        double minX=tCRS.tileLeft(zoomLevel, column);
        double maxX=tCRS.tileRight(zoomLevel, column);
        double minY=tCRS.tileBottom(zoomLevel, row);
        double maxY=tCRS.tileTop(zoomLevel, row);
        DimI size=tCRS.tileSizeInPix(zoomLevel);
        
        String ret=helper.createRequestURL(wSpec.themeSpec, new Envelope(minX, minY, maxX, maxY), size);
        helper.lastTrans = wSpec.themeSpec.trans;
        
        return ret;
    }

    @Override
	public void addLayer(WMSLayer lyr) {
    	helper.addLayer(lyr);
    }

    @Override
	public boolean isCompletelyOpaque(Object themeSpec) {
    	if (themeSpec==null) return false;
    	WMSSpec ws=(WMSSpec)themeSpec;
        String spcStr=ws.themeSpec.preparedURL.toLowerCase();
        boolean isJpg=spcStr.indexOf("format=image/jpeg")>=0;
        boolean notTrans = !ws.themeSpec.trans;
        return isJpg || notTrans;
    }
	public boolean canAdd(WMSLayer lyr) {
		return helper.canAdd(lyr);
	}
}
