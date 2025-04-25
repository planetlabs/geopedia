package com.sinergise.themebundle.gis.blue.attributes;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.sinergise.themebundle.gis.basic.attributes.BasicAttributesResources;

public interface BlueAttributesResources extends BasicAttributesResources {
	@Override
	@Source({BasicAttributesResources.ATTRIBUTE_CSS, "attributesStyle.css"})
	AttributesCss attributesStyle();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource sub();
	ImageResource subL();
	ImageResource subR();
	
	@ImageOptions(repeatStyle=RepeatStyle.Horizontal)
	ImageResource subDown();
	ImageResource subDownL();
	ImageResource subDownR();
}
