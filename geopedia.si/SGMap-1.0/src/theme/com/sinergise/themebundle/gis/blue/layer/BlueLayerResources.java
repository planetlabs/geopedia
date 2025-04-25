package com.sinergise.themebundle.gis.blue.layer;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.layer.BasicLayerResources;

public interface BlueLayerResources extends BasicLayerResources {
	@Override
	@Source({BasicLayerResources.LAYER_CSS, "layerStyle.css"})
	LayerCss layerStyle();
	
	ImageResource layerOff();
	ImageResource layerOn();
	
	ImageResource layerMini();
	ImageResource layerMiniOn();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource shadow();
}

