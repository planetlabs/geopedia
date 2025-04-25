/**
 * 
 */
package com.sinergise.java.util.sql;

/**
 * @author tcerovski
 */
public class ConsoleStatementLogger implements StatementLogger {
	
	@Override
	public void logError(final LoggableStatement stat, final String message) {
		System.err.println(message + ": " + stat);
	}
	
	@Override
	public void logExecutionError(final LoggableStatement stat) {
		logError(stat, "Error executing statement");
	}
	
	@Override
	public void logExecution(final LoggableStatement stat) {
		System.out.println(stat);
	}
	
}
