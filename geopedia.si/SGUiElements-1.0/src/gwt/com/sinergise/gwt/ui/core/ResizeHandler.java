package com.sinergise.gwt.ui.core;

import java.util.HashMap;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.sinergise.common.ui.core.IResizable;

/**
 * Handler for panel resizing actions.
 */
@Deprecated
public class ResizeHandler implements EventListener
{
	/**
	 * Resize directions
	 */
	public final static int NORTH = 1;
	public final static int EAST = 2;
	public final static int SOUTH = 4;
	public final static int WEST = 8;
	
	public final static int NORTHEAST = NORTH | EAST;
	public final static int SOUTHEAST = SOUTH | EAST;
	public final static int SOUTHWEST = SOUTH | WEST;
	public final static int NORTHWEST = NORTH | WEST;

	private static final int HANDLE_EVENTS = Event.MOUSEEVENTS | Event.ONCLICK;

	private HashMap<Element, Integer> handles = new HashMap<Element, Integer>();

	private IResizable resizableEl;
	private Element pressedHandle;
	private int pressedHandleDirection;
	
	int startX, startY, movedX, movedY;
	int origW, origH, origTop, origLeft;

	/**
	 * Default constructor
	 * 
	 * @param resizableEl
	 *            Resizable element. The element has to take care of registering
	 *            listeners and resize handles.
	 */
	public ResizeHandler(IResizable resizableEl)
	{
		this.resizableEl = resizableEl;
	}

	public void onBrowserEvent(Event event)
	{
		if (doNormalEvent(event)) {
			DOM.eventCancelBubble(event, true);
			DOM.eventPreventDefault(event);
		}
	}

	private boolean doNormalEvent(Event event)
	{
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN:
			return handleMouseDown(event);
		case Event.ONMOUSEUP:
			return handleMouseUp(event);
		case Event.ONMOUSEMOVE:
			return handleMouseMove(event);
		}
		return false;
	}

	private boolean handleMouseDown(Event event)
	{
		startX = DOM.eventGetClientX(event);
		startY = DOM.eventGetClientY(event);
		origW = resizableEl.getWidth();
		origH = resizableEl.getHeight();
		origLeft = resizableEl.getLeft();
		origTop = resizableEl.getTop();
		
		Element el = DOM.eventGetTarget(event);
		if (handles.get(el) != null && resizableEl.isResizable()) {
			handlePressed(el);
			return true;
		}
		return false;
	}

	private boolean handleMouseUp(Event event)
	{
		boolean ret = false;
		if (pressedHandle != null) {
			handleMouseMove(event);
			handleReleased(pressedHandle);
			ret = true;
		}

		return ret;
	}

	private boolean handleMouseMove(Event event)
	{
		boolean ret = false;
		if (pressedHandle != null) {
			int x = DOM.eventGetClientX(event);
			int y = DOM.eventGetClientY(event);
			scheduleResize(1, pressedHandleDirection, x, y);
			ret = true;
		}
		return ret;
	}

	private void handlePressed(Element handle)
	{
		pressedHandle = handle;
		pressedHandleDirection = handles.get(handle).intValue();
		DOM.setCapture(handle);
	}

	private void handleReleased(Element handle)
	{
		DOM.releaseCapture(handle);
		pressedHandle = null;
		pressedHandleDirection = 0;
	}

	private boolean resizeScheduled = false;

	private void scheduleResize(int delay, final int direction, final int x, final int y)
	{
		movedX = x;
		movedY = y;
		
		if (resizeScheduled)
			return;
		
		resizeScheduled = true;
		Timer t = new Timer() {
			@Override
			public void run()
			{
				doResize(direction);
				resizeScheduled = false;
			}
		};
		t.schedule(delay);
	}

	private void doResize(int direction)
	{
		int deltaX = movedX - startX;
		int deltaY = movedY - startY;
		
		int dw = 0;
		int dh = 0;
		int dx = 0;
		int dy = 0;
		
		if ((direction & NORTH) != 0) {
			dh = -deltaY;
		} else
		if ((direction & SOUTH) != 0) {
			dh = deltaY;
		}
		if ((direction & WEST) != 0) {
			dw = -deltaX;
		} else
		if ((direction & EAST) != 0) {
			dw = deltaX;
		}

		int newW = origW + dw;
		if (newW < resizableEl.getMinimalWidth()) {
			newW = resizableEl.getMinimalWidth();
		} else
		if (newW > resizableEl.getMaximalWidth()) {
			newW = resizableEl.getMaximalWidth();
		}
		if ((direction & WEST) != 0)
			dx = origW - newW;
		
		resizableEl.setWidth(newW);

	
		int newH = origH + dh;
		if (newH < resizableEl.getMinimalHeight()) {
			newH = resizableEl.getMinimalHeight();
		} else
		if (newH > resizableEl.getMaximalHeight()) {
			newH = resizableEl.getMaximalHeight();
		}
		if ((direction & NORTH) != 0)
			dy = origH - newH;

		resizableEl.setHeight(newH);

		if (dx != 0 || dy != 0) {
			int newL = origLeft + dx;
			int newT = origTop + dy;

			if (dx != 0)
				resizableEl.setLeft(newL);

			if (dy != 0)
				resizableEl.setTop(newT);
		}
	}

	public void registerHandle(Element el, int direction)
	{
		DOM.setEventListener(el, this);
		DOM.sinkEvents(el, HANDLE_EVENTS);
		DOM.setStyleAttribute(el, "cursor", getResizeCursor(direction));
		handles.put(el, Integer.valueOf(direction));
	}

	public boolean deregisterHandle(Element el)
	{
		Object ret = handles.remove(el);
		if (ret != null) {
			DOM.setEventListener(el, null);
			DOM.setStyleAttribute(el, "cursor", "default");
			return true;
		}
		return false;
	}

	private static String getResizeCursor(int direction)
	{
		switch (direction) {
		case NORTH:
			return "n-resize";
		case EAST:
			return "e-resize";
		case SOUTH:
			return "s-resize";
		case WEST:
			return "w-resize";
		case NORTHEAST:
			return "ne-resize";
		case SOUTHEAST:
			return "se-resize";
		case SOUTHWEST:
			return "sw-resize";
		case NORTHWEST:
			return "nw-resize";
		default:
			throw new IllegalArgumentException("Invalid resize direction.");
		}

	}
}
