package com.sinergise.common.util.url;

public interface URLCoder {
	public String encodePart(String plainPart);
	
	public String decodePart(String encodedPart);
}
