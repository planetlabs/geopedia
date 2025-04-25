package com.sinergise.generics.core;

public abstract class EntityTypeStorage {
	public  abstract EntityType getEntityType(Integer id);

	protected void initializeStorage(EntityTypeStorage ets) {
		AbstractEntityObject.setEntityTypeStorage(ets);
		ArrayValueHolder.setEntityTypeStorage(ets);
	}
}
