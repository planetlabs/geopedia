package com.sinergise.themebundle.gis.sinergise.toolbar;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.gis.basic.toolbar.BasicToolbarResources;

public interface SinergiseToolbarResources extends BasicToolbarResources {
	@Override
	@Source({BasicToolbarResources.TOOLBAR_CSS, "toolbarStyle.css"})
	ToolbarCss toolbarStyle();
	
	ImageResource toolBtnOver();
	ImageResource toolBtnDown();
	ImageResource toolBtnDis();
}
