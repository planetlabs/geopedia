package com.sinergise.themebundle.gis.light.layer;

import com.sinergise.gwt.gis.resources.LayerResources;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface LightLayerResources extends LayerResources {
	String LAYER_CSS = "com/sinergise/themebundle/gis/light/layer/layerStyle.css";
	@Override
	@Source({ThemeResources.COLORS,LAYER_CSS})
	LayerCss layerStyle();
}
