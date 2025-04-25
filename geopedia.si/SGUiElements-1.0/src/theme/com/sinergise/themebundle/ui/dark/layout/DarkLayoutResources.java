package com.sinergise.themebundle.ui.dark.layout;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.ui.light.layout.LightLayoutResources;

public interface DarkLayoutResources extends LightLayoutResources {

	public static String DARK_LAYOUT_PATH = "com/sinergise/themebundle/ui/dark/layout/";
	
	//TODO: when needed, prepare some new style
//	@Override
//	@Source({LAYOUT_CSS, "layoutStyle.css"})
//	LayoutCss layoutStyle();
//	@Override
//	@Source({NEW_LAYOUT_CSS, "tabLayout.css"})
//	TabLayoutCss tabLayout();
	
	@Override
	public ImageResource closePanel();
	@Override
	public ImageResource openPanel();
	
}
