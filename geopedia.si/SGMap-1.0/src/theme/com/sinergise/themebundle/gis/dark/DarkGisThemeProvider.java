package com.sinergise.themebundle.gis.dark;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.GisTheme.GisThemeProvider;
import com.sinergise.themebundle.ui.dark.DarkThemeProvider;

public class DarkGisThemeProvider extends DarkThemeProvider implements GisThemeProvider {
	@Override
	public void register() {
		super.register();
		GisTheme.initialize(this);
	}
	
	@Override
	public DarkGisResources getGisTheme() {
		return GWT.create(DarkGisResources.class);
	}
}
