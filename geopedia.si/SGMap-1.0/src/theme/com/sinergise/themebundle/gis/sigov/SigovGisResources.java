package com.sinergise.themebundle.gis.sigov;

import com.sinergise.themebundle.gis.basic.BasicGisResources;
import com.sinergise.themebundle.gis.sigov.attributes.SigovAttributesResources;
import com.sinergise.themebundle.gis.sigov.layer.SigovLayerResources;
import com.sinergise.themebundle.gis.sigov.nav.SigovNavResources;
import com.sinergise.themebundle.gis.sigov.toolbar.SigovToolbarResources;
import com.sinergise.themebundle.ui.sigov.SigovResources;

public interface SigovGisResources extends BasicGisResources, SigovResources {
	@Override
	@Source({BasicGisResources.GIS_DEFAULT_CSS, "gisStyle.css"})
	GisThemeCss gisStyle();
	
	@Override
	SigovAttributesResources attributesBundle();
	@Override
	SigovLayerResources layerBundle();
	@Override
	SigovToolbarResources toolbarBundle();
	@Override
	SigovNavResources navigationBundle();
	@Override
	SigovGisIcons gisStandardIcons();
}
