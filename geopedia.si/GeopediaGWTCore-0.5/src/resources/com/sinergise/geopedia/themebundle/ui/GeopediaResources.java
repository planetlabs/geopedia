package com.sinergise.geopedia.themebundle.ui;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.geopedia.themebundle.ui.button.GeopediaButtonResources;
import com.sinergise.geopedia.themebundle.ui.layout.GeopediaLayoutResources;
import com.sinergise.themebundle.ui.basic.BasicResources;

public interface GeopediaResources extends BasicResources {
	@Override
	@Source({BasicResources.DEFAULT_CSS, "defaultStyle.css"})
	ThemeCss defaultStyle();
	
	@Override
	public ImageResource processWorking();
	@Override
	GeopediaButtonResources buttonBundle();
	@Override
	GeopediaLayoutResources layoutBundle();
	@Override
	GeopediaStandardIcons standardIcons();
	
	ImageResource userWhite();
	ImageResource starWhite();
	ImageResource user();
	ImageResource star();
}
