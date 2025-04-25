/**
 * 
 */
package com.sinergise.java.util.sql;

/**
 * @author tcerovski
 */
public interface StatementLogger {

	public void logExecution(LoggableStatement stat);

	public void logExecutionError(LoggableStatement stat);

	public void logError(LoggableStatement stat, String message);
}
