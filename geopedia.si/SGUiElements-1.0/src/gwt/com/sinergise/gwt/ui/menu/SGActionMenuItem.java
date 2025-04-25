package com.sinergise.gwt.ui.menu;

import static com.sinergise.common.ui.action.UIObjectInfo.ENABLED;
import static com.sinergise.common.ui.action.UIObjectInfo.ICON_16;
import static com.sinergise.common.ui.action.UIObjectInfo.ICON_RES_16;
import static com.sinergise.common.ui.action.UIObjectInfo.VISIBLE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;

public class SGActionMenuItem extends SGMenuItem implements PropertyChangeListener<Object> {
	
	public interface HasOptionsMenu {
		SGMenu getOptionsMenu();
	}

	private Action action;
	
	public SGActionMenuItem(Action action) {
		this(action, new Label(action.getDescription()));
	}
	
	public SGActionMenuItem(Action action, Widget itemWidget) {
		super(itemWidget);
		setAction(action);
	}
	
	protected void setAction(Action action) {
		this.action = action;
		if (action != null) {
			updateActionIcon();
			setEnabled(action.isEnabled());
			setVisible(action.isVisible());
			
			action.addPropertyChangeListener(this);
			if (action instanceof HasOptionsMenu) {
				setSubMenu(((HasOptionsMenu)action).getOptionsMenu());
			}
			updateStyle();
		}
	}
	
	private void updateStyle() {
		if (action instanceof ToggleAction) {
			if (((ToggleAction)action).isSelected()) {
				addStyleName("selected");
			} else {
				removeStyleName("selected");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
		if (sender != action) {
			return;
		}
		
		if (ICON_16.equals(propertyName) || ICON_RES_16.equals(propertyName)) {
			updateActionIcon();
		} else if (ENABLED.equals(propertyName) && newValue instanceof Boolean) {
			setEnabled(((Boolean)newValue).booleanValue());
		} else if (VISIBLE.equals(propertyName) && newValue instanceof Boolean) {
			setVisible(((Boolean)newValue).booleanValue());
		}
		updateStyle();
	}
	
	@SuppressWarnings("deprecation")
	private void updateActionIcon() {
		if (action.getIconResource() != null) {
			setItemIcon(new Image(action.getIconResource()));
		} else if (action.getIcon() != null) {
			setItemIcon(new Image(GWT.getModuleName()+"/"+action.getIcon()));
		}
	}
	
	protected boolean shouldHideParentOnAction() {
		return true;
	}
	
	@Override
	protected void onClick() {
		if (action != null && isEnabled()) {
			if (parent != null && shouldHideParentOnAction()) {
				parent.hide();
			}
			action.performAction();
		}
	}
}
