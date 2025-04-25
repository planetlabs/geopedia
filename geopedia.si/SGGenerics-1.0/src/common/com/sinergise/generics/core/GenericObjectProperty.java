package com.sinergise.generics.core;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.util.string.StringUtil;

public class GenericObjectProperty implements Serializable {		
	private static final long serialVersionUID = -218062521789016192L;
	
	private Map<String,String> attributes;
	
	public static GenericObjectProperty createSimpleAction (String name) {
		Map<String,String> attrs = new HashMap<String,String>();
		attrs.put(MetaAttributes.NAME, name);
		attrs.put(MetaAttributes.ELEMENT_TYPE, XMLTags.Action);
		return new GenericObjectProperty(attrs);
	}
	
	@Deprecated
	protected GenericObjectProperty() {}
	
	public GenericObjectProperty(Map<String,String> attributes) {
		this.attributes = attributes;
	}
	

	public Map<String,String> getAttributes() {
		return attributes;
	}
	
	public boolean isHidden() {
		return MetaAttributes.isTrue(attributes, MetaAttributes.HIDDEN);
	}
	
	public boolean isExportable() {
		if (MetaAttributes.hasAttribute(attributes, MetaAttributes.EXPORT_TO_FILE)) {
			return MetaAttributes.isTrue(attributes, MetaAttributes.EXPORT_TO_FILE);
		}
		return true;
	}

	public boolean isRequired() {
		if (MetaAttributes.isTrue(attributes,MetaAttributes.REQUIRED)) {
			return true;
		}
		return false;
	}
	public boolean isAction() {
		if (attributes.get(MetaAttributes.ELEMENT_TYPE)!=null && 
				XMLTags.Action.equalsIgnoreCase(attributes.get(MetaAttributes.ELEMENT_TYPE))) {
			return true;
		}
		return false;
	}
	public boolean isStub() {
		if (attributes.get(MetaAttributes.ELEMENT_TYPE)!=null && 
				XMLTags.Stub.equalsIgnoreCase(attributes.get(MetaAttributes.ELEMENT_TYPE))) {
			return true;
		}
		return false;
	}
	
	public String getName() {
		return attributes.get(MetaAttributes.NAME);
	}
	
	public String getType() {
		return attributes.get(MetaAttributes.TYPE);
	}
	
	public String getLabel() {
		return getLabel(attributes);
	}
	
	public static String getLabel(Map<String,String> attributes) {
		String label = attributes.get(MetaAttributes.LABEL);
		if (label!=null && label.length()>0)
			return label;
		else if (label!=null && label.length()==0)
			return "";
		if (attributes.get(MetaAttributes.NAME)!=null)
			return attributes.get(MetaAttributes.NAME);
		return "<unknown>";
		
	}
	
	public static void sort(ArrayList<GenericObjectProperty> toSort) {
		
		Collections.sort(toSort, new Comparator<GenericObjectProperty>() {

			@Override
			public int compare(GenericObjectProperty o1,
					GenericObjectProperty o2) {
				int p1= MetaAttributes.readIntAttr(o1.attributes, MetaAttributes.POSITION, Integer.MAX_VALUE);
				int p2= MetaAttributes.readIntAttr(o2.attributes, MetaAttributes.POSITION, Integer.MAX_VALUE);
				if (p1<p2)
					return -1;
				else if (p1>p2)
					return 1;
				return 0;
			}
		});
	}
	

	public int getPosition() {
		String position = getAttributes().get(MetaAttributes.POSITION);
		if (StringUtil.isNullOrEmpty(position))
			return 0;
		return Integer.valueOf(position);
	}
}
