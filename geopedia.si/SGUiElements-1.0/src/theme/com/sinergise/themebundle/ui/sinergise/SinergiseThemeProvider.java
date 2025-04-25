package com.sinergise.themebundle.ui.sinergise;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;
import com.sinergise.themebundle.ui.sinergise.popup.SinergisePopupResources;

public class SinergiseThemeProvider extends BasicThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return (ThemeResources)GWT.create(SinergiseResources.class);
	}
	@Override
	public PopupResources getPopup() {
		return GWT.create(SinergisePopupResources.class);
	}
}
