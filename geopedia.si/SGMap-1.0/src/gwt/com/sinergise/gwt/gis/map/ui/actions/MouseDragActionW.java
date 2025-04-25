package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.ui.core.MouseDragAction;


public abstract class MouseDragActionW extends MouseDragAction implements HasCoordinate {
	
	protected final DisplayCoordinateAdapter dca;
    protected Position2D startW = null;
    protected final Position2D curW = new Position2D();
    protected final Position2D lastW = new Position2D();
	
	public MouseDragActionW(DisplayCoordinateAdapter dca, String name) {
		super(name);
		this.dca = dca;
	}
	
	@Override
	public double x() {
		return lastW.x;
	}
	
	@Override
	public double y() {
		return lastW.y;
	}
	
	@Override
	protected final boolean dragStart(int xPx, int yPx) {
		startW = new Position2D(dca.worldFromPix.x(xPx), dca.worldFromPix.y(yPx));
        lastW.x = startW.x;
        lastW.y = startW.y;
        curW.x = startW.x;
        curW.y = startW.y;
		return dragStartW(startW);
	}
	
	@Override
	protected final void dragEnd(int xPx, int yPx) {
		curW.x = dca.worldFromPix.x(xPx);
		curW.y = dca.worldFromPix.y(yPx);
		dragEndW(curW);
        lastW.setLocation(curW);
	}
	
	@Override
	protected final void dragMove(int xPx, int yPx) {
		curW.x = dca.worldFromPix.x(xPx);
		curW.y = dca.worldFromPix.y(yPx);
		dragMoveW(curW);
		lastW.setLocation(curW);
	}

	@Override
	protected void reset() {
		super.reset();
		startW = null;
	}
	
	protected abstract boolean dragStartW(Position2D startWorld);
	
	protected abstract void dragEndW(Position2D coordWorld);
	
	protected abstract void dragMoveW(Position2D coordWorld);
}
