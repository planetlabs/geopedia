package com.sinergise.generics.gwt.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;

public class GWTWidgetInspector implements WidgetInspector {

	private EntityType et;
	Map<String, String> widgetMetadata;
	Map<String,Map<String,String>> entityAttributesMetadata = new HashMap<String,Map<String,String>>();
	
	public GWTWidgetInspector(EntityType et, Map<String,String> widgetMetadata) {
		this.et=et;
		this.widgetMetadata = widgetMetadata;
	}
	private WidgetInspectorListener wil;
	
	
	public Map<String,String> getAttributeMetadata(String attributeName){
		Map<String,String> amd = entityAttributesMetadata.get(attributeName);
		if (amd==null) {
			amd= new HashMap<String,String>();
			entityAttributesMetadata.put(attributeName, amd);
		}
		return amd;		
	}
	
	public void setAttributeMetaAttribute(String attributeName, String metaAttributeName, String value) {
		Map<String,String> amd = getAttributeMetadata(attributeName);
		amd.put(metaAttributeName, value);
	}
	
	@Override
	public void inspect() {
		ArrayList<GenericObjectProperty> sortedProps = new ArrayList<GenericObjectProperty>();	
		for (TypeAttribute att:et.getAttributes()) {
			Map<String,String> gopMap = getAttributeMetadata(att.getName());
			if (MetaAttributes.isTrue(gopMap.get(MetaAttributes.IGNORE)) ||
					MetaAttributes.isTrue(gopMap.get(MetaAttributes.HIDDEN))) 
					continue;
			gopMap.put(MetaAttributes.NAME, att.getName());
			gopMap.put(MetaAttributes.TYPE, Integer.toString(att.getPrimitiveType()));
			sortedProps.add(new GenericObjectProperty(gopMap));
		}
		GenericObjectProperty.sort(sortedProps);
		wil.inspectionCompleted(et, sortedProps, widgetMetadata);
	}

	@Override
	public void setWidgetInspectorListener(WidgetInspectorListener listener) {
		wil=listener;		
	}
	
	public EntityType getEntityType() {
		return et;
	}

	public Object getAttributeMetaAttribute(String attributeName, String metaAttributeName) {
		final Map<String, String> md = getAttributeMetadata(attributeName);
		if (md==null || md.isEmpty()) return null;
		return md.get(metaAttributeName);
	}
}
