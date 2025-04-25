package com.sinergise.generics.core;

import com.sinergise.common.util.settings.Settings.TypeMap;

@TypeMap(
		names={""},
		types={AbstractEntityObject.class}
		)
public interface EntityObject extends ValueHolder {
	
	public static enum Status{STORED, NEW, UPDATED, DELETED, IGNORE}
	
	public ValueHolder getValue (int id);
	public void setValue(int id, ValueHolder value);
	
	public String getPrimitiveValue (int id);
	public void setPrimitiveValue (int id, String value);
	
	public PrimitiveValueArray  getPrimitiveValueArray(int id);
	public void setPrimitiveValueArray(int id, PrimitiveValueArray arry);
	
	public EntityObject getEntityValue(int id);
	public void setEntityValue (int id, EntityObject value);
	
	public int getAttributeCount();
	
	public int getTypeId();
	public EntityType getType();
	public EntityTypeStorage getEntityTypeStorage();
	
	public Status getStatus();	
	public void setStatus(Status status);
}
