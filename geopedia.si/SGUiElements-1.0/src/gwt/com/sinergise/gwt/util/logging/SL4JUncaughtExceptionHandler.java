/**
 * 
 */
package com.sinergise.gwt.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

/**
 * @author tcerovski
 */
public class SL4JUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(SL4JUncaughtExceptionHandler.class);
	
	@Override
	public void onUncaughtException(Throwable e) {
		logger.error("Uncaught Exception: "+e.getMessage(), e);
	}

}
