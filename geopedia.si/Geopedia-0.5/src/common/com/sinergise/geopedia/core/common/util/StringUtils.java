package com.sinergise.geopedia.core.common.util;

import java.util.ArrayList;

public class StringUtils {
	public static final String padWith(String what, char padCh, int len, boolean before)
	{
		if (what.length() >= len)
			return what;
		String temp = what;
		while (temp.length() < len) {
			if (before)
				temp = padCh + temp;
			else
				temp = temp + padCh;
		}
		return temp;
	}

	public static final String toHTMLColor(int color)
	{
		return "#" + padWith(Integer.toHexString(color & 0x00FFFFFF).toUpperCase(), '0', 6, true);
	}

	
	 /**
	  * 
	  * Very simple nested string arrays [[][]] parser. Only parses the top most level
	  * @param str
	  * @return
	  */
	 public static String [] parseArray(String str) {
		 int start=0;
		 int counter = 0;
		 int totalCounter=0;
		 ArrayList<String> parsed = new ArrayList<String>();
		 for (int i=0;i<str.length();i++) {
			 char ch = str.charAt(i);
			 if (ch=='[') {
				 if (counter==0)
					 start=i;
				 counter++;	
				 totalCounter++;
			 } else if (ch==']') {
				 counter--;
				 if (counter==0) {
					 String part = str.substring(start+1, i);
					 parsed.add(part);
				 }
				 totalCounter--;
			 }
		 }
		 if (totalCounter!=0)
			 return null;
		 return (String[])parsed.toArray(new String[parsed.size()]);
	 }
	
}
