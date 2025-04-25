package com.sinergise.gwt.gis.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

public interface PrintResources extends ClientBundle {
	
	@Shared
	public static interface PrintCss extends CssResource {
	}
	
	PrintCss printStyle();
}
