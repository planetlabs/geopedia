package com.sinergise.generics.core;

import java.io.Serializable;

public class TypeAttribute implements Serializable {
	
	public enum DatabaseIgnore{FULL, READONLY, WRITEONLY, NONE}
	/**
	 * 
	 */
	private static final long serialVersionUID = 6895383327665503568L;
	
	private int id;
	private String name;
	private int primitiveType = Integer.MIN_VALUE;
	private int entityType = Integer.MIN_VALUE;
	private boolean isArray = false;
	private DatabaseIgnore dbIgnore = DatabaseIgnore.NONE;
	private transient int sqlType = Integer.MIN_VALUE;
	
	protected TypeAttribute() {
		id = Integer.MIN_VALUE;
		primitiveType = Integer.MIN_VALUE;
		entityType = Integer.MIN_VALUE;
		isArray=false;
	}
	
	public TypeAttribute (int id, String name, int type) {
		this(id,name,type,true);
	}
	
	public TypeAttribute (int id, String name, int type, boolean isPrimitiveType) {
		this.id=id;
		this.name = name;
		if (isPrimitiveType)
			this.primitiveType = type;
		else
			this.entityType = type;
	}
	public TypeAttribute (int id, String name, int type, int sqlType, boolean isPrimitiveType) {
		this.id=id;
		this.name = name;
		if (isPrimitiveType)
		{
			this.primitiveType = type;
			setSQLType(sqlType);
		}
		else
			this.entityType = type;
	}
	public TypeAttribute (int id, String name, int type, boolean isPrimitiveType, boolean isArray) {
		this(id,name,type,isPrimitiveType);
		this.isArray=isArray;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id=id;
	}
	public String getName() {
		return name;
	}

	public boolean isPrimitive() {
		if (primitiveType!=Integer.MIN_VALUE)
			return true;
		return false;
	}
	public int getPrimitiveType() {
		return primitiveType;
	}
	public int getEntityType() {
		return entityType;
	}
	
	public boolean isArray() {
		return isArray;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TypeAttribute))
			return false;
		TypeAttribute other = (TypeAttribute) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void setDBIgnore(DatabaseIgnore dbIgnore) {
	 this.dbIgnore = dbIgnore;	
	}
	
	public boolean dbCanRead() {
		if (dbIgnore==DatabaseIgnore.NONE || dbIgnore==DatabaseIgnore.READONLY)
			return true;
		return false;
	}
	
	public boolean dbCanWrite() {
		if (dbIgnore==DatabaseIgnore.NONE || dbIgnore==DatabaseIgnore.WRITEONLY)
			return true;
		return false;
	}
	

	public void setSQLType(int type) {
		this.sqlType=type;
	}
	public int getSQLType() {
		return sqlType;
	}
}
