package com.sinergise.themebundle.ui.sinergise.popup;

import com.sinergise.themebundle.ui.basic.popup.BasicPopupResources;

public interface SinergisePopupResources extends BasicPopupResources {
	@Override
	@Source({BasicPopupResources.POPUP_CSS, "popupStyle.css"})
	PopupCss popupStyle();
	
}
