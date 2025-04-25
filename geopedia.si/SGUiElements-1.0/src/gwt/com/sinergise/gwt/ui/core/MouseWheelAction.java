/*
 *
 */
package com.sinergise.gwt.ui.core;



abstract public class MouseWheelAction extends MouseAction {
    protected int lastDelta=0;
    
    public MouseWheelAction(String name) {
        super(name);
    }
    /**
     * @see ExtDOM#eventGetWheelDelta(com.google.gwt.user.client.Event);
     * @param delta positive for up, negative for down
     */
    abstract protected boolean wheelMoved(int x, int y, int delta);
    
    /**
     * Preview of wheel actions (while the user is scrolling)
     */
    @SuppressWarnings("unused")
	protected void wheelMovedPreview(int x, int y, int curDelta) {
    }

    public void handlePreview(int docX, int docY, int curDelta, MouseHandler handler) {
        checkParentPos(handler);
        wheelMovedPreview(calcX(handler, docX), calcY(handler, docY), curDelta);
    }
    
    public void handleMove(int docX, int docY, int wheelDelta, MouseHandler handler) {
        checkParentPos(handler);
        internalMove(wheelDelta, calcX(handler, docX), calcY(handler, docY));
    }
    
    private void internalMove(int delta, int x, int y) {
        if (wheelMoved(x, y, delta)) {
            lastDelta=delta;
            lastX=x;
            lastY=y;
            return;
        }
        return;
    }
    
    @Override
	protected void actionPerformed() {
        wheelMoved(lastDelta, lastX, lastY);
    }

}
