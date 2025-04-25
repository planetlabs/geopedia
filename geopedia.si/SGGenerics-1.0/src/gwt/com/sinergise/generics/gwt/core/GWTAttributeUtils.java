package com.sinergise.generics.gwt.core;

import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;

public class GWTAttributeUtils {

	public static boolean isTrue(GenericObjectProperty gop, String attributeName) {
		String attribute = gop.getAttributes().get(attributeName);
		return MetaAttributes.isTrue(attribute);
	}
}
