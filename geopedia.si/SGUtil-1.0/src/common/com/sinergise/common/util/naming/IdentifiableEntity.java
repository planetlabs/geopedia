package com.sinergise.common.util.naming;

import java.io.Serializable;

import com.sinergise.common.util.lang.Function;


public interface IdentifiableEntity extends Identifiable, Serializable {
	Function<IdentifiableEntity, EntityIdentifier> FUNC_ENTITY_ID_GETTER = new Function<IdentifiableEntity, EntityIdentifier>() {
		@Override
		public EntityIdentifier execute(IdentifiableEntity param) {
			return param.getQualifiedID();
		}
	};

	@Override
	public EntityIdentifier getQualifiedID();
	
	public boolean hasPermanentId();
	
	boolean equalsCheckResolved(Object obj);
}
