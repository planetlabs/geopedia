package com.sinergise.geopedia.util;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.constants.Globals;

public class GeopediaServerUtility {
	public static boolean isRawHTML(String html) {
		if (StringUtil.isNullOrEmpty(html))
			return false;
		html = html.trim();
		if (html.startsWith(Globals.RAWHTMLHEADER) || html.startsWith("<p>"+Globals.RAWHTMLHEADER))
			return true;
		return false;
	}
	
	public static String removeRawHTMLHeader(String html) {
		html = html.trim();
		if (html.startsWith(Globals.RAWHTMLHEADER))
			return html.substring(Globals.RAWHTMLHEADER.length());
		else if (html.startsWith("<p>"+Globals.RAWHTMLHEADER+"</p>"))
			return html.substring(Globals.RAWHTMLHEADER.length()+7);
		else if (html.startsWith("<p>"+Globals.RAWHTMLHEADER))
			return "<p>"+html.substring(Globals.RAWHTMLHEADER.length()+3);
		return html;
	}
}
