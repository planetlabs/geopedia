package com.sinergise.geopedia.core.exceptions;

final public class GeopediaException extends EnumeratedException {

	private static final long serialVersionUID = -469731755797354241L;

	public enum Type {
		UNKNOWN,
		/**
		 * Actual exception is wrapped by within this exception
		 */
		WRAPPED_EXCEPTION,	
		/** 
		 * Session is null, but is required! 
		 * */
		NO_SESSION,
		/** No permission! */
		PERMISSION_DENIED,
		/** database error */
		DATABASE_ERROR,
		/** User is not logged in but should be! */
		NOT_LOGGED_IN,
		/*** User is already logged in or is not logged in (logout)! */
		INVALID_USER_STATE,
		INVALID_USER,
		INVALID_WIDGET_ID,
		INVALID_INSTANCE
	}

	
	private EnumeratedException wrappedException = null;
	@Deprecated
	protected GeopediaException() {
		super(Type.UNKNOWN);
	}
	
	public GeopediaException(Type type) {
		super(type);
	}

	public GeopediaException(Type type, Throwable cause) {
		super(type, cause);
	}
	
	public GeopediaException(Type type, String message, Throwable cause) {
		super(type, message, cause);
	}
	
	public GeopediaException (EnumeratedException wrappedException) {
		this(Type.WRAPPED_EXCEPTION);
		this.wrappedException=wrappedException;
	}
	
	public EnumeratedException getWrappedException() {
		return wrappedException;
	}

	@Override
	public Type getType() {
		return (Type) type;
	}
}
