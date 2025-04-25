package com.sinergise.common.gis.map.ui;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.gis.map.render.SourcesRepaintEvents;

public interface IMap extends SourcesRepaintEvents {
	void repaint(int delayMillis);
	void refresh();
	void refresh(int delayMillis, boolean continuous);
	
	DisplayCoordinateAdapter getCoordinateAdapter();	
	public ScaleLevelsSpec getUserZooms();
	boolean isPrimary();
}
