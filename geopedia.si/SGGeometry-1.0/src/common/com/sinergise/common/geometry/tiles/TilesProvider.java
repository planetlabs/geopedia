/*
 *
 */
package com.sinergise.common.geometry.tiles;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


public interface TilesProvider {
    TiledCRS getTiledCRS(CRS mapCRS);
    String createTileURL(Object themeSpec, int row, int column);
    /**
     * 
     * @param doTrans
     * @param zoomLevel
     * @param dca
     * @return
     */
    Object createThemeSpec(boolean doTrans, int zoomLevel, DisplayCoordinateAdapter dca);
    boolean hasAnything(Object themeSpec); 
    boolean isCompletelyOpaque(Object themeSpec);
    /**
     * @return a number between 0 and 1. Smaller values mean that smaller scales are preferred (i.e. Images will be expanded). 
     */
    double getPreferredScaleRatio(boolean doTrans, double scale);
    
    /**
     * @return preferred maximum number of simultaneously requested tiles
     */
    int getNumSimultaneousDownloads();
}
