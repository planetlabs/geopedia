package com.sinergise.geopedia.themebundle.ui.layout;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.layout.BasicLayoutResources;

public interface GeopediaLayoutResources extends BasicLayoutResources {
	@Override
	@Source({LAYOUT_CSS, "layoutStyle.css"})
	LayoutCss layoutStyle();
	
	@Override
	@Source({BasicLayoutResources.NEW_LAYOUT_CSS, "tabLayout.css"})
	TabLayoutCss tabLayout();
	
	@Override
	ImageResource dragEast();
	@Override
	ImageResource dragWest();
	
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource splitWest();
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource splitEast();
	
	ImageResource borderTL();
	ImageResource borderTR();
	ImageResource borderBL();
	ImageResource borderBR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource borderTop();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource borderTopOpaque();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource borderBottom();
	
	@ImageOptions(preventInlining=true)
	ImageResource tabBg();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource tabLeft();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource tabRight();
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource tab();
}
