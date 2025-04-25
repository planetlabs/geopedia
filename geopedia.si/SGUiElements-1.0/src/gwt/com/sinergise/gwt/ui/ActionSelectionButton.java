package com.sinergise.gwt.ui;

import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ActionSelection;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.ui.menu.SGActionMenuItem;
import com.sinergise.gwt.ui.menu.SGMenu;

public class ActionSelectionButton extends ActionPushButton {

	private ActionSelection actionSelection;
	
	private SGMenu selectionMenu = null;
	
	public ActionSelectionButton (ActionSelection action) {
		super();
		this.actionSelection = action;
		setAction(createShowSelectionsAction());
	}
	
	private void showSelections() {
		getSelectionMenu().showUnder(this);
	}
	
	private SGMenu getSelectionMenu() {
		//lazy init as it might not be even used
		if (selectionMenu == null) {
			selectionMenu = new SGMenu();
			for (Action action : actionSelection.getSelections()) {
				selectionMenu.addItem(new SGActionMenuItem(action));
			}
		}
		
		return selectionMenu;
	}
	
	private Action createShowSelectionsAction() {
		final Action action = new Action(actionSelection.getName()) {
			@Override
			protected void actionPerformed() {
				showSelections();
			}
		};
		action.setDescription(actionSelection.getDescription());
		action.setIcon(actionSelection.getIconResource());
		action.setDisabledIcon(actionSelection.getDisabledIconResource());
		
		action.setExternalEnabled(actionSelection.isEnabled());
		
		actionSelection.addPropertyChangeListener(new PropertyChangeListener<Object>() {
			@Override
			public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
				action.setProperty(propertyName, newValue);
			}
		});
		
		return action;
	}
	
}
