package com.sinergise.geopedia.core.entities.walk;

//import java.util.HashMap;

import java.io.Serializable;

public abstract class FieldPath implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	public TablePath table;
	public String tableAlias;

	public abstract void toString(StringBuffer sb);	
	public abstract void toStringJS(StringBuffer sb);
	
	public final String toString()
	{
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}
	/*
	public abstract String getSelectExpression(HashMap tableAliasMap);

	public abstract String getAlias(HashMap tableAliasMap);
	*/
}
