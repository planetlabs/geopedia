package com.sinergise.themebundle.generics.sigov;

import com.google.gwt.core.client.GWT;
import com.sinergise.generics.gwt.resources.GenericsTheme;
import com.sinergise.generics.gwt.resources.GenericsTheme.GenericsThemeProvider;
import com.sinergise.generics.gwt.resources.GenericsThemeResources;
import com.sinergise.themebundle.ui.sigov.SigovThemeProvider;

public class SigovGenericsThemeProvider extends SigovThemeProvider implements GenericsThemeProvider {
	@Override
	public void register() {
		super.register();
		GenericsTheme.initialize(this);
	}
	
	@Override
	public GenericsThemeResources getGenericsTheme() {
		return GWT.create(SigovGenericsResources.class);
	}
}
