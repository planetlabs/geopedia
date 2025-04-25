/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.google.gwt.core.client.GWT;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.gwt.gis.map.ui.controls.EffectElement;
import com.sinergise.gwt.gis.map.ui.controls.EffectsOverlay;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.core.MouseDragAction;


public class ZoomBoxAction extends MouseDragAction {
    private EffectElement zoomBox;
    private IMap map; 
    private EffectsOverlay ovr;
    
    public ZoomBoxAction(IMap map, EffectsOverlay ovr) {
        super("Zoom");
        //TODO: Make sure that FeatureInfo action overrides this if it is enabled by default on left-click
        setCursor("url('"+GWT.getModuleBaseURL()+"style/cur/zoom.cur'), default");
        this.map=map;
        this.ovr=ovr;
        setStyle("mapToolbarZoomAction");
    }
    
    @Override
	protected void dragEnd(int x, int y) {
        DisplayCoordinateAdapter dca=map.getCoordinateAdapter();
        double sX=dca.worldFromPix.x(startX);
        double sY=dca.worldFromPix.y(startY);
        double curX=dca.worldFromPix.x(x);
        double curY=dca.worldFromPix.y(y);
        ovr.remove(zoomBox);
        map.getCoordinateAdapter().setDisplayedRect(sX, sY, curX, curY, false);
        map.repaint(100);
    }

    @Override
	protected void dragMove(int x, int y) {
        zoomBox.setBoundsInPix(startX, startY, x, y);
    }

    @Override
	protected boolean dragStart(int x, int y) {
        if (zoomBox==null) {
            zoomBox=new EffectElement(ZoomBox.createZoomBox(StyleConsts.MAP_ZOOM_BOX));
        }
        ovr.add(zoomBox);
        zoomBox.setTopLeftInPix(x, y);
        return true;
    }

}
