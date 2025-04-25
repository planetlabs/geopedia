package com.sinergise.geopedia.core.exceptions;


public class ImportGPXException extends EnumeratedException {
	private static final long serialVersionUID = 2556134261674600635L;
	public enum Type {
		UNKNOWN,
		INVALID_GPX_FILE,
		ILLEGAL_GEOMETRY
	}
	
	
	protected ImportGPXException() {
		super(Type.UNKNOWN);
	}
	private ImportGPXException(Type type) {
		super(type);
	}

	public static final GeopediaException create(Type type) {
		return new GeopediaException(new ImportGPXException(type));
	}
	@Override
	public Type getType() {
		return (Type) type;
	}
}
