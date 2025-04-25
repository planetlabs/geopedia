package com.sinergise.themebundle.gis.basic.layer;

import com.sinergise.gwt.gis.resources.LayerResources;

public interface BasicLayerResources extends LayerResources {
	String LAYER_CSS = "com/sinergise/themebundle/gis/basic/layer/layerStyle.css";
	@Override
	LayerCss layerStyle();
}
