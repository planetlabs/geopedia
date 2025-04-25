package com.sinergise.themebundle.ui.dark.popup;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.themebundle.ui.light.popup.LightPopupResources;

public interface DarkPopupResources extends LightPopupResources {

	public static String DARK_POPUP_PATH = "com/sinergise/themebundle/ui/dark/popup/";
	//TODO: when needed, prepare some new style
//	@Override
//	@Source({POPUP_CSS, "popupStyle.css"})
//	PopupCss popupStyle();
	@Override
	public ImageResource dateLeft();
	@Override
	public ImageResource dateRight();
	@Override
	public ImageResource yearLeft();
	@Override
	public ImageResource yearRight();
}
