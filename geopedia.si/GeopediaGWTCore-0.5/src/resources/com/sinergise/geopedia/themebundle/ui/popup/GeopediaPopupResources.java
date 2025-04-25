package com.sinergise.geopedia.themebundle.ui.popup;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.ui.basic.popup.BasicPopupResources;

public interface GeopediaPopupResources extends BasicPopupResources {
	@Override
	@Source({BasicPopupResources.POPUP_CSS, "popupStyle.css"})
	PopupCss popupStyle();
	
	ImageResource TL();
	ImageResource TR();
	ImageResource BL();
	ImageResource BR();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource top();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource bottom();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource left();
	@ImageOptions(repeatStyle=RepeatStyle.Vertical)
	ImageResource right();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource dragger();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource shadow();	
}
