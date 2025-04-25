package com.sinergise.themebundle.ui.sigov.popup;

import com.sinergise.themebundle.ui.basic.popup.BasicPopupResources;

public interface SigovPopupResources extends BasicPopupResources {
	@Override
	@Source({BasicPopupResources.POPUP_CSS, "popupStyle.css"})
	PopupCss popupStyle();
	
}
