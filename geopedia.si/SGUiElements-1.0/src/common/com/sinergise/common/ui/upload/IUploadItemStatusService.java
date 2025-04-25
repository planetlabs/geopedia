package com.sinergise.common.ui.upload;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Called by widgets that upload files
 * 
 * @author bsernek
 */
public interface IUploadItemStatusService extends RemoteService {
	/**
	 * @param uploadToken or null to create a new one
	 * @return upload item null if a new upload item is to be created, UploadItem for an
	 * existing uploadToken instead
	 * @throws UploadException 
	 */
	UploadItem getUploadItem (String uploadToken) throws UploadException;
	
	public class Util {
		private static IUploadItemStatusServiceAsync INSTANCE = null;
		private static String url = null;
		public static IUploadItemStatusServiceAsync Instance() {
			if (INSTANCE == null) {
				INSTANCE = GWT.create(IUploadItemStatusService.class);
				if (url != null) {
					((ServiceDefTarget)INSTANCE).setServiceEntryPoint(url);
				}
			} 
			return INSTANCE;
		}
		public static void setUrl(String _url) {
			url = _url;
		}
	}
}
