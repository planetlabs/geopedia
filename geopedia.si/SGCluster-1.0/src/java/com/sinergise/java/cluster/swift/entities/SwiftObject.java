package com.sinergise.java.cluster.swift.entities;

import com.sinergise.common.util.server.objectstorage.IObjectStorageObject;
import com.sinergise.common.util.web.MimeType;


public class SwiftObject implements IObjectStorageObject{
	private String name;
	private String hash;
	private long size;
	private MimeType mimeType;
	private String last_modified;//TODO: deserializer
	
	
	public String getObjectHash() {
		return hash;
	}
	
	public void setObjectHash(String hash) {
		this.hash = hash;
	}
	
	@Override
	public String getMimeType() {
		return mimeType.createContentTypeString();
	}

	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return (name+" (modified:"+last_modified+") "+mimeType);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name=name;
	}
}
