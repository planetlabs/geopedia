/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;


import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.gis.map.ui.DisplayCoordUtilGWT;
import com.sinergise.gwt.gis.map.ui.controls.EffectElement;
import com.sinergise.gwt.gis.map.ui.controls.EffectsOverlay;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.core.MouseWheelAction;


public class ZoomWheelAction extends MouseWheelAction {
	private IMap map;
	private EffectsOverlay ovr;
	private EffectElement wheelBox = null;
	private boolean isThere = false;
	private double factor = 2.5;

	private Timer boxRemoveTimer = new Timer() {
		@Override
		public void run() {
			if (ovr.remove(wheelBox)) {
				isThere = false;
			}
			map.refresh();
			map.repaint(500);
		}
	};

	public ZoomWheelAction(IMap mp, EffectsOverlay ovr) {
		super("Zoom");
		setDescription("Scroll mouse wheel up to zoom in, down to zoom out");
		this.map = mp;
		this.ovr = ovr;
	}

	@Override
	protected void wheelMovedPreview(int x, int y, int curDelta) {
		boxRemoveTimer.cancel();
		if (curDelta == NOT_SET_I)
			return;
		DisplayCoordinateAdapter dca = map.getCoordinateAdapter();
		if (wheelBox == null) {
			Element wheelEl = ZoomBox.createZoomBox(StyleConsts.MAP_ZOOM_BOX_WHEEL);
			wheelBox = new EffectElement(wheelEl);
		}
		if (!isThere) {
			isThere=true;
			ovr.add(wheelBox);
		}
		DimI size = dca.pixDisplaySize;
		double sizeFactor=getZoomFactor(curDelta);
		int w = (int) (Math.min(size.w() / 3, 80) * (1.0 + 0.2*sizeFactor));
		int h = (int) (Math.min(size.h() / 3, 60) * (1.0 + 0.2*sizeFactor));
		wheelBox.setSizeInPix(w, h);
		wheelBox.setCenterInPix(x, y);
	}

	public static double getZoomFactor(int wheelDelta) {
		if (wheelDelta > 0) {
			return -0.5 - 0.15 * wheelDelta;
		} else if (wheelDelta < 0) {
			return 0.5 - 0.15 * wheelDelta;
		}
		return 0;
	}

	@Override
	protected boolean wheelMoved(int x, int y, int delta) {
		boxRemoveTimer.schedule(500);
		if (delta == 0 || delta == NOT_SET_I)
			return false;
		DisplayCoordinateAdapter dca = map.getCoordinateAdapter();
		double worldX = dca.worldFromPix.x(x);
		double worldY = dca.worldFromPix.y(y);

		int zoom = (int) Math.round(getZoomFactor(delta));
		DisplayCoordUtilGWT.zoomWithInteger(map, zoom, factor);
		double wDeltaX = dca.worldFromPix.x(x) - worldX;
		double wDeltaY = dca.worldFromPix.y(y) - worldY;
		dca.setWorldCenter(dca.worldCenterX - wDeltaX, dca.worldCenterY - wDeltaY);
		map.repaint(100);
		return true;
	}

}
