package com.sinergise.gwt.ui.upload;

import com.sinergise.gwt.ui.upload.Uploader.UploadErrorEvent;

public interface IUploaderController {

	void beginUpload();
//	void updateUploadUI(Uploader uploader);
	void onUploadStatusChange();
	void onUploadComplete();

	void handleFatalError(UploadErrorEvent event);
	void addUploadErrorHandler();

}
