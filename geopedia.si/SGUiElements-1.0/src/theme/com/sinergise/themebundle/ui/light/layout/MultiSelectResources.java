package com.sinergise.themebundle.ui.light.layout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MultiSelectResources extends ClientBundle {

	public static MultiSelectResources INSTANCE = GWT.create(MultiSelectResources.class);
	
	public interface MultiSelectCSS extends CssResource {}
	MultiSelectCSS multiSelect();
}
