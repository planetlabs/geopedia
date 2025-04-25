package com.sinergise.common.util.server.objectstorage;

public class ObjectStorageException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ObjectStorageException(String message, Throwable cause) {
		super(message,cause);
	}
	public ObjectStorageException(String message) {
		super(message);
	}
}
