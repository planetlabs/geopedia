package com.sinergise.geopedia.db.util;


public abstract class UpdateConditions
{
	UpdateConditions()
	{
		// only Update can instantiate
	}
	
	public abstract Update where(String field, int value);

	public abstract Update where(String field, String value);
}
