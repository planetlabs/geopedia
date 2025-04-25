package com.sinergise.themebundle.gis.dark;

import com.sinergise.themebundle.gis.dark.attributes.DarkAttributesResources;
import com.sinergise.themebundle.gis.dark.layer.DarkLayerResources;
import com.sinergise.themebundle.gis.dark.nav.DarkNavResources;
import com.sinergise.themebundle.gis.dark.toolbar.DarkToolbarResources;
import com.sinergise.themebundle.gis.light.LightGisResources;
import com.sinergise.themebundle.ui.dark.DarkResources;

public interface DarkGisResources extends LightGisResources, DarkResources {
	
	//TODO: when needed, prepare some new style
//	@Override
//	@Source({GIS_DEFAULT_CSS, "gisStyle.css"})
//	GisThemeCss gisStyle();
	
	@Override
	DarkAttributesResources attributesBundle();
	@Override
	DarkLayerResources layerBundle();
	@Override
	DarkToolbarResources toolbarBundle();
	@Override
	DarkNavResources navigationBundle();
}
