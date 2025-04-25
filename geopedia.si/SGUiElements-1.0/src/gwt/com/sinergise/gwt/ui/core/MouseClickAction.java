package com.sinergise.gwt.ui.core;

abstract public class MouseClickAction extends MouseAction
{
    /** 
     * boolean property which, when true, disables dragging with the buttons this
     * action is registered to
     **/
    public static final String ALLOW_DRAG="allowDrag";
    
	public MouseClickAction(String name)
	{
		super(name);
	}

	@Override
	protected void actionPerformed()
	{
		mouseClicked(lastX, lastY);
	}

	abstract protected boolean mouseClicked(int x, int y);

	protected boolean handleClickScreen(int screenX, int screenY, MouseHandler handler) {
	    checkParentPos(handler);
	    return handleClick(calcX(handler, screenX), calcY(handler, screenY));
	}
	
	public boolean handleClick(int x, int y)
	{
		boolean ret = mouseClicked(x, y);
		lastX = x;
		lastY = y;
		return ret;
	}
	
	public boolean allowDrag() {
	    return getBooleanValue(ALLOW_DRAG, true);
	}
}
