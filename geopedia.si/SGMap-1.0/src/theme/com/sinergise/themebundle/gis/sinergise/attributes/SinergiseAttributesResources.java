package com.sinergise.themebundle.gis.sinergise.attributes;

import com.sinergise.themebundle.gis.basic.attributes.BasicAttributesResources;

public interface SinergiseAttributesResources extends BasicAttributesResources {
	@Override
	@Source({BasicAttributesResources.ATTRIBUTE_CSS, "attributesStyle.css"})
	AttributesCss attributesStyle();
}
