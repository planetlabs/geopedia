/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.google.gwt.core.client.GWT;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;


public class RedrawAction extends Action {
    
    private static final AppMessages MESSAGES = (AppMessages) GWT.create(AppMessages.class);
    
    private final MapComponent map;
    public RedrawAction(MapComponent map) {
        super(MESSAGES.SpireMapControls_ACTION_REDRAW());
        setIcon(GisTheme.getGisTheme().gisStandardIcons().reload());
        this.map=map;
    }
    @Override
    protected void actionPerformed() {
    	map.context.invalidateLayers();
		map.repaint(0);
    }
}
