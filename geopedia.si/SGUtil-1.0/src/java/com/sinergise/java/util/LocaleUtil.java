package com.sinergise.java.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class LocaleUtil {
	public static final Locale		SLO				= new Locale("sl", "SI");
	public static final DateFormat	SLO_DATE_MEDUIM	= DateFormat.getDateInstance(DateFormat.MEDIUM, SLO);

	public static final String sloCurrentDate() {
		return SLO_DATE_MEDUIM.format(new Date(System.currentTimeMillis()));
	}
}
