package com.sinergise.themebundle.gis.light.toolbar;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.gis.resources.ToolbarResources;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface LightToolbarResources extends ToolbarResources {

	public static String TOOLBAR_CSS = "com/sinergise/themebundle/gis/light/toolbar/toolbar.css";
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	public ImageResource toolbar();
	public ImageResource toolbarRight();

	public ImageResource optionsButton();
	public ImageResource optionsButtonOn();
	
	@Override
	@Source({ThemeResources.COLORS,"toolbar.css"})
	public ToolbarCss toolbarStyle();
}
