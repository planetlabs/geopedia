package com.sinergise.java.util.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <code>TimeStamper</code> ... DOCUMENT ME!
 * 
 * @author dvitas
 * @version $Id: TimeStamper.java 14311 2007-03-08 19:38:43Z dvitas $
 */
public class TimeStamper {
	private static Date             date          = new Date();
	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS ");
	
	public static String getTimeStamp() {
		date.setTime(System.currentTimeMillis());
		return timeFormatter.format(date);
	}
}
