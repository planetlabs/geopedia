package com.sinergise.geopedia.style;

public class ParseStyleException extends Exception
{
	String[] errors;

	public ParseStyleException(String[] errors)
	{
		this.errors = errors;
	}

	public String[] getErrors()
	{
		return errors;
	}
}
