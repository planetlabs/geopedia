package com.sinergise.geopedia.core.exceptions;

import com.sinergise.geopedia.core.entities.Field;

public class FeatureDataException extends EnumeratedException{
	private static final long serialVersionUID = -1158385720931302658L;
	public enum Type {
		ILLEGAL_FIELD_VALUE,
		MISSING_MANDATORY_FIELD_VALUE,
		UNKNOWN_FIELD_TYPE,
		MISSING_GEOMETRY
	}
//	public static final int T_ILLEGAL_FIELD_VALUE = 1;
//	public static final int T_MISSING_MANDATORY_FIELD_VALUE = 2;
//	public static final int T_UNKNOWN_FIELD_TYPE = 3;
//	public static final int T_MISSING_GEOMETRY = 4;
	private Field field;
	
	protected FeatureDataException() {
		super(Type.ILLEGAL_FIELD_VALUE);
	}
	private FeatureDataException(Type type) {
		super(type);
	}

	public FeatureDataException(Type type, Field f) {
		super(type);
		this.field=f;
	}
	public static final GeopediaException create(Type type) {
		return new GeopediaException(new FeatureDataException(type));
	}
	
	public static final GeopediaException create(Type type, Field f) {
		return new GeopediaException(new FeatureDataException(type, f));
	}
	@Override
	public Type getType() {
		return (Type) type;
	}
}