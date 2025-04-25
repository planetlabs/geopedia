package com.sinergise.generics.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.EntityTypeStorage;
import com.sinergise.generics.core.TypeAttribute;

public abstract class AbstractEntityTypeStorage extends EntityTypeStorage{
	private Map<Integer, EntityType> entityTypesMap = new HashMap<Integer, EntityType>();
	protected static AbstractEntityTypeStorage instance = null;
	
	public static AbstractEntityTypeStorage getInstance() {
		return instance;
	}
	
	
	@Override
	public EntityType getEntityType(Integer typeID) {
		return entityTypesMap.get(typeID);
	}
	
	public EntityType getEntityType (String name) {
		for (EntityType et:entityTypesMap.values()) {
			if (et.getName().equals(name))
				return et;
		}
		return null;
	}
	
	
	
	public static EntityObject fixObjects(EntityObject obj) {
		if (instance==null)
			throw new RuntimeException("No running instance found..");
		try {
			Class eoClass=instance.getClassForEntityId(obj.getTypeId());
			if (!(eoClass.isInstance(obj))) {
				obj = (EntityObject) eoClass.getConstructor(EntityObject.class).newInstance(obj);
			}
			EntityType et = obj.getType();
			for (TypeAttribute ta:et.getAttributes()) {
				if (!ta.isPrimitive()) {
					if (!ta.isArray()) {
						EntityObject eo = obj.getEntityValue(ta.getId());
						if (eo!=null ) {
							eo = AbstractEntityTypeStorage.fixObjects(eo);
							obj.setEntityValue(ta.getId(), eo);
						}
					} else {
						ArrayValueHolder avh = (ArrayValueHolder) obj.getValue(ta.getId());
						if (avh!=null) {
							for (int i=0;i<avh.size();i++) {
								avh.set(i, fixObjects((EntityObject)avh.get(i)));
							}
						}
					}
				}
			}
			return obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		throw new RuntimeException("Unable to fix objects");
	}
	
	
	public abstract Class getClassForEntityId(int id);
	public abstract Integer getIdForEntityName(String name);
	
	public EntityType createEntityType (String name) {
		Integer id=getIdForEntityName(name); // get from static mapping or something else
		EntityType ent = entityTypesMap.get(id);
		if (ent==null) {
			ent = new EntityType(name, id);
			entityTypesMap.put(id,ent);
		}
		return ent;
	}
	
	public void removeEntityType(String name) {
		Integer id = getIdForEntityName(name);
		if (id!=null) {
			System.out.println("Removed: "+entityTypesMap.remove(id));
		}
	}


	public Collection<EntityType> getTypes() {
		return entityTypesMap.values();
	}
	
	
	public void clear() {
		entityTypesMap.clear();
	}
}
