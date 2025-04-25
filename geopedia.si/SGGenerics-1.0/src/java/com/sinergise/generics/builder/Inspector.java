package com.sinergise.generics.builder;

import org.w3c.dom.Element;

public interface Inspector {
	public Element inspect (Object toInspect) throws InspectorException;
}
