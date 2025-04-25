package com.sinergise.gwt.gis.map.ui;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.gwt.ui.core.MouseHandler;

public interface IMapComponent extends IMap {
	public MouseHandler getMouseHandler();
	public IOverlaysHolder getOverlaysHolder();
	public Widget getParent();		
}
