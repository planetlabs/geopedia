package com.sinergise.geopedia.pro.client.ui.importexport;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.service.ExportService;
import com.sinergise.geopedia.core.service.params.ExportSettings;
import com.sinergise.geopedia.core.service.params.ExportStatus;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.i18n.ProMessages;
import com.sinergise.geopedia.pro.client.ui.AbstractSettingsStackPanel;
import com.sinergise.geopedia.pro.client.ui.SettingsStack;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.ProgressBar;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup.PositionType;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup.TooltipPosition;

public class ExportTablePanel extends FlowPanel {
	
	private static final int REFRESH_PERIOD=1*1000;
	private FlowPanel basePanel = null;
	private StatusPanel statusPanel;
	private SGPushButton btnExport;
	private SGPushButton  btnCancel;
	private SGPushButton btnDownload; 
	private Table tbl;
	private SettingsStack exportSettingsStack;
	private FlowPanel stackCont;
	private LoadingIndicator loading = new LoadingIndicator(true, true);
	
	private boolean isWorking = false;
	private boolean cancel = false;
	private class StatusPanel extends FlowPanel {
		ProgressBar pBar;
		Label errorMessage;
		Label processing;
		
		public StatusPanel () {
			errorMessage = new Label();
			errorMessage.setStyleName("error status clear");
			processing = new Label(ProConstants.INSTANCE.Process());
			pBar = new ProgressBar();
			add(processing);
			add(pBar);
			add(errorMessage);
			addStyleName("progressPanel clearfix");
			showWidget(pBar, false);
			showWidget(processing, false);// default is to hide the progress bar
			showWidget(errorMessage,false);
		}
		

		private void enableButton(SGPushButton anchor, boolean enable) {
			anchor.setEnabled(enable);
			anchor.setVisible(enable);
		}
		
		public void showWidget(Widget wg, boolean show) {
			wg.setVisible(show);
		}
		public void update(ExportStatus status) {
			if (status.errorMessage!=null)
				errorMessage.setText(status.errorMessage);
			else
				errorMessage.setText("");
			
			if (status.totalFeatures>0)
				pBar.setProgress((int)((double)status.exportedCnt/(double)status.totalFeatures*100.0));
			else
				pBar.setProgress(0);
			
			if (status.isExported()) {
				enableButton(btnDownload,true);
				enableButton(btnCancel,true);
				enableButton(btnExport,false);
				showWidget(processing, false);
				showWidget(pBar, false);
				showWidget(errorMessage,false);
				showWidget(loading, true);
			} else if (status.isWorking()) {
				enableButton(btnDownload,false);
				enableButton(btnCancel,true);
				enableButton(btnExport,false);
				showWidget(processing, true);
				showWidget(pBar, true);
				showWidget(errorMessage,false);
				showWidget(loading, true);
			} else if (status.isError()) {
				enableButton(btnDownload,false);
				enableButton(btnCancel,true);
				enableButton(btnExport,false);
				showWidget(processing, false);
				showWidget(pBar, false);
				showWidget(errorMessage,true);
				showWidget(loading, true);
			} else {
				enableButton(btnDownload,false);
				enableButton(btnCancel,false);
				enableButton(btnExport,true);
				showWidget(processing, false);
				showWidget(pBar, false);
				showWidget(errorMessage,false);
				showWidget(loading, false);
			}
		}
	}
	
	
	public static boolean hasPermissions(Table table) {
		return ClientSession.hasTablePermission(table, Permissions.TABLE_ADMIN);
	}
	
	public class ExportFieldsSP extends AbstractSettingsStackPanel {
		CheckBox exportCentroid = null;
		private class FieldCB {
			CheckBox cb;
			Field f;
			public FieldCB(Field f) {
				this.f=f;
				cb = new CheckBox(f.getName());
				cb.setValue(true);
			}			
		}
		
		ArrayList<FieldCB> fChckboxes = new ArrayList<FieldCB>();
		public ExportFieldsSP() {
			titleText = ProConstants.INSTANCE.Export_ExportFields();
			titleDetails=null;
			createUI();
		}
		
		private void createUI() {
			
			 Field[]  fields = tbl.getFields();
			 for (int i=0;i<fields.length;i++) {		
				 if (fields[i].isDeleted()) continue;
				 FieldCB fcb = new FieldCB(fields[i]);
				 fChckboxes.add(fcb);
				 add(fcb.cb);
			 }
			 
			 if (!tbl.getGeometryType().isPoint()) {
				 exportCentroid = new CheckBox("Centroid");
				 add(exportCentroid);
			 }
		}
		public ArrayList<Integer> getSelectedFields() {
			ArrayList<Integer> fieldIDs = new ArrayList<Integer>();
			for (FieldCB fcb:fChckboxes) {
				if (fcb.cb.getValue()==true) {
					fieldIDs.add(fcb.f.id);
				}
			}
			return fieldIDs;
		}
		
		public boolean exportCentroidAsFields() {
			if (exportCentroid==null) return false;
			return exportCentroid.getValue();
		}
	}
	
	public class ExtraSettings extends AbstractSettingsStackPanel {
		CheckBox cbResolveLookups;
		public ExtraSettings() {
			titleText = ProConstants.INSTANCE.Export_Extra();
			titleDetails = null;
			cbResolveLookups = new CheckBox(ProConstants.INSTANCE.Export_UseCodelists());
			cbResolveLookups.setValue(true);
			add(cbResolveLookups);
			Image codelistImg = new Image(Theme.getTheme().standardIcons().question());
			add(cbResolveLookups);
			add(codelistImg);
			codelistImg.setStyleName("fl-right");
			SGRichTooltipPopup.addTextTooltipPopup(codelistImg, ProMessages.INSTANCE.codelistHelp(), 
					new TooltipPosition(PositionType.RELATIVE_TO_WIDGET, 20),new TooltipPosition(PositionType.RELATIVE_TO_WIDGET, 0));
			updateTitle();
		}
		
		public void saveValues(ExportSettings settings) {
			settings.resolveLookups = cbResolveLookups.getValue();
		}
	}
	
	public class FormatSettings extends AbstractSettingsStackPanel {
		
		protected ListBox lbFormat;
		
		public FormatSettings(Table table) {
			titleText = ProConstants.INSTANCE.Export_ExportFormat();
			titleDetails=null;
			lbFormat = new ListBox();
			populateFormatBox(table, lbFormat);
			lbFormat.addChangeHandler(new ChangeHandler() {
				
				@Override
				public void onChange(ChangeEvent event) {
					updateDetails();
				}
			});
			
			add(lbFormat);
			updateDetails();
		}
		
		protected void populateFormatBox(Table table, ListBox lbFormat){
			if (table.hasGeometry()) {
				lbFormat.addItem("GPX",String.valueOf(ExportSettings.FMT_GPX));
				lbFormat.addItem("SHP",String.valueOf(ExportSettings.FMT_SHP));
			}
			lbFormat.addItem("CSV", String.valueOf(ExportSettings.FMT_CSV));
			lbFormat.addItem("XLSX", String.valueOf(ExportSettings.FMT_XLSX));
		}
		
		private void updateDetails() {
			int idx = lbFormat.getSelectedIndex();
			if (idx==-1) return;
			titleDetails=lbFormat.getItemText(idx);		
			updateTitle();
		}
		
		public int getValue() {
			int idx = lbFormat.getSelectedIndex();
			if (idx==-1) idx=0;
			return Integer.valueOf(lbFormat.getValue(idx));
		}
		
	}
	
	
	ExportFieldsSP exportFields;
	FormatSettings formatSettings;
	CRSChooserPanel crsChooserPanel;
	ExtraSettings extraSettings;
	private SGFlowPanel buttonsHolder;
	
	public ExportTablePanel(Table table) {
		this.tbl=table;
		addStyleName("exportTab");
		basePanel = new FlowPanel();
		
		
		stackCont = new FlowPanel();
		stackCont.setStyleName("StackCont");
		stackCont.add(new Heading.H2(ProConstants.INSTANCE.Export_ExportSettings()));
		
		exportSettingsStack = new SettingsStack();
		
		addExportFieldsSP(exportSettingsStack, table);
		
		addFormatSettings(exportSettingsStack, table);
		
		addCRSChooser(exportSettingsStack, table);
		
		addExtraSettings(exportSettingsStack, table);
		
		
		stackCont.add(exportSettingsStack);
		basePanel.add(stackCont);
		
		btnExport = new SGPushButton(GeopediaTerms.INSTANCE.exportIt(), Theme.getTheme().standardIcons().export(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				onExport();
			}

		});
		
		btnCancel = new SGPushButton(Buttons.INSTANCE.cancel(), Theme.getTheme().standardIcons().close(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onCancel();
			}

		});
		SimplePanel clear = new SimplePanel();
		clear.setStyleName("clear");
		
		btnDownload  = new SGPushButton(GeopediaTerms.INSTANCE.downloadIt(), GeopediaProStyle.INSTANCE.downGreen(), new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {
				Window.Location.replace(GWT.getHostPageBaseURL()+ExportService.SERVICE_URI+"?cmd=download&"
						+Globals.SESSION_PARAM_NAME+"="+ClientSession.getSessionValue());
				changeStatus(new ExportStatus());
			}
		});
		
		statusPanel = new StatusPanel();
		basePanel.add(statusPanel);
		buttonsHolder = new SGFlowPanel();
		buttonsHolder.setStyleName("buttonPanel centered");
		buttonsHolder.add(btnExport);
		buttonsHolder.add(btnDownload);
		buttonsHolder.add(btnCancel);
		btnDownload.setVisible(false);
		btnCancel.setVisible(false);
		loading.setVisible(false);
			
		basePanel.setStyleName("importBase clearfix");
		basePanel.add(loading);
		add(basePanel);
		add(buttonsHolder);
	}
	
	protected void addExportFieldsSP(SettingsStack exportSettingsStack, Table table){
		exportFields = new ExportFieldsSP();
		exportSettingsStack.add(exportFields);
	}
	
	protected void addCRSChooser(SettingsStack exportSettingsStack, Table table){
		CRSSettings crsSettings = ClientGlobals.getCRSSettings();
		crsChooserPanel = new CRSChooserPanel(crsSettings.getFromCRSTransformCapabilities(crsSettings.getMainCrsId()));
		exportSettingsStack.add(crsChooserPanel);
	}
	
	protected void addFormatSettings(SettingsStack exportSettingsStack, Table table){
		formatSettings = new FormatSettings(table);
		exportSettingsStack.add(formatSettings);
	}
	
	protected void addExtraSettings(SettingsStack exportSettingsStack, Table table){
		extraSettings = new ExtraSettings();
		exportSettingsStack.add(extraSettings);
	}


	protected void changeStatus(ExportStatus eStatus) {
		if (eStatus==null)
			return;
		statusPanel.update(eStatus);
	}
	
	private AsyncCallback<ExportStatus> exportCallback = new AsyncCallback<ExportStatus>() {

		@Override
		public void onFailure(Throwable caught) {
			processCallbackException(caught);
		}

		@Override
		public void onSuccess(ExportStatus result) {
			if (cancel)
				return;
			changeStatus(result);
			if (result.isWorking()) {
				isWorking=true;
				new Timer() {

					@Override
					public void run() {
						if (!cancel) {
							RemoteServices.getExportServiceInstance().doExport(ExportService.CMD_EXPORT_STATUS,
									null,exportCallback);
						}
					}
					
				}.schedule(REFRESH_PERIOD);
			} else {
				isWorking=false;
			}
			
		}
		
	};
	
	private void processCallbackException(Throwable caught) {
		
	}
	protected ExportSettings prepareExportSettings() {
		ExportSettings es = new ExportSettings();
		es.exportFormat = formatSettings.getValue();
		es.tableID = tbl.getId();
		es.fieldIDs = exportFields.getSelectedFields();
		es.exportCentroidAsField = exportFields.exportCentroidAsFields();
		es.crsTransformID = crsChooserPanel.getSelectedId();
		extraSettings.saveValues(es);
		return es;
	}
	
	private void onExport() {
		ExportSettings es = prepareExportSettings();
		cancel=false;
		RemoteServices.getExportServiceInstance().doExport(ExportService.CMD_EXPORT_START,es,exportCallback);
	}
	
	private void onCancel() {
		cancel=true;
		RemoteServices.getExportServiceInstance().doExport(ExportService.CMD_EXPORT_CANCEL,null, new AsyncCallback<ExportStatus>() {

			@Override
			public void onFailure(Throwable caught) {
				processCallbackException(caught);				
			}

			@Override
			public void onSuccess(ExportStatus result) {
				changeStatus(result);
			}
		});
	}

	public boolean canTerminate() {
		onCancel();
		return true;
	}
	
}
