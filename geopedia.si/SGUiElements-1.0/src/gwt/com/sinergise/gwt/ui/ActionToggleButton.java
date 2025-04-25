package com.sinergise.gwt.ui;

import static com.sinergise.common.ui.action.ToggleAction.PROP_SELECTED;
import static com.sinergise.common.ui.action.ToggleAction.PROP_SELECTED_ICON;
import static com.sinergise.common.ui.action.ToggleAction.PROP_SELECTED_ICON_RES;
import static com.sinergise.common.ui.action.UIObjectInfo.DESC;
import static com.sinergise.common.ui.action.UIObjectInfo.DISABLED_ICON_16;
import static com.sinergise.common.ui.action.UIObjectInfo.DISABLED_ICON_RES_16;
import static com.sinergise.common.ui.action.UIObjectInfo.ENABLED;
import static com.sinergise.common.ui.action.UIObjectInfo.ICON_16;
import static com.sinergise.common.ui.action.UIObjectInfo.ICON_RES_16;
import static com.sinergise.common.ui.action.UIObjectInfo.NAME;
import static com.sinergise.common.ui.action.UIObjectInfo.VISIBLE;
import static com.sinergise.gwt.ui.ActionUtilGWT.createImageForCustomButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.string.StringUtil;


public class ActionToggleButton extends ToggleButton {
	
	protected Action myAction;

	private PropertyChangeListener<Object> propListener = new PropertyChangeListener<Object>() {
		public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {

			if (ENABLED.equals(propertyName)) {
				setEnabled(myAction.isEnabled());
				
			} else if (PROP_SELECTED.equals(propertyName)) {
				setDown(ToggleAction.isSelected(myAction));
				
			} else if (useActionForDisplay) {
				 // All ifs below will not be applied if the link between action and display is disabled
				if (VISIBLE.equals(propertyName)) {
					setVisible(myAction.isVisible());

				} else if (NAME.equals(propertyName)) {
					update();

				} else if (DESC.equals(propertyName)) {
					setTitle(myAction.getDescription());
					
				} else if (ICON_16.equals(propertyName)) {
					setEnabledIcon(myAction.getIcon());

				} else if (ICON_RES_16.equals(propertyName)) {
					setEnabledIcon(myAction.getIconResource());

				} else if (DISABLED_ICON_16.equals(propertyName)) {
					setDisabledIcon(myAction.getDisabledIcon());

				} else if (DISABLED_ICON_RES_16.equals(propertyName)) {
					setDisabledIcon(myAction.getDisabledIconResource());
					
				} else if (PROP_SELECTED_ICON.equals(propertyName)) {
					setSelectedIcon(ToggleAction.getSelectedIcon(myAction));

				} else if (PROP_SELECTED_ICON_RES.equals(propertyName)) {
					setSelectedIcon(ToggleAction.getSelectedIconResource(myAction));
				}
			}
		}
	};
	
	public ActionToggleButton(Action act) {
		super();
		setAction(act);
		
		//prevents propagation to e.g. DisclosurePanel
		addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				event.stopPropagation();
			}
		});
	}

	@Override
	protected void onClick() {
		super.onClick();
		ToggleAction.setSelected(myAction, isDown());
	}

	@Override
	public void setDown(boolean down) {
		super.setDown(down);
		ToggleAction.setSelected(myAction, isDown());
	}

	public void setAction(Action act) {
		if (myAction != null) {
			myAction.removePropertyChangeListener(propListener);
		}
		myAction = act;
		myAction.addPropertyChangeListener(propListener);
		update();
	}

	public void setDisabledIcon(String disabledIcon) {
		if (disabledIcon == null || disabledIcon.length()==0) return;
		if (disabledIcon.equals(myAction.getIcon())) return;
		getUpDisabledFace().setImage(createImageForCustomButton(GWT.getModuleBaseURL()+disabledIcon));
	}

	public void setDisabledIcon(ImageResource disabledIcon) {
		if (disabledIcon == null) return;
		if (disabledIcon.equals(myAction.getIconResource())) return;
		getUpDisabledFace().setImage(new Image(disabledIcon));
	}
	
	@Deprecated
	public void setEnabledIcon(String iconURL) {
		String desc = myAction.getDescription();
		if (iconURL != null && iconURL.length()!=0) {
			getUpFace().setImage(createImageForCustomButton(GWT.getModuleBaseURL()+iconURL));
		} else {
			getUpFace().setText(myAction.getName());
		}
		setTitle(desc == null ? "" : desc);
	}
	
	public void setEnabledIcon(ImageResource icon) {
		setEnabledIcon(icon == null ? null : new Image(icon));
	}
	
	public void setEnabledIcon(Image icon) {
		String desc = myAction.getDescription();
		if (icon != null) {
			getUpFace().setImage(icon);
		} else {
			getUpFace().setText(myAction.getName());
		}
		setTitle(desc == null ? "" : desc);
	}

	public void setSelectedIcon(String iconURL) {
		if (iconURL==null || iconURL.length()==0 || iconURL.equals(myAction.getIcon())) {
			return;
		}
		getDownFace().setImage(createImageForCustomButton(GWT.getModuleBaseURL()+iconURL));
	}
	public void setSelectedIcon(ImageResource icon) {
		if (icon==null || icon.equals(myAction.getIconResource())) {
			return;
		}
		getDownFace().setImage(new Image(icon));
	}
	
	private void update() {
		if (useActionForDisplay) {
			Image img = getActionIconImage();
			String desc = myAction.getDescription();
			if (img != null && myAction.getName() != null) {
				setTitle(myAction.getName());
			} else {
				setTitle(desc != null ? desc : "");
			}
			if (myAction.getStyle() != null) addStyleName(myAction.getStyle());
			if (myAction.getPrimaryStyle() != null) setStylePrimaryName(myAction.getPrimaryStyle());

			setEnabledIcon(img);
			setDisabledIcon((String)myAction.getProperty(DISABLED_ICON_16));
			setSelectedIcon((String)myAction.getProperty(PROP_SELECTED_ICON));
		}
		setVisible(myAction.isVisible());
		setEnabled(myAction.isEnabled());
		setDown(ToggleAction.isSelected(myAction));
	}
	
	private Image getActionIconImage() {
		ImageResource res = myAction.getIconResource();
		if (res != null) {
			return new Image(res);
		}
		String iconURL = myAction.getIcon();
		if (StringUtil.isNullOrEmpty(iconURL)) {
			return null;
		}
		return createImageForCustomButton(GWT.getModuleBaseURL()+iconURL);
	}

	private boolean useActionForDisplay = true;
	
	public void setUseActionForDisplay(boolean useActionForDisplay) {
		this.useActionForDisplay = useActionForDisplay;
	}
}
