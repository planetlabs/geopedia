/*
 *
 */
package com.sinergise.gwt.ui.core;

import com.google.gwt.event.dom.client.MouseEvent;

public abstract class MouseDragAction extends MouseAction {
	public static final String DRAG_CURSOR = "dragCursor";
	public static final String DRAG_DELAY = "dragDelay";

	protected int startX = NOT_SET_I;
	protected int startY = NOT_SET_I;

	protected boolean dragging = false;

	public MouseDragAction(String name) {
		super(name);
		setDragDelay(30);
	}

	public void setDragCursor(String cursor) {
		setProperty(DRAG_CURSOR, cursor);
	}	
	
	@Override
	public MouseDragAction setProperty(String name, Object value) {
		super.setProperty(name, value);
		if (DRAG_DELAY.equals(name) && tmr != null) {
			tmr.setDelay(getDragDelay());
		}
		return this;
	}

	public void setDragDelay(int delay) {
		setProperty(DRAG_DELAY, Integer.valueOf(delay));
	}

	public int getDragDelay() {
		Object delay = getProperty(DRAG_DELAY);
		if (delay == null) { return 0; }
		return ((Integer)delay).intValue();
	}

	/**
	 * @param x
	 * @param y
	 * @return <code>true</code> if dragging started
	 */
	abstract protected boolean dragStart(int x, int y);

	abstract protected void dragMove(int x, int y);

	abstract protected void dragEnd(int x, int y);

	/**
	 * @param evt
	 * @return true iff event bubbling should be cancelled
	 */
	protected boolean handleStartScreen(int screenX, int screenY, MouseHandler handler) {
		checkParentPos(handler);
		handler.updateParentPosition();
		boolean ret = internalStart(calcX(handler, screenX), calcY(handler, screenY));
		return ret;
	}

	protected boolean handleEndScreen(int screenX, int screenY, MouseHandler handler) {
		checkParentPos(handler);
		boolean ret = internalEnd(calcX(handler, screenX), calcY(handler, screenY));
		handler.updateParentPosition();
		return ret;
	}

	private int curX;
	private int curY;

	private TimerExt tmr = new TimerExt(1, false) {
		@Override
		public void execute() {
			internalMove();
		}
	};

	protected void handleMoveScreen(MouseEvent<?> evt, MouseHandler handler) {
		checkParentPos(handler);
		curX = calcX(evt, handler);
		curY = calcY(evt, handler);
		tmr.schedule();
	}

	protected void reset() {
		startX = NOT_SET_I;
		startY = NOT_SET_I;
		dragging = false;
	}

	private boolean internalStart(int x, int y) {
		lastX = startX = x;
		lastY = startY = y;
		boolean ret = dragStart(x, y);
		if (ret) {
			dragging = true;
		} else {
			reset();
		}
		return ret;
	}

	private void internalMove() {
		if (curX != lastX || curY != lastY) {
			dragMove(curX, curY);
			lastX = curX;
			lastY = curY;
		}
	}

	private boolean internalEnd(int x, int y) {
		// Do the move in case we filtered something
		tmr.cancel();
		tmr.execute();

		dragging = false;
		dragEnd(x, y);
		lastX = x;
		lastY = y;
		reset();
		return true;
	}

	@Override
	protected final void actionPerformed() {
		if (lastX != NOT_SET_I && lastY != NOT_SET_I) {
			internalEnd(lastX, lastY);
		}
	}

	public boolean isDragging() {
		return dragging;
	}

	public String getDragCursor() {
		Object ret = getProperty(DRAG_CURSOR);
		if (ret == null) return getCursor();
		return (String)ret;
	}

}
