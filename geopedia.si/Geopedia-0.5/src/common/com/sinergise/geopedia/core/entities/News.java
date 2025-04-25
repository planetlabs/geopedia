package com.sinergise.geopedia.core.entities;

import java.io.Serializable;
import java.util.Date;

public class News implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public int id;
	public String title;
	public String data;
	public Date date;
	
}
