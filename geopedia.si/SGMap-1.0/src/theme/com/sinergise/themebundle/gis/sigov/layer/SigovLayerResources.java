package com.sinergise.themebundle.gis.sigov.layer;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.layer.BasicLayerResources;

public interface SigovLayerResources extends BasicLayerResources {
	@Override
	@Source({BasicLayerResources.LAYER_CSS, "layerStyle.css"})
	LayerCss layerStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource layerGroup();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource layerGroupOn();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource group();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource groupOver();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource groupOn();
}
