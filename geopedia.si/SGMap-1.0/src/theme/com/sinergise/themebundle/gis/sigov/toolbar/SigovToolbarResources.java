package com.sinergise.themebundle.gis.sigov.toolbar;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.toolbar.BasicToolbarResources;

public interface SigovToolbarResources extends BasicToolbarResources {
	@Override
	@Source({BasicToolbarResources.TOOLBAR_CSS, "toolbarStyle.css"})
	ToolbarCss toolbarStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource separator();
	
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource toolbar();
	@Override
	ImageResource toolbarRight();
}
