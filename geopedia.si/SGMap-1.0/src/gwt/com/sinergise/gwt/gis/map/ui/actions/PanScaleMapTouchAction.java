package com.sinergise.gwt.gis.map.ui.actions;

import static java.lang.Double.isNaN;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.ui.IMap;
import com.sinergise.gwt.ui.gesture.action.PanScaleGestureAction;

/**
 * @author tcerovski
 *
 */
public class PanScaleMapTouchAction extends PanScaleGestureAction {
	
	private IMap map;
	private DisplayCoordinateAdapter dca;
	
	public PanScaleMapTouchAction(IMap map) {
		this.map = map;
		this.dca = map.getCoordinateAdapter();
	}

	private transient double startWorldX = Double.NaN;
	private transient double startWorldY = Double.NaN;
	private transient double startScale = Double.NaN;
	
	@Override
	public void onStart() {
		startWorldX = dca.worldCenterX;
		startWorldY = dca.worldCenterY;
		startScale = dca.getScale();
	}
	
	@Override
	public void onPanAndScale(int tx, int ty, double sc) {
		if (Double.isNaN(startWorldX) || Double.isNaN(startWorldY) || Double.isNaN(startScale)) {
			return;
		}
		
		double wtx = -dca.worldFromPix.length(tx);
		double wty = dca.worldFromPix.length(ty);
		dca.setWorldCenter(startWorldX + wtx, startWorldY + wty);
		
		if (sc > 0 && !isNaN(sc)) {
			dca.setScale(startScale / sc);
		}
		
		map.refresh(200, true);
	}

	int f=0;
	
	@Override
	public void onFinish() {
		startWorldX = Double.NaN;
		startWorldY = Double.NaN;
		startScale = Double.NaN;
		map.repaint(200);
	}

}
