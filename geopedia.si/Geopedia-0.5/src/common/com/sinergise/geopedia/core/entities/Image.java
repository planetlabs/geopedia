package com.sinergise.geopedia.core.entities;

import java.io.Serializable;

public class Image implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int blob_id;
	public int user_id;
	public String mime;
	public int width;
	public int height;
	public long uploadedTime;
	
	public transient byte[] data;
	public transient String datamd5;
}
