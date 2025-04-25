package com.sinergise.java.util.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;


public class LoggerNameFilter extends AbstractMatcherFilter<ILoggingEvent> {

	String loggerName;
	
	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) {
			return FilterReply.NEUTRAL;
		}
		
		if (event.getLoggerName().startsWith(loggerName)) {
	      return onMatch;
	    } 
		return onMismatch;
	}
	
	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	
	public String getLoggerName() {
		return loggerName;
	}
	
	@Override
	public void start() {
		if (loggerName != null) {
			super.start();
		} else {
			addError("No loggerName set for filter " + this.getName());
		}
	}
}
