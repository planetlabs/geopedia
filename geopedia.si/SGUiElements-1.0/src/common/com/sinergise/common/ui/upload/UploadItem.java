package com.sinergise.common.ui.upload;

import java.io.Serializable;
import java.util.Date;

/**
 * A single uploaded file item. Inherit this class to create additional fields. 
 * Respect GWT serialization 
 * 
 * @author bsernek
 */
public class UploadItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String SESSION_DEFAULT_UPLOAD_TOKEN = "com.sinergise.gwt.ui.upload.Uploader.SESSION_DEFAULT_UPLOAD_TOKEN"; 

	//TODO: Refactor upload mechanisms to either strictly support only one file per post, or extend the notifications and items for multiple files  
	
	String fileName = "";        	//< file name associated with
	String token = "";       		//< randomised upload ID
	long   fileSize;        		//< file size
	float  percentComplete = 0;		//< percentage of the item complete
	Date   generatedAt = new Date();//< when this UploadItem was generated
	String contentType;     		//< content type of the file
	boolean uploadComplete = false;	//< whether the upload has finished completely
	String uploadFailedMessage;
	
	public UploadItem() {
	}
	
	public UploadItem(UploadItem item) {
		this.token = item.getToken();
		this.fileName  = item.getFileName();
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String uploadToken) {
		this.token = uploadToken;
	}
	public float getPercentComplete() {
		return percentComplete;
	}
	public void setPercentComplete(float percentComplete) {
		this.percentComplete = percentComplete;
	}
	public Date getGeneratedAt() {
		return generatedAt;
	}
	public void setGeneratedAt(Date generatedAt) {
		this.generatedAt = generatedAt;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setUploadComplete(boolean uploadComplete) {
		this.uploadComplete = uploadComplete;
		if (uploadComplete) uploadFailedMessage = null;
	}
	
	public boolean isUploadComplete() {
		return uploadComplete;
	}

	public void setUploadFailedMessage(String message) {
		this.uploadFailedMessage = message;
	}
	
	public String getUploadFailedMessage() {
		return uploadFailedMessage;
	}
}