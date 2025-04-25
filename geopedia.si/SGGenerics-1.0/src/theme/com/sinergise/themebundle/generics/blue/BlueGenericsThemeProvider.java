package com.sinergise.themebundle.generics.blue;

import com.google.gwt.core.client.GWT;
import com.sinergise.generics.gwt.resources.GenericsTheme;
import com.sinergise.generics.gwt.resources.GenericsTheme.GenericsThemeProvider;
import com.sinergise.generics.gwt.resources.GenericsThemeResources;
import com.sinergise.themebundle.ui.blue.BlueThemeProvider;

public class BlueGenericsThemeProvider extends BlueThemeProvider implements GenericsThemeProvider {
	@Override
	public void register() {
		GenericsTheme.initialize(this);
	}
	
	@Override
	public GenericsThemeResources getGenericsTheme() {
		return GWT.create(BlueGenericsResources.class);
	}
}
