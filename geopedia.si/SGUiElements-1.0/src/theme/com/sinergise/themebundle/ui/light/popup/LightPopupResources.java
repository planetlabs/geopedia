package com.sinergise.themebundle.ui.light.popup;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface LightPopupResources extends PopupResources {
	String POPUP_CSS = "com/sinergise/themebundle/ui/light/popup/popupStyle.css";
	@Override
	@Source({ThemeResources.COLORS,POPUP_CSS})
	PopupCss popupStyle();
	
	ImageResource dateLeft();
	ImageResource dateRight();
	ImageResource yearRight();
	ImageResource yearLeft();
	
	ImageResource dialogClose();
	ImageResource dialogResize();
	ImageResource dialogResizeOn();
}
