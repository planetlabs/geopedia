package com.sinergise.themebundle.ui.sinergise.layout;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.ui.basic.layout.BasicLayoutResources;

public interface SinergiseLayoutResources extends BasicLayoutResources {
	@Override
	@Source({BasicLayoutResources.LAYOUT_CSS, "layoutStyle.css"})
	LayoutCss layoutStyle();
	
	@Override
	@Source({BasicLayoutResources.NEW_LAYOUT_CSS, "tabLayout.css"})
	TabLayoutCss tabLayout();
	
	ImageResource logout();
	ImageResource logoutOver();
	
	ImageResource xOpen();
}
