package com.sinergise.generics.core.upload.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sinergise.generics.core.upload.entities.GenericDocument;

@RemoteServiceRelativePath("genericsUpload")
public interface GenericsUploadService extends RemoteService {

	public static class Util {
		public static synchronized GenericsUploadServiceAsync createInstance() {
			if (GWT.isClient()) {
				GenericsUploadServiceAsync ret = (GenericsUploadServiceAsync) GWT.create(GenericsUploadService.class);
				((ServiceDefTarget) ret).setServiceEntryPoint(GWT.getModuleBaseURL() + "genericsUpload");
				return ret;
			}
			return null;
		}
	}

	GenericDocument saveGenericDocument(GenericDocument doc) throws Exception;
	GenericDocument loadGenericDocument(Integer docId, boolean loadFileData) throws Exception;

}
