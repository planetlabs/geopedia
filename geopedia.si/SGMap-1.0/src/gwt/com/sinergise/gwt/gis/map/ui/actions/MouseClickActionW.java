package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.ui.core.MouseClickAction;

public abstract class MouseClickActionW extends MouseClickAction implements HasCoordinate {

	protected final DisplayCoordinateAdapter dca;

	protected double lastXw = Double.NaN;
	protected double lastYw = Double.NaN;

	public MouseClickActionW(DisplayCoordinateAdapter dca, String name) {
		super(name);
		this.dca = dca;
	}
	
	@Override
	protected boolean mouseClicked(int xPx, int yPx) {
		lastXw = dca.worldFromPix.x(xPx);
		lastYw = dca.worldFromPix.y(yPx);
		return mouseClickedW(lastXw, lastYw);
	}
	
	@Override
	public double x() {
		return lastXw;
	}
	
	@Override
	public double y() {
		return lastYw;
	}

	protected abstract boolean mouseClickedW(double xWorld, double yWorld);
}
