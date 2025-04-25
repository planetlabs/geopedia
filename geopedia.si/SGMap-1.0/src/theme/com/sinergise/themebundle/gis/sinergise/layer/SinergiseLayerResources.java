package com.sinergise.themebundle.gis.sinergise.layer;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.layer.BasicLayerResources;

public interface SinergiseLayerResources extends BasicLayerResources {
	@Override
	@Source({BasicLayerResources.LAYER_CSS, "layerStyle.css"})
	LayerCss layerStyle();
	
	ImageResource layerOff();
	ImageResource layerOn();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource shadow();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal, preventInlining=true)
	ImageResource layerBg();
}

