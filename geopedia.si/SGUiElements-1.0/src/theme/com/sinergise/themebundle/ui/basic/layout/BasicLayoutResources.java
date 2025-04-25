package com.sinergise.themebundle.ui.basic.layout;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.resources.LayoutResources;

public interface BasicLayoutResources extends LayoutResources {
	String LAYOUT_CSS = "com/sinergise/themebundle/ui/basic/layout/layoutStyle.css";
	String NEW_LAYOUT_CSS = "com/sinergise/themebundle/ui/basic/layout/tabLayout.css";
	String LAYOUT_COMPONENTS_CSS = "com/sinergise/themebundle/ui/basic/layout/layoutComponents.css";
	
	@Override
	LayoutCss layoutStyle();
	@Override
	TabLayoutCss tabLayout();
	
	@Override
	LayoutComponentsCss layoutComponents();
	
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource splitWest();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource splitEast();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource splitNorth();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource splitSouth();
	
	ImageResource dragEast();
	ImageResource dragWest();
	ImageResource dragNorth();
	ImageResource dragSouth();
	
	ImageResource tab();
	ImageResource tabSelect();
	ImageResource closeTabs();
	
	ImageResource qsSubmit();
	ImageResource qsSubmitHover();
	ImageResource qsIe();
	ImageResource qsIeRight();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource shadow();
	
	ImageResource filterIcon();
	ImageResource filterIcon2();
	ImageResource filterIcon3();
	
	@Override
	ImageResource closePanel();
	@Override
	ImageResource openPanel();
}
