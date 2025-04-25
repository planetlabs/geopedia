package com.sinergise.java.cluster.swift.entities;

public class SwiftContainer {
	private long count;
	private long bytes;
	private String name;
	
	
	public SwiftContainer(String container, long objCount, long bytesUsed) {
		this.name=container;
		this.count = objCount;
		this.bytes = bytesUsed;
	}

	public String getName() {
		return name;
	}
	
	public long getContainerSize() {
		return bytes;
	}
	
	public long getObjectCount() {
		return count;
	}
	
	
	@Override
	public String toString() {
		return "Name: "+name+" [objectCount="+count+", size="+(bytes/1024/1024)+"MB]";
	}
}
