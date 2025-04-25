package com.sinergise.themebundle.gis.basic.toolbar;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.gis.resources.ToolbarResources;

public interface BasicToolbarResources extends ToolbarResources {
	String TOOLBAR_CSS = "com/sinergise/themebundle/gis/basic/toolbar/toolbarStyle.css";
	@Override
	ToolbarCss toolbarStyle();
	
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource toolbar();
	ImageResource toolbarRight();
	
	ImageResource optionsButton();
	ImageResource optionsButtonOn();
}
