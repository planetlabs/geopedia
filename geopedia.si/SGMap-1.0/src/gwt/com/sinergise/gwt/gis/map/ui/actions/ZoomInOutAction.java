/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.DisplayCoordUtilGWT;
import com.sinergise.gwt.gis.resources.GisTheme;


public class ZoomInOutAction extends Action {
    
    private final DisplayCoordinateAdapter dca;
    private IMap map;
    protected double factor;
    private boolean zoomIn=true;

    public ZoomInOutAction(IMap map, boolean zoomIn) {
        super(zoomIn?
                Tooltips.INSTANCE.toolbar_zoomIn():Tooltips.INSTANCE.toolbar_zoomOut());
        setIcon(zoomIn? GisTheme.getGisTheme().gisStandardIcons().zoomIn() : GisTheme.getGisTheme().gisStandardIcons().zoomOut());
        setStyle("actionButZoom"+(zoomIn?"In":"Out"));
        setProperty(LARGE_ICON_RES, zoomIn? GisTheme.getGisTheme().gisStandardIcons().zoomIn() : GisTheme.getGisTheme().gisStandardIcons().zoomOut());
        this.dca=map.getCoordinateAdapter();
        this.map=map;
        this.factor=zoomIn?(1.0/factor):factor;
        this.zoomIn=zoomIn;
        
        dca.addCoordinatesListener(new CoordinatesListener() {
            @Override
			public void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
                if (scaleChanged) updateEnabled();
            }
            @Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {
            }
        });
    }
    
    protected void updateEnabled() {
        if (zoomIn) {
            if (dca.worldLenPerDisp <= dca.bounds.minScale()) {
                setProperty(INTERNAL_ENABLED, Boolean.valueOf(false));
            } else {
                setProperty(INTERNAL_ENABLED, Boolean.valueOf(true));
            }
        } else {
            if (dca.worldLenPerDisp >= dca.bounds.maxScale()) {
                setProperty(INTERNAL_ENABLED, Boolean.valueOf(false));
            } else {
                setProperty(INTERNAL_ENABLED, Boolean.valueOf(true));
            }
        }
    }
    
    @Override
	protected void actionPerformed() {
        DisplayCoordUtilGWT.zoomWithInteger(map, zoomIn?1:-1, factor);
    }
}
