/**
 * 
 */
package com.sinergise.java.util.sql;

import org.apache.log4j.Logger;


/**
 * @deprecated Use SL4JStatementLogger instead as all logging should be ported to SL4J. This will be deleted in the very near future!!
 * 
 * @author tcerovski
 */
@Deprecated
public class Log4JStatementLogger implements StatementLogger {

	private static final Logger logger = Logger.getLogger("LoggableStatementLogger");
	
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
		logger.debug(stat);
	}

}
