package com.sinergise.generics.gwt.core;

import com.sinergise.generics.core.EntityObject;

public class EntityObjectToken {

	public static EntityObjectToken EMPTY_TOKEN = new EntityObjectToken();

	private EntityObject entityObject = null;
	private int tokenIndex = Integer.MIN_VALUE;
			
	public EntityObjectToken(){
		entityObject = null;
		tokenIndex = Integer.MIN_VALUE;
	}
	
	public EntityObjectToken(int index, EntityObject object) {
		this.entityObject = object;
		this.tokenIndex = index;
	}
	public boolean exists(){
		if (tokenIndex==Integer.MIN_VALUE)
			return false;
		return true;
	}

	public void setEntityObject(EntityObject entityObject) {
		this.entityObject = entityObject;
	}

	public EntityObject getEntityObject() {
		return entityObject;
	}

	public int getTokenIndex() {
		return tokenIndex;
	}

	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}
	
	// bad match but should do..
	public boolean equals(EntityObjectToken token){
		if (token==null)
			return false;
		if (tokenIndex != token.tokenIndex)
			return false;
		return true;
	}
	
	
}
