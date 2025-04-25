package com.sinergise.common.util.io;

import java.io.Closeable;

public interface ObjectWriter <T> extends Closeable {

	void append(T o) throws ObjectWriteException;
	
	public static class ObjectWriteException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public ObjectWriteException(String msg) {
			super(msg);
		}

		public ObjectWriteException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
}
