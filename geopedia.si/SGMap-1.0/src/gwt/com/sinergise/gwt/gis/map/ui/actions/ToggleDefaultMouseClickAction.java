package com.sinergise.gwt.gis.map.ui.actions;

import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseHandler;

public class ToggleDefaultMouseClickAction extends ToggleAction {
	
	protected final MouseHandler 	mouser;
	protected MouseClickAction 		leftClickAct;
	protected MouseClickAction 		rightClickAct = new MouseClickAction("CancelPickFeatures") {
		@Override
		protected boolean mouseClicked(int x, int y) {
			setSelected(false);
			return false;
		}
	};
	
	protected ToggleDefaultMouseClickAction(MapComponent map) {
		super("ToggleDefaultMouseClickAction");
		this.mouser = map.getMouseHandler();
	}

	public ToggleDefaultMouseClickAction(MapComponent map, MouseClickAction leftClickAction) {
		this(map);
		this.leftClickAct = leftClickAction;
	}
	
	public void setLeftClickAct(MouseClickAction leftClickAct) {
		this.leftClickAct = leftClickAct;
	}
	
	public void setRightClickAct(MouseClickAction rightClickAct) {
		this.rightClickAct = rightClickAct;
	}
	
	@Override
	protected void selectionChanged(boolean newSelected) {
		if (newSelected) {
            startMode();
        } else {
            endMode();
        }
	}
	
	private void endMode() {
        mouser.deregisterAction(leftClickAct);
        mouser.deregisterAction(rightClickAct);
    }

    private void startMode() {
    	mouser.registerAction(leftClickAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
        mouser.registerAction(rightClickAct, MouseHandler.BUTTON_RIGHT, MouseHandler.MOD_ANY, 1);
    }
	
	protected void toggleOff() {
		setSelected(false);
	}

}
