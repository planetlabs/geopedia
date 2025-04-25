package com.sinergise.geopedia.core.entities.properties;

import com.sinergise.common.util.property.ScalarPropertyImpl;

public class BinaryFileProperty extends ScalarPropertyImpl<Long>{
	private static final long serialVersionUID = -2750353346784473335L;
	
	private boolean isDeleted = false;
	private String fileToken = null;
	
	public BinaryFileProperty() {
	}
	
	public BinaryFileProperty(Long id) {
		super(id);
	}
	
	public void setFileToken(String fileToken) {
		this.fileToken=fileToken;
	}
	
	public String getFileToken() {
		return fileToken;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	
	public void delete() {
		if (fileToken!=null) {
			fileToken=null;
		}
		if (value != null) {
			isDeleted=true;
		}
	}

	public boolean hasValidFile() {
		if (value==null && fileToken==null)
			return false;
		if (value!=null && isDeleted() && fileToken==null)
			return false;
		return true;
	}

	public boolean hasFileToken() {
		return fileToken!=null;
	}
}
