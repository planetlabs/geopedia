package com.sinergise.themebundle.ui.blue.layout;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.layout.BasicLayoutResources;

public interface BlueLayoutResources extends BasicLayoutResources {
	@Override
	@Source({BasicLayoutResources.LAYOUT_CSS, "layoutStyle.css"})
	LayoutCss layoutStyle();
	@Override
	@Source({BasicLayoutResources.NEW_LAYOUT_CSS, "tabLayout.css"})
	TabLayoutCss tabLayout();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource tabBg();
	@ImageOptions(preventInlining=true)
	ImageResource tabEdge();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource sub();
	ImageResource subL();
	ImageResource subR();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource subDown();
	ImageResource subDownL();
	ImageResource subDownR();
}
