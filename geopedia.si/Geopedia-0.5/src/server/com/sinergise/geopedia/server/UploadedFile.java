package com.sinergise.geopedia.server;

import java.io.File;

public class UploadedFile
{
	public final String key;
	public final File tempFile;
	public final String contentType;
	public final String fileName;
	public final String filePath;
	
	public UploadedFile(String key, File tempFile, String contentType, String fileName, String filePath)
	{
		if ("image/x-png".equalsIgnoreCase(contentType))
			contentType = "image/png";
		// STUPID FU**ING IE
		
		this.key = key;
		this.tempFile = tempFile;
		this.contentType = contentType;
		this.fileName = fileName;
		this.filePath = filePath;
	}
}
