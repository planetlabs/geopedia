package com.sinergise.themebundle.gis.sigov.attributes;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.attributes.BasicAttributesResources;

public interface SigovAttributesResources extends BasicAttributesResources {
	@Override
	@Source({BasicAttributesResources.ATTRIBUTE_CSS, "attributesStyle.css"})
	AttributesCss attributesStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource group();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource groupOver();
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource groupOn();
}
