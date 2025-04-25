package com.sinergise.themebundle.ui.light;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.ui.resources.GridResources;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.light.layout.grid.LightGridResources;
import com.sinergise.themebundle.ui.light.popup.LightPopupResources;

public class LightThemeProvider extends Theme.AbstractThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return GWT.create(LightResources.class);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(LightPopupResources.class);
	}
	
	public GridResources getGrid() {
		return GWT.create(LightGridResources.class);
	}
}
