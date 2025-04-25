package com.sinergise.themebundle.gis.light.attributes;

import com.sinergise.gwt.gis.resources.AttributesResources;
import com.sinergise.gwt.ui.resources.ThemeResources;

public interface LightAttributesResources extends AttributesResources {
	String ATTRIBUTE_CSS = "com/sinergise/themebundle/gis/light/attributes/attributesStyle.css";
	@Override
	@Source({ThemeResources.COLORS,ATTRIBUTE_CSS})
	AttributesCss attributesStyle();
}
