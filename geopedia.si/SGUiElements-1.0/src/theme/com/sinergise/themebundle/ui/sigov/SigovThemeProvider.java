package com.sinergise.themebundle.ui.sigov;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;
import com.sinergise.themebundle.ui.sigov.popup.SigovPopupResources;

public class SigovThemeProvider extends BasicThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return (ThemeResources)GWT.create(SigovResources.class);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(SigovPopupResources.class);
	}
}
