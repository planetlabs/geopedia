package com.sinergise.gwt.ui.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface PopupResources extends ClientBundle {
	
	@Shared
	public static interface PopupCss extends CssResource {
	}
	
	PopupCss popupStyle();
}
