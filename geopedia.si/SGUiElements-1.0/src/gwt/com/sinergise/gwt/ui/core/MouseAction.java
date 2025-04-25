/*
 *
 */
package com.sinergise.gwt.ui.core;

import com.google.gwt.event.dom.client.MouseEvent;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.util.html.CSS;


public abstract class MouseAction extends Action {
    protected static final int NOT_SET_I = Integer.MIN_VALUE;
    
	protected int lastX = NOT_SET_I;
    protected int lastY = NOT_SET_I;

    protected boolean useDocumentCoords=false;
    protected boolean volatileParentPosition=false;
    
    protected boolean isFinal = true; // If the event is successfully triggered, no further events will be processed
    protected boolean isChainable = false; // other similar events may be processed after this event
    
    public MouseAction(String name) {
        super(name);
    }
    
    public int getCurrentModifiers() {
    	return MouseHandler.getModifiers();
    }
    
    
    public void setChainable(boolean isChainable) {
    	this.isChainable=isChainable;
    }
    
    /**
     * 
     * @return CSS-style cursor spec
     */
    public String getCursor() {
        Object ret=getProperty(CURSOR);
        if (ret==null) {
        	return CSS.CURSOR_DEFAULT;
        }
        return (String)ret;
    }
    
    public void setCursor(String cursor) {
        setProperty(CURSOR, cursor);
    }
    
    protected void checkParentPos(MouseHandler handler) {
        if (volatileParentPosition && !useDocumentCoords) {
        	handler.updateParentPosition();
        }
    }
    
    protected int calcX(MouseEvent<?> evt, MouseHandler handler) {
		if (useDocumentCoords) {
			return MouseHandler.getPageX(evt);
		}
		return handler.getElementX(evt);
    }

    protected int calcY(MouseEvent<?> evt, MouseHandler handler) {
		if (useDocumentCoords) {
			return MouseHandler.getPageY(evt);
		}
        return handler.getElementY(evt);
    }
    
    protected int calcX(MouseHandler handler, int docX) { 
        if (useDocumentCoords) {
        	return docX;
        }
        return handler.getElementX(docX);
    }

    protected int calcY(MouseHandler handler, int docY) { 
        if (useDocumentCoords) {
        	return docY;
        }
        return handler.getElementY(docY);
    }

}
