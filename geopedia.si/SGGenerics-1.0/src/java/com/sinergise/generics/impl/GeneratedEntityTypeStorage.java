package com.sinergise.generics.impl;

import java.util.HashMap;

import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.java.AbstractEntityTypeStorage;

public class GeneratedEntityTypeStorage extends AbstractEntityTypeStorage{
	private int lastID;
	private HashMap<String,Integer> entityIdMap = new HashMap<String, Integer>();
	

	public GeneratedEntityTypeStorage() {
		lastID=0;
		initializeStorage(this);
	}
	
	public static GeneratedEntityTypeStorage getInstance() {
		if (instance == null) {
			instance = new GeneratedEntityTypeStorage();
		}
		return (GeneratedEntityTypeStorage)instance;
	}
	
	
	public EntityObject createEntityObject(int entityTypeId) {
		return new AbstractEntityObject(entityTypeId);
	}
	public EntityObject createEntityObject(EntityType entityType) {
		return createEntityObject(entityType.getId());
	}
	
	public EntityObject createEntityObject(String entityTypeName) {
		EntityType et = getEntityType(entityTypeName);
		if (et==null)
			return null;
		return createEntityObject(et);
	}
	
	@Override
	public Class getClassForEntityId(int id) {
		return null;
	}

	@Override
	public Integer getIdForEntityName(String name) {
		Integer id = entityIdMap.get(name);
		if (id==null) {
			id=lastID;
			entityIdMap.put(name, id);
			lastID++;
		}
		
		return id;
	}

	
	@Override
	public void removeEntityType(String name) {
		super.removeEntityType(name);
	}
	
	@Override
	public void clear() {
		super.clear();
		entityIdMap.clear();
		lastID=0;
	}
}
