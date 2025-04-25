package com.sinergise.themebundle.gis.blue.toolbar;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.toolbar.BasicToolbarResources;

public interface BlueToolbarResources extends BasicToolbarResources {
	@Override
	@Source({BasicToolbarResources.TOOLBAR_CSS, "toolbarStyle.css"})
	ToolbarCss toolbarStyle();
	
	ImageResource toolBtnOver();
	ImageResource toolBtnDown();
	
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource toolbar();
	@Override
	ImageResource toolbarRight();
}
