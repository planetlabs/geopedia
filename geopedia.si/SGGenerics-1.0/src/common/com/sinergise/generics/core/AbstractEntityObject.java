package com.sinergise.generics.core;

import static com.sinergise.common.util.lang.TypeUtil.boxI;
import static com.sinergise.generics.core.EntityObject.Status.NEW;

import java.io.Serializable;
import java.util.Arrays;

import com.sinergise.generics.core.util.EntityUtils;


public  class AbstractEntityObject implements EntityObject, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5915702404189306858L;
	protected Status status = NEW;
	protected ValueHolder[] values;
	protected  int entityTypeId = Integer.MIN_VALUE;
	protected static EntityTypeStorage etStorage; 
	protected AbstractEntityObject() {
	}

	/**
	 * Cloning constructor
	 * @param eo
	 */
	public AbstractEntityObject(EntityObject eo) {
		this.entityTypeId = eo.getTypeId();
		this.status = eo.getStatus();
		this.values = new ValueHolder[eo.getAttributeCount()];
		for (int id = 0; id < this.values.length; id++) {
			this.values[id] = EntityUtils.deepCopy(eo.getValue(id));
		}
	}

	public AbstractEntityObject(EntityType et) {
		this(et.getId());		
	}
	public AbstractEntityObject(int entityTypeId) {
		this.entityTypeId = entityTypeId;
		EntityType et = etStorage.getEntityType(boxI(entityTypeId));
		values = new ValueHolder[et.getAttributeCount()];
		status = NEW;
	}
	
	@Override
	public EntityObject getEntityValue(int id) {
		EntityObject eo = (EntityObject) values[id];
		return eo;
	}

	@Override
	public int getTypeId() {
		return entityTypeId;
	}
	
	@Override
	public int getAttributeCount() {
		if (values!=null) return values.length;
		return getType().getAttributeCount();
	}
	
	@Override
	public EntityType getType() {
		return etStorage.getEntityType(boxI(entityTypeId));
	}
	
	@Override
	public ValueHolder getValue(int id) {
		return values[id];
	}
	
	@Override
	public void setValue(int id, ValueHolder value) {
		values[id] = value;
	}

	
	@Override
	public String getPrimitiveValue(int id) {
		PrimitiveValue po = (PrimitiveValue)values[id];
		if (po!=null)
			return po.value;
		return null;
	}

	@Override
	public void setEntityValue(int id, EntityObject value) {
		values[id]= value;
	}

	@Override
	public void setPrimitiveValue(int id, String value) {
		PrimitiveValue pv = (PrimitiveValue)values[id];
		if (pv!=null)
			pv.value = value;
		else {
			pv = new PrimitiveValue(value);
			values[id]=pv;
		}
	}
	
	@Override
	public PrimitiveValueArray getPrimitiveValueArray(int id) {
		return (PrimitiveValueArray) values[id];
	}

	@Override
	public void setPrimitiveValueArray(int id, PrimitiveValueArray arry) {
		values[id] = arry;
	}

	@Override
	public EntityTypeStorage getEntityTypeStorage() {
		return etStorage;
	}
	
	public static void setEntityTypeStorage (EntityTypeStorage ets) {
		etStorage = ets;
	}
	
	@Override
	public String toString() {
		StringBuilder rv = new StringBuilder();
		for (ValueHolder vh:values) {
			if (vh instanceof PrimitiveValue) rv.append(vh).append(", ");
			else if (vh == null) rv.append("<null>").append(", ");
			else rv.append("<complex>");
		}
		return rv.toString();
	}

	@Override
	public Status getStatus() {
		return status;
	}
	
	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	
	@Override
	public boolean isNull() {
		if (values==null || values.length==0)
			return true;
		
		EntityType type=getType();
		for (TypeAttribute ta: type.getAttributes()) {
			if (ta.getPrimitiveType() == Types.BOOLEAN)
				continue;
			ValueHolder vh = getValue(ta.getId());
			if (vh!=null && !vh.isNull())
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + entityTypeId;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AbstractEntityObject other = (AbstractEntityObject)obj;
		if (entityTypeId != other.entityTypeId) return false;
		if (status != other.status) return false;
		if (!Arrays.equals(values, other.values)) return false;
		return true;
	}

}
