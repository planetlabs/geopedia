package com.sinergise.themebundle.gis.blue;

import com.sinergise.themebundle.gis.basic.BasicGisResources;
import com.sinergise.themebundle.gis.blue.attributes.BlueAttributesResources;
import com.sinergise.themebundle.gis.blue.layer.BlueLayerResources;
import com.sinergise.themebundle.gis.blue.toolbar.BlueToolbarResources;

public interface BlueGisResources extends BasicGisResources {
	@Override
	@Source({BasicGisResources.GIS_DEFAULT_CSS, "gisStyle.css"})
	GisThemeCss gisStyle();
	
	@Override
	BlueAttributesResources attributesBundle();
	@Override
	BlueLayerResources layerBundle();
	@Override
	BlueToolbarResources toolbarBundle();
	
}
