package com.sinergise.geopedia.style.processor;

import com.sinergise.geopedia.core.style.model.StyleSpecPart;

public class Match
{
	static boolean matchNameLen(String name, String reqName, int n, StyleSpecPart[] params)
	{
		return name.equals(reqName) && params.length == n;
	}
	
	static boolean is(StyleSpecPart param, int type)
	{
		int t = param.getType();
		return t == type || t == StyleSpecPart.T_NULL;
	}
	
	public static boolean is(String name, StyleSpecPart[] params, String reqName, int type1)
	{
		return matchNameLen(name, reqName, 1, params) &&
	       is(params[0], type1);
	}

	public static boolean is(String name, StyleSpecPart[] params, String reqName, int type1, int type2, int type3)
	{
		return matchNameLen(name, reqName, 3, params) &&
	       is(params[0], type1) &&
	       is(params[1], type2) &&
	       is(params[2], type3);
	}

	public static boolean is(String name, StyleSpecPart[] params, String reqName, int type1, int type2, int type3, int type4)
	{
		return matchNameLen(name, reqName, 4, params) &&
	       is(params[0], type1) &&
	       is(params[1], type2) &&
	       is(params[2], type3) &&
	       is(params[3], type4);
	}
	
	public static boolean is(String name, StyleSpecPart[] params, String reqName, int type1, int type2, int type3, int type4, int type5)
	{
		return matchNameLen(name, reqName, 5, params) &&
	       is(params[0], type1) &&
	       is(params[1], type2) &&
	       is(params[2], type3) &&
	       is(params[3], type4) &&
	       is(params[4], type5);
	}
}
