/*
 *
 */
package com.sinergise.gwt.gis.geopedia;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TilesProvider;
import com.sinergise.common.gis.geopedia.GeopediaLayer;
import com.sinergise.common.gis.geopedia.GeopediaLayersSource;
import com.sinergise.common.gis.geopedia.GeopediaLayersSource.GeopediaStaticSpec;
import com.sinergise.common.util.string.StringUtil;


public class GeopediaStaticRenderer implements TilesProvider {
    private final String path; 
    private final String ext;
    final GeopediaLayersSource.GeopediaStaticSpec spec;
    final GeopediaLayer lyr;
    
    public GeopediaStaticRenderer(GeopediaLayer layer) {
        this.spec = (GeopediaStaticSpec)layer.getSpec();
        this.lyr=layer;
        String normalizedPath = spec.getFullPath();
        if (!StringUtil.isNullOrEmpty(normalizedPath) && (normalizedPath.endsWith("/") || normalizedPath.endsWith("\\"))) {
        	normalizedPath = normalizedPath.substring(0, normalizedPath.length()-1);
        }
        this.path = normalizedPath;
        ext = "."+spec.getImageExtension();
    }
    @Override
	public double getPreferredScaleRatio(boolean doTrans, double scale) {
        return 0.5;
    }
    
    @Override
	public boolean hasAnything(Object themeSpec) {
        return themeSpec!=null;
    }
    
    @Override
	public Object createThemeSpec(boolean doTrans, int zoomLevel, DisplayCoordinateAdapter dca) {
        if (lyr==null) return null;
        if (!lyr.hasAnythingToRender(dca)) return null;
        return new Integer(zoomLevel);
    }
    
    @Override
	public boolean isCompletelyOpaque(Object themeSpec) {
    	return lyr.isOpaque();
    }

    @Override
	public String createTileURL(Object themeSpec, int row, int column) {
        if (themeSpec==null) return null;
        int zoomLevel=((Integer)themeSpec).intValue();
        return path + '/' + TileUtilGWT.tileInDirColRow(spec.getCRS(), zoomLevel, column, row) + ext;
    }
    
    @Override
	public int getNumSimultaneousDownloads() {
        return 10;
    }

    @Override
	public TiledCRS getTiledCRS(CRS mapCRS) {
        return spec.getCRS();
    }
    
}
