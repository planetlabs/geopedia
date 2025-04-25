package com.sinergise.gwt.ui.upload;

import java.util.HashSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.sinergise.common.ui.upload.IUploadItemStatusService;
import com.sinergise.common.ui.upload.UploadItem;
import com.sinergise.common.util.format.Format;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.upload.Uploader.Status;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusHandler;

public class SingleUploader extends FlowPanel {

	protected IUploaderController controller;
	protected NotificationPanel notifications = null;

	protected HTML fileInfoContent;
	protected FlowPanel resultPanel;

	protected HashSet<String> allowedExtensions;

	protected FileUpload uploadFile;
	protected Hidden hidden;
	protected FormPanel formPanel;
	protected Uploader uploader;
	protected FlowPanel uploaderHolder;
	private	  NotificationPanel loader;
	
	// TODO: language constants, builder design


	public SingleUploader(String uploadServletName, NotificationPanel notifications) {

		//this.controller = controler;
		this.notifications = notifications;
		
		allowedExtensions = new HashSet<String>();

		fileInfoContent = new HTML();
		hidden = new Hidden();
		loader = new NotificationPanel(); 
		loader.hide();

		initResultPanel();
		initUploadFile();
		initFormPanel();
		initUploader(uploadServletName);

		initUploaderHolder();

	}

	
	
	// TODO consider this, probably not needed
	protected void initResultPanel() {
		resultPanel = new FlowPanel();
		resultPanel.setVisible(false);
	}

	
	protected void initUploadFile() {
		uploadFile = new FileUpload();
		uploadFile.setName("UploadedFile");

		uploadFile.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				checkUploadFile();
			}
		});

	}

	protected void checkUploadFile() {
		if (isAllowedFile(uploadFile.getFilename())) {
			uploader.beginUpload();
		}
	}

	protected void initFormPanel() {
		formPanel = new FormPanel();
		formPanel.setAction(GWT.getModuleBaseURL());

		FlowPanel fp = new FlowPanel();
		fp.add(hidden);
		fp.add(uploadFile);

		formPanel.add(fp);
	}

	protected FlowPanel initUploaderHolder() {

		uploaderHolder = new FlowPanel();
		uploaderHolder.setStyleName("uploadFormHolder");

		uploaderHolder.add(fileInfoContent);
		uploaderHolder.add(loader);
		loader.addStyleName("statusLoader");

		uploaderHolder.insert(formPanel, 0);
		return uploaderHolder;
	}

	protected void initUploader(String uploadServletName) {
		IUploadItemStatusService.Util.setUrl(GWT.getModuleBaseURL() + uploadServletName);

		uploader = new Uploader(hidden, formPanel);
		uploader.setUseDefaultSessionToken(true);
		uploader.setPostProcessingRequired(true);

		uploader.addUploadCompleteHandler(new UploadCompleteHandler() {

			@Override
			public void onUploadComplete(UploadCompleteEvent event) {
				updateUploadUI();
			}

		});

		uploader.addUploadStatusHandler(new UploadStatusHandler() {

			@Override
			public void onUploadStatusChange(UploadStatusEvent event) {
				updateUploadUI();
			}

		});

		uploader.addUploadErrorHandler(new UploadErrorHandler() {
			@Override
			public void onUploadError(UploadErrorEvent event) {
				if (event.isFatalError()) {
					updateUploadUI();
				} else {
					updateUploadUI();
				}
			}
		});

	}

	public boolean isAllowedFile(String fileName) {

		if (fileName == null)
			return false;

		if (allowedExtensions.size() == 0)
			return true;

		if (fileName.lastIndexOf(".") <= 0)
			return false;

		String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (allowedExtensions.contains(ext)) {
			notifications.hide();
			return true;
		}

		// TODO: customize message
		String message = "Dovoljene so le naslednje vrste datotek: " + getAllowedExtensionsStr() + ".";
		notifications.showWarningMsg(message, false);

		return false;

	}

	
	
	
	public void updateUploadUI() {
		loader.showProgressMsg("", false);
		
		Status status = uploader.getStatus();
		Throwable fatalError = uploader.getFatalError();

		switch (status) {

			case CLEAN:
			case FETCHING_TOKEN:
				if (fatalError == null) {
					fileInfoContent.setText("Inicializiram");
				} else {
					fileInfoContent.setText("Napaka pri inicializaciji: " + fatalError.getMessage());
				}
				break;
				
			case BEFORE_SUBMIT:
				break;
				
			case SUBMITTING:				
			case SUBMITTING_GOTINFO:
				uploader.updateInfoFromInput(uploadFile);
				if (fatalError == null) {
					UploadItem metaInfo = uploader.getUploadingItem();
					String fName = metaInfo == null ? uploadFile.getFilename() : metaInfo.getFileName();
					String percStr = metaInfo == null ? "neznan" : Format.format(metaInfo.getPercentComplete(), 2) + "%";
					if (status == Status.SUBMITTING || metaInfo == null) {
						fileInfoContent.setText("Uploading " + fName + " ...");
					} else if (uploader.getInfoErrorCount() != 0) {
					fileInfoContent.setText(fName + ": Napredek pri uvozu ni poznan, prosimo počakajte (zadnji poznan je bil " + percStr + ")");
					} else {
						fileInfoContent.setText(fName + ": " + percStr);
					}
				} else {
					fileInfoContent.setText("Napaka pri uvozu: " + fatalError.getMessage());
				}
				break;
			case SUBMITTED_POSTPROC_CALLED:
				if (fatalError == null) {
					fileInfoContent.setText("Shranjujem datoteko");
				} else {
					fileInfoContent.setText("Napaka pri shranjevanju datoteke " + fatalError.getMessage());
				}
				break;
			case FINISHED:
				UploadItem uploadedFile = uploader.getUploadingItem();
				String fName = uploadedFile.getFileName();
				if (fName.isEmpty()) {
					fName = "Najprej uvozite datoteko";
				}
				fileInfoContent.setText(fName);
				loader.hide();
				break;
			default:
				break;

		}
		formPanel.setVisible(status.isBefore(Status.SUBMITTING));
		fileInfoContent.setVisible(status != Status.CLEAN && status != Status.BEFORE_SUBMIT);
		uploadFile.setVisible(status.isBefore(Status.SUBMITTING));
	}
	
	
	public void externalPostProcessDone(){
		Status status = uploader.getStatus();

		if (status != Status.SUBMITTED_POSTPROC_CALLED)
			throw new IllegalStateException("External process finished, out of sync!");
		
		
	}
	
	
	// -------------------		GETTER / SETTER		-------------------------------
	
	public String getAllowedExtensionsStr() {
		String allowed = "";
		// TODO: language constants
		if(allowedExtensions.size()==0) return "Dovoljeni vsi tipi datotek";
		for (String a : allowedExtensions) {
			allowed += ", " + a;
		}
		return allowed.substring(2);
	}


	public void addAllowedExtension(String extension) {
		allowedExtensions.add(extension);
	}

	public void removeAllowedExtension(String extension) {
		allowedExtensions.remove(extension);
	}

	public void resetAllowedExtension(String extension) {
		// empty allowedExtension stands for everything is allowed
		allowedExtensions.clear();
	}

	public NotificationPanel getNotifications() {
		return notifications;
	}

	public void setNotifications(NotificationPanel notifications) {
		this.notifications = notifications;
	}

	public FlowPanel getResultPanel() {
		return resultPanel;
	}

	public void setResultPanel(FlowPanel resultPanel) {
		this.resultPanel = resultPanel;
	}

	public FormPanel getFormPanel() {
		return formPanel;
	}

	public void setFormPanel(FormPanel formPanel) {
		this.formPanel = formPanel;
	}

	public FlowPanel getUploaderHolder() {
		return uploaderHolder;
	}

	public void setUploaderHolder(FlowPanel uploaderHolder) {
		this.uploaderHolder = uploaderHolder;
	}

	public IUploaderController getController() {
		return controller;
	}

	public void setControlLer(IUploaderController controler) {
		this.controller = controler;
	}

	public Uploader getUploader() {
		return uploader;
	}

	public void setUploader(Uploader uploader) {
		this.uploader = uploader;
	}

	public NotificationPanel getLoader() {
		return loader;
	}
	


}