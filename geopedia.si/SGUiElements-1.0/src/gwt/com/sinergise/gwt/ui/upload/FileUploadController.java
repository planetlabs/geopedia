package com.sinergise.gwt.ui.upload;

import static com.sinergise.gwt.ui.i18n.UiMessages.UI_MESSAGES;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteHandler;

public class FileUploadController {
	
	public interface FileUploadControllerCallback {
		void onUploadStarted();
		void onUploadCompleted(String token);
		void onError(Throwable error);
	}
	
	private final FileUploadControllerCallback callback;
	private FormPanel uploaderForm;
	private FileUpload uploaderInput;
	private Uploader uploader;
	
	private Set<String> supportedFormats = null;
	
	public FileUploadController(final FileUploadControllerCallback callback) {
		this.callback = callback;
		
		uploaderForm = new FormPanel();
		uploaderForm.setAction(GWT.getModuleBaseURL());
		uploaderForm.setMethod(FormPanel.METHOD_POST);
		uploaderForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		createOrResetInputField();
		
		uploader = new Uploader(null, uploaderForm);
		uploader.setUseDefaultSessionToken(true);
		uploader.addUploadCompleteHandler(new UploadCompleteHandler() {
			@Override
			public void onUploadComplete(UploadCompleteEvent event) {
				if (event.getSource() == uploader) {
					createOrResetInputField();
					callback.onUploadCompleted(event.getUploader().getUploadingItem().getToken());
				}
			}
		});
	}
	
	public FormPanel getUploaderForm() {
		return uploaderForm;
	}
	
	public void setSupportedFormats(Collection<String> formats) {
		this.supportedFormats = new LinkedHashSet<String>(formats);
	}
	
	public boolean doUpload() {
		if (!uploaderInput.getFilename().isEmpty() && checkSupportedFiles()) {
			uploader.reset();
			callback.onUploadStarted();
			uploader.beginUpload();
			return true;
		}
		return false;
	}
	
	public void reset() {
		createOrResetInputField();
	}
	
	private boolean checkSupportedFiles() {
		if (supportedFormats == null) {
			return true;
		}
		
		String ext = FileUtil.getSuffixLowerCase(uploaderInput.getFilename());
		if (!supportedFormats.contains(ext)) {
			callback.onError(new Exception(UI_MESSAGES.FileUploadController_unsupportedFormat(StringUtil.join(", ", supportedFormats), ext)));
			return false;
		}
		return true;
	}
	
	private void createOrResetInputField() {
		//create new field as the element doesn't allow changing of value
		uploaderForm.setWidget(uploaderInput = new FileUpload());
		uploaderInput.setName("file");
	}

}
