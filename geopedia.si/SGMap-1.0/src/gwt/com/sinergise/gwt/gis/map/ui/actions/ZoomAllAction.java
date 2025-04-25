/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;


public class ZoomAllAction extends Action {
    
    private MapComponent map;
    
    public ZoomAllAction(MapComponent map) {
        super(Tooltips.INSTANCE.toolbar_zoomAll());
        setIcon(GisTheme.getGisTheme().gisStandardIcons().zoomAll());
        setStyle("actionButZoomAll");
        this.map=map;
    }
    @Override
	protected void actionPerformed() {
        map.coords.zoomAll(true);
        map.repaint(100);
    }
}
