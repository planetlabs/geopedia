/**
 * 
 */
package com.sinergise.java.util.sql;

/**
 * @author tcerovski
 */
public class StatementLoggerProvider {

	private static StatementLogger	logger	= new SL4JStatementLogger();

	public static StatementLogger getLogger() {
		return logger;
	}

	public static void setLogger(final StatementLogger logger) {
		if (logger != null) {
			StatementLoggerProvider.logger = logger;
		} else {
			StatementLoggerProvider.logger = new EmptyStatementLogger();
		}
	}

	public static class EmptyStatementLogger implements StatementLogger {
		@Override
		public void logExecutionError(final LoggableStatement stat) {
		// do nothing
		}

		@Override
		public void logError(final LoggableStatement stat, final String message) {
		// do nothing
		}

		@Override
		public void logExecution(final LoggableStatement stat) {
		// do nothing
		}
	}

}
