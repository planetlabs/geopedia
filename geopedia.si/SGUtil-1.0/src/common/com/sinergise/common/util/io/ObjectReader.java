package com.sinergise.common.util.io;

import java.io.Closeable;

public interface ObjectReader<T> extends Closeable {

	T readNext() throws ObjectReadException;
	
	boolean hasNext() throws ObjectReadException;
	
	public static class ObjectReadException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public ObjectReadException(String msg) {
			super(msg);
		}

		public ObjectReadException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
}
