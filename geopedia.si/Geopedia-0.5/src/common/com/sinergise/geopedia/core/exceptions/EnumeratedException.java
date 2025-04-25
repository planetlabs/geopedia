package com.sinergise.geopedia.core.exceptions;

public abstract class EnumeratedException extends Exception {
	private static final long serialVersionUID = -6099337919513932634L;

	Enum<?> type;
	
	protected EnumeratedException(Enum<?> type) {
		this(type, type.name());
	}
	
	protected EnumeratedException(Enum<?> type, String message) {
		super(message);
		this.type=type;
	}
	
	protected EnumeratedException(Enum<?> type, Throwable cause) {
		this(type, type.name(), cause);
	}

	protected EnumeratedException(Enum<?> type, String message, Throwable cause) {
		this(type, message);
		if (cause != null) {
			initCause(cause);
		}
	}
	
	public abstract Enum<?> getType();
}
