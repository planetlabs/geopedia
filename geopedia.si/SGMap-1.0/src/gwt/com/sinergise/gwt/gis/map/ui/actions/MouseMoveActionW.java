package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.ui.core.MouseMoveAction;

public abstract class MouseMoveActionW extends MouseMoveAction implements HasCoordinate {

	protected final DisplayCoordinateAdapter dca;

    protected final Position2D lastW = new Position2D();
    protected final Position2D curW = new Position2D();

	public MouseMoveActionW(DisplayCoordinateAdapter dca, String name) {
		super(name);
		this.dca = dca;
	}
	
	@Override
	public double x() {
		return lastW.x();
	}
	
	@Override
	public double y() {
		return lastW.y();
	}

	@Override
	protected final void mouseMoved(int xPx, int yPx) {
		curW.x = dca.worldFromPix.x(xPx);
		curW.y = dca.worldFromPix.y(yPx);
		if (!curW.equals2D(lastW)) {
			mouseMovedW(curW);
			lastW.setLocation(curW);
		}
	}
	
	protected abstract void mouseMovedW(Position2D curPos);
}
