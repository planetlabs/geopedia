package com.sinergise.java.cluster.swift.entities;

import java.util.Date;
import java.util.HashMap;

import com.sinergise.common.util.server.objectstorage.IObjectStorageObject;

public class SwiftObjectMetadata implements IObjectStorageObject {

	private String name;
	private String mimeType;
	private Date lastModified;
	private String eTag;
	private long length;
	private HashMap<String,String> customMetadata;
	public SwiftObjectMetadata(String name, String mimeType, Date lastModified, String eTag, long length) {
		this.name=name;
		this.mimeType = mimeType;
		this.lastModified =  lastModified;
		this.eTag = eTag;
		this.length = length;
		
	}

	public void seCustomMetadata(HashMap<String, String> customMetadata) {
		this.customMetadata = customMetadata; 
		
	}
	
	public String getMD5Sum() {
		return eTag;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}
}
