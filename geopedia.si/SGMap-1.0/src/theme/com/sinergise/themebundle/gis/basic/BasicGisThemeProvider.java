package com.sinergise.themebundle.gis.basic;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.GisTheme.GisThemeProvider;
import com.sinergise.gwt.gis.resources.GisThemeResources;
import com.sinergise.themebundle.ui.basic.BasicThemeProvider;

public class BasicGisThemeProvider extends BasicThemeProvider implements GisThemeProvider {
	@Override
	public void register() {
		GisTheme.initialize(this);
	}
	
	@Override
	public GisThemeResources getGisTheme() {
		return GWT.create(BasicGisResources.class);
	}
}
