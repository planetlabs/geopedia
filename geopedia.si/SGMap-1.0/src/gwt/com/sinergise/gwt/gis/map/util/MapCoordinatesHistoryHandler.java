package com.sinergise.gwt.gis.map.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RepaintListenerAdapter;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.util.history.HistoryHandler;
import com.sinergise.gwt.util.history.HistoryManager;

/**
 * @author tcerovski
 *
 */
public class MapCoordinatesHistoryHandler implements HistoryHandler {
	
	private static final String HISTORY_PARAM_KEY_MAP_X = "map_x";
	private static final String HISTORY_PARAM_KEY_MAP_Y = "map_y";
	private static final String HISTORY_PARAM_KEY_MAP_SCALE = "map_sc";

	/**
	 * Binds to the default map coordinates history handler. 
	 * In effect all {@link MapComponent}s bound to the default handler will be synchronized.
	 */
	public static void bind(MapComponent map) {
		MapCoordinatesHistoryHandler handler = getDefaultHandler();
		handler.register(map);
		HistoryManager.getInstance().registerHandler(handler);
	}
	
	
	private static synchronized MapCoordinatesHistoryHandler getDefaultHandler() {
		if (defaultHandler == null) {
			defaultHandler = new MapCoordinatesHistoryHandler();
		}
		return defaultHandler;
	}
	
	private static MapCoordinatesHistoryHandler defaultHandler = null;
	
	private Collection<MapComponent> maps = new HashSet<MapComponent>();

	private MapCoordinatesHistoryHandler() {
		//hide public constructor
	}
	
	private void register(final MapComponent map) {
		maps.add(map);
		
		//listen for repaint to update the params
		map.addRepaintListener(new RepaintListenerAdapter() {
			@Override
			public void onRepaint(boolean hard) {
				if(hard) {
					HistoryManager.getInstance().setHistoryParams(
						new String[] {
							MapCoordinatesHistoryHandler.HISTORY_PARAM_KEY_MAP_X,
							MapCoordinatesHistoryHandler.HISTORY_PARAM_KEY_MAP_Y,
							MapCoordinatesHistoryHandler.HISTORY_PARAM_KEY_MAP_SCALE
						},
						new String[] {
							String.valueOf(map.coords.worldCenterX),
							String.valueOf(map.coords.worldCenterY),
							String.valueOf((int)map.coords.getScale())
						}
					);
				}
			}
		});
	}
	
	@Override
	public Collection<String> getHandledHistoryParams() {
		return Arrays.asList(HISTORY_PARAM_KEY_MAP_X, HISTORY_PARAM_KEY_MAP_Y, HISTORY_PARAM_KEY_MAP_SCALE);
	}

	@Override
	public void handleHistoryChange(HistoryManager manager) {
		Double x = null;
		Double y = null;
		Double sc = null;
		try {
			x = manager.getHistoryDoubleParam(HISTORY_PARAM_KEY_MAP_X);
			y = manager.getHistoryDoubleParam(HISTORY_PARAM_KEY_MAP_Y);
			sc = manager.getHistoryDoubleParam(HISTORY_PARAM_KEY_MAP_SCALE);
		} catch (Exception ignore) {}

		
		for (MapComponent map : maps) {
			DisplayCoordinateAdapter mapCoords = map.coords;
			
			if(!map.initialized() && x != null && y != null && sc != null) {
				map.context.setInitialView(x.doubleValue(), y.doubleValue(), sc.doubleValue());
				return;
			}
			
			
			boolean changed = false;
			if (x != null && y != null
				&&( x.doubleValue() != mapCoords.worldCenterX
				|| y.doubleValue() != mapCoords.worldCenterY)) 
			{
				
				if (sc != null && mapCoords.getScale() != sc.doubleValue()) {
					mapCoords.setWorldCenterAndScale(x.doubleValue(), y.doubleValue(), sc.doubleValue());
				} else {
					mapCoords.setWorldCenter(x.doubleValue(), y.doubleValue());
				}
				changed = true;
				
			} else if (sc != null && mapCoords.getScale() != sc.doubleValue()) {
				mapCoords.setScale(sc.doubleValue());
				changed = true;
			}
			
			if (changed) {
				map.repaint(100);
			}
		}
		
	}

}
