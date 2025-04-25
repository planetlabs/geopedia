package com.sinergise.themebundle.gis.sigov;

import com.google.gwt.core.client.GWT;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.GisTheme.GisThemeProvider;
import com.sinergise.gwt.gis.resources.GisThemeResources;
import com.sinergise.themebundle.ui.sigov.SigovThemeProvider;

public class SigovGisThemeProvider extends SigovThemeProvider implements GisThemeProvider {
	@Override
	public void register() {
		super.register();
		GisTheme.initialize(this);
	}

	@Override
	public GisThemeResources getGisTheme() {
		return GWT.create(SigovGisResources.class);
	}
}
