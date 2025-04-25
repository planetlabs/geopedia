package com.sinergise.gwt.util.logging;

public class StackTraceGenerator {
	/**
	 * Generate an HTML stack trace from a throwable in a GWT client environment 
	 * @param th Throwable to analyze
	 * @return HTML formatted stack trace
	 */
	public static String generateStackTrace(Throwable th, boolean html) {
		StringBuilder msg = new StringBuilder();
		String fill = html? "\n&nbsp;&nbsp;" : "\n  "; 
		for (StackTraceElement ele : th.getStackTrace()) {
			msg.append(fill).append(ele.toString());
		}
		return msg.toString();
	}
}
