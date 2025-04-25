/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;


import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.ui.DisplayCoordUtilGWT;
import com.sinergise.gwt.ui.core.MouseClickAction;


public class ZoomClickAction extends MouseClickAction {
    private IMap map;
    private double factor;
    public ZoomClickAction(IMap map, double factor) {
        super(factor>1?"Zoom Out":"Zoom In");
        this.map=map;
        this.factor=factor;
    }
    @Override
	protected boolean mouseClicked(int x, int y) {
        map.getCoordinateAdapter().setWorldCenter(map.getCoordinateAdapter().worldFromPix.x(x), map.getCoordinateAdapter().worldFromPix.y(y));
        DisplayCoordUtilGWT.zoomWithInteger(map, factor>1?-1:1, MathUtil.invertIfSmall(factor));
        return true;
    }

}
