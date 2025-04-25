package com.sinergise.generics.java;

import java.util.Map;

import org.w3c.dom.Element;

import com.sinergise.generics.core.filter.DataFilter;

public interface GenericsSettings {
	public Map<String, DataFilter> getFilters();
	public Map<String, Element> getEntityMetadataMap();
}
