/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;



import com.google.gwt.core.client.GWT;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.gwt.ui.core.MouseDragAction;
import com.sinergise.gwt.util.html.CSS;


public class PanAction extends MouseDragAction {
    private double startWX;
    private double startWY;
    private DisplayCoordinateAdapter dca;
    private IMap map;
    
    public PanAction(IMap mp) {
        super("Pan");
        setCursor(CSS.CURSOR_HAND);
        setDragCursor("url('"+GWT.getModuleBaseURL()+"style/cur/pan.cur'), move");
        dca=mp.getCoordinateAdapter();
        map=mp;
    }
    
    @Override
	protected void dragEnd(int x, int y) {
    	startWX = Double.NaN;
    	startWY = Double.NaN;
        map.repaint(250);
    }

    @Override
	protected void dragMove(int x, int y) {
        if (Double.isNaN(startWX)) return;
        dca.pixPanWithReference(startWX, startWY, x, y);
    }

    @Override
	protected boolean dragStart(int x, int y) {
    	startWX = dca.worldFromPix.x(x);
    	startWY = dca.worldFromPix.y(y);
        return true;
    }

}
