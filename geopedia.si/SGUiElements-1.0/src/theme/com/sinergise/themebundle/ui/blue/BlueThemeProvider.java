package com.sinergise.themebundle.ui.blue;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;
import com.sinergise.themebundle.ui.blue.popup.BluePopupResources;

public class BlueThemeProvider extends BasicThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return GWT.create(BlueResources.class);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(BluePopupResources.class);
	}
}
