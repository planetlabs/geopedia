/**
 * 
 */
package com.sinergise.java.util.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tcerovski
 */
public class SL4JStatementLogger implements StatementLogger {

	private static final Logger logger = LoggerFactory.getLogger("LoggableStatementLogger");
	
	@Override
	public void logExecutionError(LoggableStatement stat) {
		logError(stat,"Error executing statement");
	}
	
	@Override
	public void logError(LoggableStatement stat, String message) {
        logger.error(message+": "+stat);
	}

	@Override
	public void logExecution(LoggableStatement stat) {
		logger.debug(stat.getQueryString());
	}

}
