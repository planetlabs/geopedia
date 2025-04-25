package com.sinergise.themebundle.ui.basic;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.ui.resources.GridResources;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.Theme.AbstractThemeProvider;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.layout.grid.BasicGridResources;
import com.sinergise.themebundle.ui.basic.popup.BasicPopupResources;

public class BasicThemeProvider extends AbstractThemeProvider {
	@Override
	public ThemeResources getTheme() {
		return GWT.create(BasicResources.class);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(BasicPopupResources.class);
	}

	public GridResources getGrid() {
		return GWT.create(BasicGridResources.class);
	}
}
