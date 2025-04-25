package com.sinergise.geopedia.core.exceptions;

public class ImportExportException extends EnumeratedException{
	private static final long serialVersionUID = -1158385720931302658L;
	public enum Type {
		UNKNOWN,
		UNKNOWN_OR_CORRUPTED_FILE
	}
	
	
	protected ImportExportException() {
		super(Type.UNKNOWN);
	}
	private ImportExportException(Type type) {
		super(type);
	}

	public static final GeopediaException create(Type type) {
		return new GeopediaException(new ImportExportException(type));
	}
	@Override
	public Type getType() {
		return (Type) type;
	}
}
