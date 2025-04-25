package com.sinergise.geopedia.themebundle.ui;

import com.google.gwt.core.client.GWT;
import com.sinergise.geopedia.themebundle.ui.popup.GeopediaPopupResources;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;

public class GeopediaThemeProvider extends BasicThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return GWT.create(GeopediaResources.class);
	}
	@Override
	public PopupResources getPopup() {
		return GWT.create(GeopediaPopupResources.class);
	}
}
