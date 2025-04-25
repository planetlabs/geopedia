package com.sinergise.gwt.ui.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface ButtonResources extends ClientBundle {
	
	@Shared
	public static interface ButtonCss extends CssResource {
		String down();
		String disabled();
		String icon();

		String btn();
		String txt();
		String spanImg();
		String inlineButtonPanel();
	}
	
	ImageResource pin();
	ImageResource pinned();
	
	ButtonCss buttonStyle();
}
