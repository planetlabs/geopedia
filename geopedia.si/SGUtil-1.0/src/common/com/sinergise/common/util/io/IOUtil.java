package com.sinergise.common.util.io;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtil {
	private static final Logger logger = LoggerFactory.getLogger(IOUtil.class); 

	protected IOUtil() {}
	
	public static final void close(Closeable... resources) throws IOException {
		Throwable first = null;
		for (Closeable r : resources) {
			try {
				close(r);
			} catch (Throwable t) {
				if (first == null) {
					first = t;
				}
			}
		}
		if (first != null) {
			throw new IOException(first);
		}
	}
	
	public static final void close(Closeable resource) throws IOException {
		if (resource == null) {
			return;
		}
		resource.close();
	}
	
	public static final void closeSilent(Closeable... resources) {
		for (Closeable r : resources) {
			try {
				close(r);
			} catch (Throwable t) {
				// be quiet
				logger.warn("Error while closing resource {}", r, t);
			}
		}
	}
}
