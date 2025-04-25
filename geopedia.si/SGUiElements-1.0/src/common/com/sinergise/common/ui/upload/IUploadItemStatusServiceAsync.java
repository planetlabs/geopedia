package com.sinergise.common.ui.upload;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Called by widgets that upload files
 * 
 * @author bsernek
 */
public interface IUploadItemStatusServiceAsync {
	/**
	 * Poll the status of an UploadItem 
	 * 
	 * @param uploadToken null if a new upload item is to be created, uploadToken of an
	 * existing UploadItem instead
	 */
	void getUploadItem (String uploadToken, AsyncCallback<UploadItem> callback);
}
