package com.sinergise.themebundle.generics.basic;

import com.google.gwt.core.client.GWT;
import com.sinergise.generics.gwt.resources.GenericsTheme;
import com.sinergise.generics.gwt.resources.GenericsTheme.GenericsThemeProvider;
import com.sinergise.generics.gwt.resources.GenericsThemeResources;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.BasicResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;
import com.sinergise.themebundle.ui.basic.popup.BasicPopupResources;

public class BasicGenericsThemeProvider extends BasicThemeProvider implements GenericsThemeProvider {
	@Override
	public void register() {
		GenericsTheme.initialize(this);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(BasicPopupResources.class);
	}

	@Override
	public ThemeResources getTheme() {
		return GWT.create(BasicResources.class);
	}
	
	@Override
	public GenericsThemeResources getGenericsTheme() {
		return GWT.create(BasicGenericsResources.class);
	}
}
