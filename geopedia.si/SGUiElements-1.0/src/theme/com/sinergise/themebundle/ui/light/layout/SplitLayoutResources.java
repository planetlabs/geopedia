package com.sinergise.themebundle.ui.light.layout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface SplitLayoutResources extends ClientBundle {

	public static SplitLayoutResources INSTANCE = GWT.create(SplitLayoutResources.class);
	
	public interface SplitLayoutCSS extends CssResource {}
	SplitLayoutCSS splitLayout();
	
	ImageResource dragEast();
	ImageResource dragWest();
	ImageResource dragNorth();
	ImageResource dragSouth();
}
