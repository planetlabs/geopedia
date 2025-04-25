package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.resources.GisTheme;


public class MoveToPointAction extends Action {
	public final MapComponent map;
	public double x = Double.NaN;
	public double y = Double.NaN;
	public final double minScale;
	public final double maxScale;
	
	public MoveToPointAction(MapComponent mc) {
		this(mc, null);
	}
	
	public MoveToPointAction(MapComponent mc, Point center) {
		this(mc, center == null ? Double.NaN : center.x, center == null ? Double.NaN : center.y);
	}
	public MoveToPointAction(MapComponent mc, double x, double y) {
		this(mc, x, y, 0, Double.POSITIVE_INFINITY);
	}
	public MoveToPointAction(MapComponent mc, double x, double y, double minScale, double maxScale) {
		super(AppMessages.INSTANCE.MapActions_MoveToPoint_Name());
		setIcon(GisTheme.getGisTheme().gisStandardIcons().centerXY());
		this.x=x;
		this.y=y;
		this.map=mc;
		this.minScale=minScale;
		this.maxScale=maxScale;
	}
	@Override
	protected void actionPerformed() {
		moveTo(x, y);
	}
	
	protected void moveTo(double cx, double cy) {
		if (Double.isNaN(cx) || Double.isNaN(cy)) {
			return;
		}
		map.setCenter(cx, cy);
		if (map.getScale() < minScale) {
			map.setScale(minScale);
			
		} else if (map.getScale() > maxScale) {
			map.setScale(maxScale);
		}
		map.repaint(500);
	}
}
