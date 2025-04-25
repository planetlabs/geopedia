package com.sinergise.themebundle.ui.blue.popup;

import com.sinergise.themebundle.ui.basic.popup.BasicPopupResources;

public interface BluePopupResources extends BasicPopupResources {
	@Override
	@Source({BasicPopupResources.POPUP_CSS, "popupStyle.css"})
	PopupCss popupStyle();
}
