package com.sinergise.geopedia.core.entities;

import java.io.Serializable;

public class WidgetInfo implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public int widgetId;
	public String uniqueKey;
	public int themeId;
	public int asUserId = Integer.MIN_VALUE;
	public User user;
}
