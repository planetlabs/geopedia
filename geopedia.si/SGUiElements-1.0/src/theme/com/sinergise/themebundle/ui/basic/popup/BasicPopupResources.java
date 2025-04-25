package com.sinergise.themebundle.ui.basic.popup;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.gwt.ui.resources.PopupResources;

public interface BasicPopupResources extends PopupResources {
	String POPUP_CSS = "com/sinergise/themebundle/ui/basic/popup/popupStyle.css";
	@Override
	PopupCss popupStyle();
	
	ImageResource dateLeft();
	ImageResource dateLeftOver();
	ImageResource dateRight();
	ImageResource dateRightOver();
	ImageResource yearRight();
	ImageResource yearRightOver();
	ImageResource yearLeft();
	ImageResource yearLeftOver();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource ie6fixBg();
	ImageResource ie6close();
	ImageResource ie6ff();
	ImageResource ie6chrome();
	ImageResource ie6safari();
	ImageResource ie6ie();
	ImageResource ie6opera();
	
	ImageResource actionClose();
	ImageResource actionCloseOver();
	
	ImageResource dialogResize();
	ImageResource dialogResizeOn();
}
