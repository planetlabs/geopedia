package com.sinergise.themebundle.gis.sinergise;

import com.sinergise.themebundle.gis.basic.BasicGisResources;
import com.sinergise.themebundle.gis.sinergise.attributes.SinergiseAttributesResources;
import com.sinergise.themebundle.gis.sinergise.layer.SinergiseLayerResources;
import com.sinergise.themebundle.gis.sinergise.nav.SinergiseNavResources;
import com.sinergise.themebundle.gis.sinergise.toolbar.SinergiseToolbarResources;
import com.sinergise.themebundle.ui.sinergise.SinergiseResources;

public interface SinergiseGisResources extends BasicGisResources, SinergiseResources {
	@Override
	@Source({BasicGisResources.GIS_DEFAULT_CSS, "gisStyle.css"})
	GisThemeCss gisStyle();
	
	@Override
	SinergiseAttributesResources attributesBundle();
	@Override
	SinergiseLayerResources layerBundle();
	@Override
	SinergiseToolbarResources toolbarBundle();
	@Override
	SinergiseNavResources navigationBundle();
	
}
