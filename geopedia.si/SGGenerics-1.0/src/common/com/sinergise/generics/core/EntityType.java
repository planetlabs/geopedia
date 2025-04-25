package com.sinergise.generics.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EntityType implements Serializable, IsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private int id;	
	private int primaryKeyId = Integer.MIN_VALUE;
	protected LinkedHashMap<String, TypeAttribute> attributesMap = new LinkedHashMap<String, TypeAttribute>();
	
	public EntityType() {
		name = "null";
		id = Integer.MIN_VALUE;
	}
	public EntityType (String type, int id) {
		this.name=type;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public int getAttributeCount() {
		return attributesMap.size();
	}
	
	public Map<String, TypeAttribute> getAttributesMap() {
		return attributesMap;
	}
	public Collection<TypeAttribute> getAttributes() {
		return attributesMap.values();
	}
	public TypeAttribute getAttribute(String name) {
		if (name==null || name.length()==0)
			return null;
		return attributesMap.get(name);
	}
	
	public TypeAttribute getAttribute(int id) {
		for (TypeAttribute ta:attributesMap.values()) {
			if (ta.getId()==id) {
				return ta;
			}
		}
		return null;
	}
	
	public int addTypeAttribute (TypeAttribute attribute) {
		if (attribute == null) throw new IllegalArgumentException("Attribute must not be null");
		if (hasAttribute(attribute.getName())) {
			throw new IllegalArgumentException("TypeAttribute '"+attribute.getName()+"' already exists in this Entity");
		}
		int id = attribute.getId();
		if (id ==Integer.MIN_VALUE) {
			id = attributesMap.size();
			attribute.setId(id);
		}
		attributesMap.put(attribute.getName(), attribute);
		return id;
	}
	
	public boolean hasAttribute(String name) {
		if (attributesMap.containsKey(name))
			return true;
		return false;
	}
	@Deprecated
	public int getPrimaryKeyId() {
		return primaryKeyId;
	}
	@Deprecated
	public void setPrimaryKeyId(int primaryKeyId) {
		this.primaryKeyId = primaryKeyId;
	}
	
	public int indexOf(String attName) {
		int i=0;
		for (String s : attributesMap.keySet()) {
			if (s.equals(attName)) return i;
			i++;
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return String.valueOf(name);
	}
	
}
