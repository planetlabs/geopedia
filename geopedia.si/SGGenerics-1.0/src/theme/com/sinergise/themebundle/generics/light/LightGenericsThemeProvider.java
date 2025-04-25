package com.sinergise.themebundle.generics.light;

import com.google.gwt.core.client.GWT;
import com.sinergise.generics.gwt.resources.GenericsTheme;
import com.sinergise.generics.gwt.resources.GenericsTheme.GenericsThemeProvider;
import com.sinergise.generics.gwt.resources.GenericsThemeResources;
import com.sinergise.gwt.ui.resources.PopupResources;
import com.sinergise.gwt.ui.resources.ThemeResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;
import com.sinergise.themebundle.ui.light.LightResources;
import com.sinergise.themebundle.ui.light.popup.LightPopupResources;

public class LightGenericsThemeProvider extends BasicThemeProvider implements GenericsThemeProvider {
	@Override
	public void register() {
		super.register();
		GenericsTheme.initialize(this);
	}
	
	@Override
	public PopupResources getPopup() {
		return GWT.create(LightPopupResources.class);
	}

	@Override
	public ThemeResources getTheme() {
		return GWT.create(LightResources.class);
	}
	
	@Override
	public GenericsThemeResources getGenericsTheme() {
		return GWT.create(LightGenericsResources.class);
	}
}
