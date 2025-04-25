package com.sinergise.gwt.ui;

import static com.sinergise.gwt.ui.ActionUtilGWT.createImageForCustomButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.UIObjectInfo;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.util.UtilGWT;


public class ActionPushButton extends PushButton {
	
	protected Action myAction;

	private PropertyChangeListener<Object> propListener = new PropertyChangeListener<Object>() {
		public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
			if (UIObjectInfo.ENABLED.equals(propertyName)) {
				setEnabled(myAction.isEnabled());
				
			} else if (UIObjectInfo.VISIBLE.equals(propertyName)) {
				setVisible(myAction.isVisible());
				
			} else if (UIObjectInfo.NAME.equals(propertyName)) {
				update();
				
			} else if (UIObjectInfo.DESC.equals(propertyName)) {
				setTitle(myAction.getDescription());
				
			} else if (UIObjectInfo.ICON_16.equals(propertyName)) {
				setEnabledIcon(myAction.getIcon());
				
			} else if (UIObjectInfo.ICON_RES_16.equals(propertyName)) {
				setEnabledIcon(myAction.getIconResource());

			} else if (UIObjectInfo.DISABLED_ICON_16.equals(propertyName) && !isEnabled()) {
				setDisabledIcon(myAction.getDisabledIcon());

			} else if (UIObjectInfo.DISABLED_ICON_RES_16.equals(propertyName) && !isEnabled()) {
				setDisabledIcon(myAction.getDisabledIconResource());

			}
		}
	};
	
	protected ActionPushButton() {
		super();
		
		//prevents propagation to i.e. DisclosurePanel
		addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				event.stopPropagation();
			}
		});
	}
	
	public ActionPushButton(Action act) {
		this();
		setAction(act);
	}

	@Override
	protected void onClick() {
		super.onClick();
		myAction.performAction();
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
	
	public void setEnabledIcon(String iconURL) {
		setEnabledIcon(StringUtil.isNullOrEmpty(iconURL) ? null : createImageForCustomButton(GWT.getModuleBaseURL()+iconURL));
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

	
	private void update() {
		Image img = getActionIconImage();
		String desc = myAction.getDescription();
		if (img != null && myAction.getName() != null) {
			setTitle(myAction.getName());
		} else {
			setTitle(desc != null ? desc : "");
		}
		setEnabledIcon(img);
		setDisabledIcon((String)myAction.getProperty(UIObjectInfo.DISABLED_ICON_16));
		setVisible(myAction.isVisible());
		setEnabled(myAction.isEnabled());
		if(myAction.getStyle() != null) {
			addStyleName(myAction.getStyle());
		}
		if(myAction.getPrimaryStyle() != null) {
			setStylePrimaryName(myAction.getPrimaryStyle());
		}
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
		return createImageForCustomButton(UtilGWT.getAbsoluteUrlFromModuleBase(iconURL));
	}
}
