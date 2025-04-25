/**
 * 
 */
package com.sinergise.generics.gwt.widgets.upload;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sinergise.common.ui.upload.IUploadItemStatusService;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.generics.core.upload.entities.GenericDocument;
import com.sinergise.generics.core.upload.entities.GenericFile;
import com.sinergise.generics.core.upload.services.GenericsUploadService;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;
import com.sinergise.gwt.ui.maingui.SimpleLoadingPanel;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.table.FlexTableBuilder;
import com.sinergise.gwt.ui.upload.Uploader;
import com.sinergise.gwt.ui.upload.Uploader.Status;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusHandler;

/**
 * @author mperse
 */
public class GenericDocumentUploader extends AbstractDialogBox {
	public static final String UPLOAD_SERVLET_NAME = "documentUpload.upload";

	SimpleLoadingPanel loadingPanel = new SimpleLoadingPanel(this);

	protected DecoratedAnchor butSave;
	private String fileName;

	protected FileUpload uploadElement;
	protected FormPanel formPanel;
	
	private ErrorPopup errorPopup;

	protected boolean allowSaveWithoutFile = false;
	
	HandlerRegistration errCloseReg;
	FlexTable fileList = null;

	FlowPanel content;
	FlowPanel listHolder;

	GenericDocument document;
	GenericFile currentFile;
	
	WidgetLabels widgetLabels;

	private final GenericDocumentUploaderWidget parentPanel;
	
	public class ErrorPopup extends AbstractDialogBox {
		Label errorMsg = new Label();
		protected DecoratedAnchor btnOk;
		
		public ErrorPopup(WidgetLabels widgetLabels) {
			super(false, true, true);
			setText("Error");
			VerticalPanel vp = new VerticalPanel();
			vp.add(errorMsg);
			vp.add(btnOk = new DecoratedAnchor(widgetLabels.ok(), Theme.getTheme().standardIcons().ok(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ErrorPopup.this.hide();
				}
			}));
			add(vp);
			this.hide();
			this.center();
		}
		
		public void showError(String message){
			errorMsg.setText(message);
			this.show();
		}
	}
	
	public void setWidgetLabels(WidgetLabels widgetLabels) {
		this.widgetLabels = widgetLabels;
	}

	public GenericDocumentUploader(WidgetLabels widgetLabels, GenericDocumentUploaderWidget parentPanel) {
		super(false, true, true);
		this.parentPanel = parentPanel;
		setText("Document uploader");
		IUploadItemStatusService.Util.setUrl(GWT.getModuleBaseURL() + UPLOAD_SERVLET_NAME);
		this.widgetLabels = widgetLabels;
		errorPopup = new ErrorPopup(widgetLabels);
		document = new GenericDocument();
		init();
		updateUI();
		initUploadItem();
		errorPopup.hide();
	}

	@Override
	protected void onUnload() {
		reset();
		super.onUnload();
	}

	protected void save() {
		if ((document == null)) {
			return;
		}
		loadingPanel.showLoading();
		GenericsUploadService.Util.createInstance().saveGenericDocument(document, new AsyncCallback<GenericDocument>() {

			@Override
			public void onFailure(Throwable caught) {
				butSave.setEnabled(true);
				errorPopup.showError(caught.getMessage());
				caught.printStackTrace();
				loadingPanel.hideLoading();
			}

			@Override
			public void onSuccess(GenericDocument result) {
				butSave.setVisible(false);
				document = result;
				parentPanel.reloadList(result);
				loadingPanel.hideLoading();
				hide();
			}
		});
	}
	
	private String getFileFormat(String fileName) {
		MimeType mimeType = MimeType.getForFileExtension(fileName);
		return mimeType != null ? mimeType.toString() : "";
	}

	private void extractFileName() {
		// replace backslashes with slashes
		fileName = fileName.replaceAll("\\\\", "/");
		// get index of last slash
		int slashIndex = fileName.lastIndexOf('/');
		if (slashIndex > -1) {
			fileName = fileName.substring(slashIndex + 1);
		}
	}

	protected Label helpLabel;
	Uploader uploader;
	
	private void init() {
		content = new FlowPanel();
		listHolder = new FlowPanel();
		formPanel = new FormPanel();
		formPanel.setAction  (GWT.getModuleBaseURL());
		formPanel.setMethod(FormPanel.METHOD_POST);
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		
		uploadElement = new FileUpload();
		uploadElement.setName("UploadedFile");
		Hidden hidden = new Hidden();
		uploader = new Uploader(hidden, formPanel);
		uploader.setUseDefaultSessionToken(false);
		uploader.setPostProcessingRequired(true);
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(hidden);
		hp.add(uploadElement);
		
		uploadElement.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				uploadFile();
			}
		});

		formPanel.add(hp);
		hp.setVisible(true);
		
		butSave = new DecoratedAnchor(widgetLabels.save(), Theme.getTheme().standardIcons().save(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save();
			}
		});
		butSave.setVisible(false);
		uploader.addUploadCompleteHandler(new UploadCompleteHandler() {
			@Override
			public void onUploadComplete(UploadCompleteEvent event) {
				updateUI();
			}
		});
		
		uploader.addUploadStatusHandler(new UploadStatusHandler() {
			@Override
			public void onUploadStatusChange(UploadStatusEvent event) {
				updateUI();
			}
		});
		
		uploader.addUploadErrorHandler(new UploadErrorHandler() {
			
			@Override
			public void onUploadError(UploadErrorEvent event) {
				if (event.isFatalError()) {
					errorPopup.showError(event.getFatalError().getMessage());
				} else {
					updateUI();
				}
			}
		});
		
		updateFileList();
		content.add(createCloseButton());
		content.add(formPanel);
		content.add(listHolder);
		content.add(butSave);
		add(content);
	}
	
	public void uploadFile() {
		loadingPanel.showLoading();
		try {
			if ((uploadElement.getFilename() == null) || (uploadElement.getFilename().equals(""))) {
				return;
			}
			fileName = uploadElement.getFilename();
			extractFileName();
			currentFile = new GenericFile();
			currentFile.fileName = fileName;
			currentFile.mimeType = getFileFormat(fileName);
			uploader.beginUpload();
		} catch (Throwable t) {
			errorPopup.showError(t.getMessage());
		}
	}

	private void updateFileList() {
		FlexTableBuilder ftb = new FlexTableBuilder();
		ftb.addTitle("File name");
		ftb.addTitle("Edit");
		ftb.newRow();
		
		if (document.fileList.size() > 0) {
			for (int i = 0; i < document.fileList.size(); i++) {
				final int id = i;
				final GenericFile file = document.fileList.get(i);
				if (file.status != GenericFile.Status.DELETED) {
					ftb.addFieldLabelAndWidget(file.fileName, new DecoratedAnchor(widgetLabels.remove(), Theme.getTheme().standardIcons().delete(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							removeFile(id);
							updateFileList();
						}
					}));
					ftb.newRow();
				}
			}
			butSave.setVisible(true);
		} else {
			butSave.setVisible(false);
		}
		
		if(fileList != null) this.listHolder.remove(fileList);
		fileList = ftb.buildTable();
		fileList.setWidth("100%");
		this.listHolder.add(fileList);
	}

	public void removeFile(int i) {
		GenericFile file = document.fileList.get(i);
		System.out.println(file.status + " new status " + GenericFile.Status.NEW);
		if (file.status == GenericFile.Status.NEW) {
			document.fileList.remove(i);
		} else {
			file.status = GenericFile.Status.DELETED;
			butSave.setVisible(true);
		}
	}

	protected void reset() {
		if (errCloseReg != null) {
			errCloseReg.removeHandler();
			errCloseReg = null;
		}
		formPanel.reset();
		uploader.reset();
		updateUI();
		initUploadItem();
	}
	
	protected void updateUI() {
		Status st = uploader.getStatus();
		Throwable fatalError = uploader.getFatalError();
		
		switch (st) {
			case FETCHING_TOKEN:
			case SUBMITTING:
			case SUBMITTING_GOTINFO:
				uploader.updateInfoFromInput(uploadElement);
				break;
			case SUBMITTED_POSTPROC_CALLED:
				if (fatalError != null)
					errorPopup.showError(widgetLabels.docFetchingError() + fatalError.getMessage());
			case CLEAN:
			case BEFORE_SUBMIT:
				break;
			case FINISHED:
				currentFile.status = GenericFile.Status.NEW;
				currentFile.token = uploader.getCurrentToken();
				document.fileList.add(currentFile);
				reset();
				updateFileList();
				loadingPanel.hideLoading();
				break;
			default:
				break;
		}
	}

	protected void initUploadItem() {
		if (!uploader.isUseDefaultSessionToken()) uploader.fetchInitialItem();
	}

	public String getValue() {
		return (document != null && document.documentId != null) ? String.valueOf(document.documentId) : "-1";
	}

	public void setValue(Integer docId, final AsyncCallback<GenericDocument> callBack) {
		if (docId != null) {
			GenericsUploadService.Util.createInstance().loadGenericDocument(docId, false, new AsyncCallback<GenericDocument>() {

				@Override
				public void onFailure(Throwable caught) {
					errorPopup.showError(caught.getMessage());
					caught.printStackTrace();
				}

				@Override
				public void onSuccess(GenericDocument result) {
					document = result;
					updateFileList();
					callBack.onSuccess(result);
				}
			});
		}
	}

	public static boolean isIntegerValue(String value) {
		try {
			Integer val = Integer.valueOf(value);
			if (val == null || val.intValue() == -1) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
