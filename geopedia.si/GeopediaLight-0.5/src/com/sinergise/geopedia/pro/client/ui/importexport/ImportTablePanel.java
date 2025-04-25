package com.sinergise.geopedia.pro.client.ui.importexport;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.ui.upload.UploadItem;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.service.ImportService;
import com.sinergise.geopedia.core.service.params.ImportSettings;
import com.sinergise.geopedia.core.service.params.ImportSettings.FieldSettings;
import com.sinergise.geopedia.core.service.params.ImportSettings.FileTypes;
import com.sinergise.geopedia.core.service.params.ImportStatus;
import com.sinergise.geopedia.core.service.params.TaskStatus;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractSettingsStackPanel;
import com.sinergise.geopedia.pro.client.ui.SettingsStack;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.ProgressBar;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup.PositionType;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup.TooltipPosition;
import com.sinergise.gwt.ui.upload.Uploader;
import com.sinergise.gwt.ui.upload.Uploader.Status;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusHandler;
import com.sinergise.gwt.util.html.CSS;

public class ImportTablePanel extends FlowPanel {

	private Uploader uploader;
	private ImportSettings iSettings = null;
	private UploadFilePanel uploadFilePanel;
	private SettingsStack importSettingsStack;
	private FlowPanel stackCont;
	private FileTypeCP cpFileType;
	private FieldChooserCP cpFieldChooser;
	private TableNameCP cpTableName;
	private CRSChooserPanel cpCRSChooser;
	private boolean cancel = false;
	private boolean isWorking = false;
	private static final int REFRESH_PERIOD = 1 * 1000;

	private SGPushButton btnImport;
	private SGPushButton btnCancel;
	private ProgressBar pbar;
	private FlowPanel msgPanel;
	
	//TODO: change error and ok messages with NotificationPanel
	private FlowPanel errorMsg;
	private FlowPanel okMsg;
	
	private SGPushButton linkImport;
	private FlowPanel importCtrlPanel;
	private boolean canTerminate = false;
	private Label processing;
	private MapLayers mapState;
	private ImportStatus status;
	
	private void enableButton(SGPushButton anchor, boolean enable) {
		if (anchor.isEnabled() == enable)
			return;
		if (enable) {
			anchor.setEnabled(true);
		} else {
			anchor.setEnabled(false);
		}
	}

	private void showWidget(Widget wg, boolean show) {
		if (show) {
			wg.removeStyleName("hide");
		} else {
			wg.addStyleName("hide");
		}
	}

	private class UploadFilePanel extends FlowPanel {
		Heading uploadTxt;
		FormPanel uploadForm;
		Hidden hidden;
		FileUpload uploadElement;

		SGPushButton btnUpload;
		ProgressBar pBar;

		public void onStatusChanged() {
			Status st = uploader.getStatus();
			if (st == Status.SUBMITTING) {
				showWidget(pBar, true);
				enableButton(btnUpload, false);
				showWidget(btnUpload, false);
				showWidget(uploadElement, false);

			} else {
				showWidget(pBar, false);
				enableButton(btnUpload, true);
				showWidget(uploadElement, true);
				showWidget(btnUpload, true);
			}

		}

		public UploadFilePanel() {
			uploadTxt = new Heading.H2(ProConstants.INSTANCE.Import_ChooseZipFile());
			uploadForm = new FormPanel();
			uploadForm.setStyleName("uploadFile");
			uploadForm.setMethod(FormPanel.METHOD_POST);
			uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
			uploadForm.setAction(GWT.getModuleBaseURL());

			hidden = new Hidden();
			uploadElement = new FileUpload();
			uploadElement.setName("UploadedFile");
			uploader = new Uploader(hidden, uploadForm);
			uploader.setUseDefaultSessionToken(true);
			FlowPanel pnl = new FlowPanel();
			pnl.add(uploadTxt);
			pnl.add(uploadElement);
			pnl.add(hidden);
			uploadForm.add(pnl);

			btnUpload = new SGPushButton(ProConstants.INSTANCE.Import_Upload());
			btnUpload.setImage(GeopediaProStyle.INSTANCE.upBlue());
			btnUpload.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					if (uploadElement.getFilename()==null || uploadElement.getFilename().length()==0)
						return;
					try {
						uploader.beginUpload();
					} catch (Throwable t) {
						handleThrowable(t);
					}

				}
			});

			add(uploadForm);
			add(btnUpload);

			pBar = new ProgressBar();
			add(pBar);
			showWidget(pBar, false);
			setStyleName("uploadForm clearfix");

			uploader.addUploadCompleteHandler(new UploadCompleteHandler() {

				@Override
				public void onUploadComplete(UploadCompleteEvent event) {
					onStatusChanged();
					uploadComplete(event);
				}
			});
			uploader.addUploadStatusHandler(new UploadStatusHandler() {

				@Override
				public void onUploadStatusChange(UploadStatusEvent event) {
					onStatusChanged();
					UploadItem item = event.getItem();
					if (item != null) {
						pBar.setProgress(item.getPercentComplete());
					}
				}
			});
			uploader.addUploadErrorHandler(new UploadErrorHandler() {

				@Override
				public void onUploadError(UploadErrorEvent event) {
					onStatusChanged();
				}
			});

		}
	}

	private void uploadComplete(UploadCompleteEvent event) {
		showWidget(uploadFilePanel, false);
		RemoteServices.getImportServiceInstance()
				.analyzeUploadedFile(event.getUploader().getCurrentToken(),
						new AsyncCallback<ImportSettings>() {

							@Override
							public void onSuccess(ImportSettings result) {
								onFileAnalysisComplete(result);
							}

							@Override
							public void onFailure(Throwable caught) {
								handleThrowable(caught);
							}
						});
	}

	private void onFileAnalysisComplete(ImportSettings iSettings) {
		this.iSettings = iSettings;
		showWidget(importCtrlPanel, true);
		showWidget(stackCont, true);
		cpFileType.setFileType(iSettings.fileType);
		cpFieldChooser.setImportSettings(iSettings);
		cpTableName.setTableName(iSettings.tableName);
		cpCRSChooser.setValue(iSettings.crsId);
	}

	private class FileTypeCP extends AbstractSettingsStackPanel {
		Label fmtLabel;

		public FileTypeCP() {
			titleText = ProConstants.INSTANCE.Import_ImportFormat();
			Label lbl = new Label(ProConstants.INSTANCE.Import_ImportFormat());
			lbl.setStyleName("label");
			fmtLabel = new Label("");
			fmtLabel.setStyleName("value");
			setStyleName("formatGroup");
			add(lbl);
			add(fmtLabel);

		}

		public void setFileType(FileTypes iType) {
			switch (iType) {

			case SHP:
				fmtLabel.setText("ESRI Shapefile");
				titleDetails = "ESRI Shapefile";
				updateTitle();
				break;
			default:
			case UNKNOWN:
				fmtLabel.setText(GeopediaTerms.INSTANCE.unknown());
				titleDetails = "unknown";
				updateTitle();
				break;
			}
		}
	}

	private class TableNameCP extends AbstractSettingsStackPanel {
		SGTextBox tableNameTB;

		public TableNameCP() {
			titleText = ProConstants.INSTANCE.Import_LayerName();;
			tableNameTB = new SGTextBox();
			tableNameTB.setStyleName("layerName");
			add(tableNameTB);
			tableNameTB.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					nameChanged();
				}
			});
		}

		private void nameChanged() {
			titleDetails = tableNameTB.getText();
			updateTitle();
		}

		public void setTableName(String name) {
			tableNameTB.setText(name);
			nameChanged();
		}

		public String getTableName() {
			return tableNameTB.getText();
		}
	}

	private class FieldChooserCP extends AbstractSettingsStackPanel {
		FlowPanel fieldsPanel;
		CheckBox[] cbFieldEnabled;
		SGTextBox[] fieldNames;
		CheckBox[] isCodelist;

		CheckBox cbImportGeometry;
		public FieldChooserCP() {
			titleText = ProConstants.INSTANCE.Import_ImportFields();
			fieldsPanel = new FlowPanel();
			add(fieldsPanel);
		}
		
		private void updateFieldsLabel() {
			if (Boolean.TRUE.equals(cbImportGeometry.getValue())) {
				titleDetails = GeopediaTerms.INSTANCE.geometry();
			} else {
				titleDetails = GeopediaTerms.INSTANCE.codelist();
			}
			updateTitle();
		}

		public void setImportSettings(ImportSettings iSettings) {
			Field[] fieldss = iSettings.fields;
			fieldsPanel.clear();
			int nFields = fieldss.length;
			cbFieldEnabled = new CheckBox[nFields];
			isCodelist = new CheckBox[nFields];
			fieldNames = new SGTextBox[nFields];
			
			if (iSettings.cFeatureDesc.hasGeometry()) {
				cbImportGeometry = new CheckBox(ProConstants.INSTANCE.Import_ImportGeometryData());
				cbImportGeometry.addStyleName("geometry");
				cbImportGeometry.setValue(true);
				cbImportGeometry.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						updateFieldsLabel();
					}
				});
				FlowPanel groupCode = new FlowPanel();
				groupCode.setStyleName("groupCode");
				groupCode.add(cbImportGeometry);
				Image helpLink = new Image(Theme.getTheme().standardIcons().help());
				SGRichTooltipPopup.addWidgetTooltipPopup(helpLink, new Label(ProConstants.INSTANCE.Import_GeometryDataHelp()), 
						new TooltipPosition(PositionType.RELATIVE_TO_WIDGET, -8),new TooltipPosition(PositionType.RELATIVE_TO_WIDGET, 27),400);
				groupCode.add(helpLink);
				fieldsPanel.add(groupCode);
				updateFieldsLabel();
			}
			
			for (int i = 0; i < nFields; i++) {
				FlowPanel groupCode = new FlowPanel();
				groupCode.setStyleName("groupCode");
				buildFieldPanel(groupCode, fieldss[i],i);
				fieldsPanel.add(groupCode);
			}
			
			
		}
		
		private void buildFieldPanel (FlowPanel groupCode, Field fld, int i) {
			final CheckBox cbEnabled  = new CheckBox();
			cbFieldEnabled[i] = cbEnabled;
			cbFieldEnabled[i].setValue(true);
			cbFieldEnabled[i].addStyleName("include");

			final SGTextBox tbName = new SGTextBox();
			fieldNames[i] = tbName;
			fieldNames[i].setText(fld.getName());
			fieldNames[i].setMaxLength(100);//TODO: read from some settings or constant
			fieldNames[i].addStyleName("name");
			
			final CheckBox cbIsCodelist =  new CheckBox(ProConstants.INSTANCE.Import_Codelist());
			isCodelist[i] = cbIsCodelist;
			isCodelist[i].setValue(false);
			isCodelist[i].addStyleName("codelist");
			
			cbEnabled.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					if (cbEnabled.getValue()) {
						tbName.setEnabled(true);
						cbIsCodelist.setEnabled(true);
					} else {
						tbName.setEnabled(false);
						cbIsCodelist.setEnabled(false);
					}
				}
			});
			groupCode.add(cbFieldEnabled[i]);
			groupCode.add(fieldNames[i]);
			groupCode.add(isCodelist[i]);
		}

		public void updateSettings(ImportSettings iSettings) {
			iSettings.enabledFields  = new ArrayList<FieldSettings>();
			if (cbFieldEnabled == null) {	
				return;
			}
			for (int i = 0; i < cbFieldEnabled.length; i++) {
				if (cbFieldEnabled[i].getValue()) {
					iSettings.fields[i].setName(fieldNames[i].getText());
					FieldSettings fs = new FieldSettings(i);
					fs.isCodelist = isCodelist[i].getValue();
					iSettings.enabledFields.add(fs);
				}
			}
			if (cbImportGeometry!=null && cbImportGeometry.getValue()) {
				FieldSettings fsGeom = new FieldSettings(iSettings.cFeatureDesc.getGeomIndex());
				iSettings.enabledFields.add(fsGeom);
			}
			
			return;
		}

	}

	private AsyncCallback<ImportStatus> importCallback = new AsyncCallback<ImportStatus>() {

		@Override
		public void onFailure(Throwable caught) {
			handleThrowable(caught);
		}

		@Override
		public void onSuccess(ImportStatus status) {
			if (cancel)
				return;
			updateStatus(status);
			if (status.getStatus().equals(TaskStatus.Status.WORKING)) {
				isWorking = true;
				new Timer() {

					@Override
					public void run() {
						if (!cancel) {
							RemoteServices.getImportServiceInstance(
									).doImport(
									ImportService.CMD_IMPORT_STATUS, null,
									importCallback);
						}
					}

				}.schedule(REFRESH_PERIOD);
			} else {
				isWorking = false;
			}

		}

	};

	public boolean canTerminate() {
		if (canTerminate) {
			if (! (status.getStatus().equals(TaskStatus.Status.FINAL) || status.getStatus().equals(TaskStatus.Status.NOP)) ) {
				onCancel();
			}
			return true;
		}
		return false;
	}

	public void updateStatus(ImportStatus status) {
		this.status=status;
		if (status.getStatus().equals(TaskStatus.Status.WORKING)) {
			showWidget(pbar, true);
			showWidget(processing, true);
			pbar.setProgress(status.getProgress());
			canTerminate = false;
		} else {
			showWidget(pbar, false);
			showWidget(processing, false);
			canTerminate = true;

		}

		if (status.getStatus().isBefore(TaskStatus.Status.DONE)) {
			enableButton(btnCancel, true);
			showWidget(btnCancel, true);
		} else {
			enableButton(btnCancel, false);
			showWidget(btnCancel, false);
		}

		if (status.getStatus().equals(TaskStatus.Status.NOP)) {
			showWidget(btnImport, true);
			enableButton(btnImport, true);
		} else {
			showWidget(btnImport, false);
			enableButton(btnImport, false);
		}
		if (status.getStatus().equals(TaskStatus.Status.FINAL)) {
			okMsg.add(new InlineHTML(ProConstants.INSTANCE.Import_ImportSuccessfull()));
			showWidget(stackCont, false);
			showWidget(msgPanel, true);
			showWidget(okMsg, true);
			showWidget(linkImport, true);
			onImportFinished(status);
		}
		if (status.getStatus().equals(TaskStatus.Status.ERROR)) {
			showError(status.getError());
		}
	}

	private void onImportFinished(ImportStatus status) {
		//linkImport.setHref("http://v1.geopedia.si/?params=L"+status.tableId);
		mapState.addTableToVirtualTheme(status.tableId, ProConstants.INSTANCE.Import_LayersGroup());
	}
	private void onCancel() {
		cancel = true;
		RemoteServices.getImportServiceInstance().doImport(
				ImportService.CMD_IMPORT_CANCEL, getImportSettings(),
				new AsyncCallback<ImportStatus>() {

					@Override
					public void onFailure(Throwable caught) {
						handleThrowable(caught);
					}

					@Override
					public void onSuccess(ImportStatus result) {
						updateStatus(result);
						initialState();
					}
				});
	}

	private void onImport() {
		showWidget(stackCont, true);
		showWidget(uploadFilePanel, false);
		RemoteServices.getImportServiceInstance().doImport(
				ImportService.CMD_IMPORT_START, getImportSettings(),
				importCallback);

	}

	private void showError(int errorNumber) {
		switch (errorNumber) {
		default:
			showError("Import error");
			break;
		}
	}

	private void showError(String error) {
		errorMsg.add(new InlineHTML(error));
		showWidget(importCtrlPanel, false);
		showWidget(stackCont, false);
		showWidget(uploadFilePanel, false);
		showWidget(linkImport, false);
		showWidget(msgPanel, true);
		showWidget(errorMsg, true);
	}

	private void handleThrowable(Throwable th) {
		showError(ExceptionI18N.getLocalizedMessage(th));
	}

	public ImportSettings getImportSettings() {
		cpFieldChooser.updateSettings(iSettings);
		iSettings.tableName = cpTableName.getTableName();
		iSettings.crsId = cpCRSChooser.getSelectedId();
		return iSettings;
	}

	public ImportTablePanel(MapLayers mapState) {
		this(null, mapState);
	}

	public ImportTablePanel(Table tbl,MapLayers mapState) {
		this.mapState=mapState;
		addStyleName("importTab");
		// upload panel
		uploadFilePanel = new UploadFilePanel();
		FlowPanel basePanel = new FlowPanel();
		basePanel.add(uploadFilePanel);
		// import settings
		stackCont = new FlowPanel();
		importSettingsStack = new SettingsStack();
		// add stack panels
		stackCont.setStyleName("StackCont");

		cpFileType = new FileTypeCP();
		cpFieldChooser = new FieldChooserCP();
		cpTableName = new TableNameCP();
		CRSSettings crsSettings = ClientGlobals.getCRSSettings();
		cpCRSChooser = new CRSChooserPanel(crsSettings.getToCRSTransformCapabilities(crsSettings.getMainCrsId()));
		importSettingsStack.add(cpTableName);
		importSettingsStack.add(cpFileType);
		importSettingsStack.add(cpFieldChooser);
		importSettingsStack.add(cpCRSChooser);
		stackCont.add(new Heading.H2(ProConstants.INSTANCE.Import_ImportSettings()));
		stackCont.add(importSettingsStack);
		basePanel.add(stackCont);
		basePanel.setStyleName("importBase clearfix");

		// import control (buttons, progress bar)
		importCtrlPanel = new FlowPanel();
		btnImport = new SGPushButton(GeopediaTerms.INSTANCE.Import(), GeopediaProStyle.INSTANCE.upGreen());
		btnCancel = new SGPushButton(Buttons.INSTANCE.cancel(), GeopediaProStyle.INSTANCE.crossRed());
		btnCancel.addStyleName("fl-right");
		pbar = new ProgressBar();
		processing = new Label(ProConstants.INSTANCE.Process());
		importCtrlPanel.add(btnImport);
		importCtrlPanel.add(btnCancel);
		importCtrlPanel.add(processing);
		importCtrlPanel.add(pbar);
		importCtrlPanel.setStyleName("progressPanel");

		btnImport.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				onImport();
			}
		});

		btnCancel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				onCancel();
			}

		});

		basePanel.add(importCtrlPanel);
		add(basePanel);
		updateStatus(new ImportStatus());

		// error and message panels
		msgPanel = new FlowPanel();
		msgPanel.setStyleName("msgPanel");
		
		errorMsg = new FlowPanel();
		errorMsg.add(new Image(Theme.getTheme().standardIcons().errorBig()));
		okMsg = new FlowPanel();
		okMsg.add(new Image(Theme.getTheme().standardIcons().okBig()));
		linkImport = new SGPushButton(ProConstants.INSTANCE.Import_EditContentLinkText(),GeopediaProStyle.INSTANCE.editBlue());
		linkImport.addStyleName("goto");
		CSS.marginTop(linkImport, 30);
		msgPanel.add(errorMsg);
		msgPanel.add(okMsg);
		msgPanel.add(new Breaker());
//		msgPanel.add(linkImport);
//		basePanel.add(errorMsg);
		basePanel.add(msgPanel);
		initialState();
	}

	private void initialState() {
		showWidget(errorMsg, false);
		showWidget(importCtrlPanel, false);
		showWidget(stackCont, false);
		showWidget(okMsg, false);
		showWidget(msgPanel, false);
		showWidget(linkImport, false);
		showWidget(uploadFilePanel, true);
		uploader.reset();

	}

}
