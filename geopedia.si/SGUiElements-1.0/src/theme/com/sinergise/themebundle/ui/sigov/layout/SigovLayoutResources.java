package com.sinergise.themebundle.ui.sigov.layout;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.layout.BasicLayoutResources;

public interface SigovLayoutResources extends BasicLayoutResources {
	@Override
	@Source({BasicLayoutResources.LAYOUT_CSS, "layoutStyle.css"})
	LayoutCss layoutStyle();
	@Override
	@Source({BasicLayoutResources.NEW_LAYOUT_CSS, "tabLayout.css"})
	TabLayoutCss tabLayout();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both, preventInlining=true)
	ImageResource tabBg();
	@Override
	@ImageOptions(repeatStyle=RepeatStyle.Both, preventInlining=true)
	ImageResource tab();
	@ImageOptions(repeatStyle=RepeatStyle.Both, preventInlining=true)
	ImageResource tabAct();
	
	@ImageOptions(preventInlining=true, repeatStyle=RepeatStyle.Both)
	ImageResource qsBg();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource subTab();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource subTabOn();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource subTabOver();
	
	@Override
	public ImageResource openPanel();
	@Override
	public ImageResource closePanel();
}
