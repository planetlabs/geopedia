package com.sinergise.gwt.gis.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface ToolbarResources extends ClientBundle {
	
	@Shared
	public static interface ToolbarCss extends CssResource {
	}
	
	ToolbarCss toolbarStyle();
}
