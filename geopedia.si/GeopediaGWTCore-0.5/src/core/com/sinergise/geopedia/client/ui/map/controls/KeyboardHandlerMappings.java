package com.sinergise.geopedia.client.ui.map.controls;

import com.google.gwt.event.dom.client.KeyCodes;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.ui.DisplayCoordUtilGWT;
import com.sinergise.gwt.gis.map.ui.IMapComponent;



public class KeyboardHandlerMappings {

	public static void setup(final IMapComponent mapComponent, KeyboardHandler keyboardHandler) {

		final DisplayCoordinateAdapter dca = mapComponent.getCoordinateAdapter();
		KeyboardHandler.Listener arrowsListener = new KeyboardHandler.Listener() {
			
			private final int DELTA = 20;
			
			public void handleKey(int keyCode) {
				
				int dx = 0;
				int dy = 0;
				
				switch (keyCode) {
					case KeyCodes.KEY_UP: {
						dy = DELTA;
						break;
					}
					case KeyCodes.KEY_DOWN: {
						dy = -DELTA;
						break;
					}
					case KeyCodes.KEY_LEFT: {
						dx = -DELTA;
						break;
					}
					case KeyCodes.KEY_RIGHT: {
						dx = DELTA;
						break;
					}
					default: {
						// do nothing
					}
				}
				
				pixPan(dx, dy);
			}
			
			private void pixPan(int dx, int dy) {
				if (dx != 0 || dy != 0) {
					dca.pixPan(dx, dy);
				}
			}
		};
		
		
		keyboardHandler.register(KeyCodes.KEY_UP, arrowsListener);
		keyboardHandler.register(KeyCodes.KEY_DOWN, arrowsListener);
		keyboardHandler.register(KeyCodes.KEY_LEFT, arrowsListener);
		keyboardHandler.register(KeyCodes.KEY_RIGHT, arrowsListener);
		
		
		KeyboardHandler.Listener zoomListener = new KeyboardHandler.Listener() {

			public void handleKey(int keyCode) {
				if (keyCode == KeyCodes.KEY_PAGEUP) {
					zoomIn();
				}
				if (keyCode == KeyCodes.KEY_PAGEDOWN) {
					zoomOut();
				}
			}

			private void zoomIn() {			
		        DisplayCoordUtilGWT.zoomWithInteger(mapComponent, 1, MathUtil.invertIfSmall(1));
			}

			private void zoomOut() {
		        DisplayCoordUtilGWT.zoomWithInteger(mapComponent, -1, MathUtil.invertIfSmall(0.1));
			}
		};
		
		
		keyboardHandler.register(KeyCodes.KEY_PAGEUP, zoomListener);
		keyboardHandler.register(KeyCodes.KEY_PAGEDOWN, zoomListener);
	}
}
