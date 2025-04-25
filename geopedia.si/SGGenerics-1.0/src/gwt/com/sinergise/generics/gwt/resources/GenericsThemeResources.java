package com.sinergise.generics.gwt.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface GenericsThemeResources extends ClientBundle {
	
	public static interface GenericsThemeCss extends CssResource {
	}
	GenericsThemeCss genericsStyle();
	
	ImageResource excel();
	ImageResource csv();
}