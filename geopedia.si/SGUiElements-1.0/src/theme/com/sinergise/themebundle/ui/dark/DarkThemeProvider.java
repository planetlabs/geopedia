package com.sinergise.themebundle.ui.dark;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.dark.popup.DarkPopupResources;
import com.sinergise.themebundle.ui.light.LightThemeProvider;

public class DarkThemeProvider extends LightThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return GWT.create(DarkResources.class);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(DarkPopupResources.class);
	}
}
