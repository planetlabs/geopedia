package com.sinergise.geopedia.core.exceptions;


public class QueryException extends EnumeratedException{
	private static final long serialVersionUID = -8851451698410171626L;
	public enum Type {
		SERVER_EXCEPTION,
		ILLEGAL_QUERY,
		DB_QUERY_FAILED
	}
	
	
	protected QueryException() {
		super(Type.SERVER_EXCEPTION);
	}
	private QueryException(Type type) {
		super(type);
	}

	public static final GeopediaException create(Type type) {
		return new GeopediaException(new QueryException(type));
	}
	@Override
	public Type getType() {
		return (Type) type;
	}
}