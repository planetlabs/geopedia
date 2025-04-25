/*
 *
 */
package com.sinergise.gwt.ui.core;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.MouseMoveEvent;

abstract public class MouseMoveAction extends MouseAction {
    protected transient int curX;
    protected transient int curY;
    private final ScheduledCommand tmr;
    private boolean scheduled = false;
    
    public MouseMoveAction(String name) {
        super(name);
		tmr = new ScheduledCommand() {
			public void execute() {
            	scheduled = false;
				if (lastX == curX && lastY == curY) {
					return;
				}
                mouseMoved(curX, curY);
                lastX=curX;
                lastY=curY;
            }
        };
    }
    
    protected abstract void mouseMoved(int x, int y);
    
    void handleMove(MouseMoveEvent evt, MouseHandler handler) {
        checkParentPos(handler);
        internalMove(calcX(evt,handler), calcY(evt,handler));
    }
    @Override
	protected void actionPerformed() {
        mouseMoved(lastX, lastY);
    }
    private boolean internalMove(int x, int y) {
        if (curX == x && curY == y) {
        	return true;
        }
        curX=x;
        curY=y;
        if (lastX == x && lastY == y) {
        	return true;
        }
        if (!scheduled) {
        	scheduled = true;
        	Scheduler.get().scheduleDeferred(tmr);
        }
        return true;
    }
}
