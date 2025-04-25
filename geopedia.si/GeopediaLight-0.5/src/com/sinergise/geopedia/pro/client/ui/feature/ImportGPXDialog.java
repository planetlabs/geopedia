package com.sinergise.geopedia.pro.client.ui.feature;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.upload.UploadItem;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.GeopediaLight;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.ProgressBar;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.upload.Uploader;
import com.sinergise.gwt.ui.upload.Uploader.Status;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusHandler;

public class ImportGPXDialog extends CenteredBox{
	
	private static final Logger logger = LoggerFactory.getLogger(ImportGPXPanel.class);
	private class ImportGPXPanel extends FlowPanel {
		
		private FormPanel uploadForm;
		private Hidden hidden;
		private FileUpload uploadElement;
		private Uploader uploader;
		private SGPushButton btnUpload;
		private Table table;
		private NotificationPanel notificationPanel;
		ProgressBar pBar;
		
		private LoadingIndicator loadingIndicator;
		
		private void showWidget(Widget wg, boolean show) {
			wg.setVisible(show);
		}
		
		public ImportGPXPanel(Table table) {
			this.table=table;
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
			
			FlowPanel uploadFormPanel = new FlowPanel();
			uploadFormPanel.add(new Heading.H2(ProConstants.INSTANCE.findGPX()));
			uploadFormPanel.add(uploadElement);
			uploadFormPanel.add(hidden);
			uploadForm.add(uploadFormPanel);

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
			
			notificationPanel = new NotificationPanel();
			notificationPanel.hide();
			add(notificationPanel);
			loadingIndicator = new LoadingIndicator(true,false);
			add(loadingIndicator);
			loadingIndicator.setVisible(false);
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
		
		
		private void handleThrowable(Throwable th) {
			notificationPanel.showErrorMsg(ExceptionI18N.getLocalizedMessage(th));
			notificationPanel.show();
			logger.error("ImportGPXError", th);
		}
		private void enableButton(SGPushButton anchor, boolean enable) {
			if (anchor.isEnabled() == enable)
				return;
			if (enable) {
				anchor.setEnabled(true);
			} else {
				anchor.setEnabled(false);
			}
		}
		
		public void onStatusChanged() {
			Status st = uploader.getStatus();
			if (st == Status.SUBMITTING) {
				showWidget(pBar, true);
				enableButton(btnUpload, false);
				showWidget(btnUpload, false);
				showWidget(uploadElement, false);
			} else {
				showWidget(pBar, false);
				enableButton(btnUpload, false);
				showWidget(uploadElement, false);
				showWidget(btnUpload, false);
				showWidget(uploadForm,false);
			}

		}

		private void uploadComplete(UploadCompleteEvent event) {
			
			loadingIndicator.setVisible(true);
			RemoteServices.getFeatureServiceInstance().createFeatureFromGPX(table.getId(), uploader.getCurrentToken(), 
					new AsyncCallback<ArrayList<Feature>>() {
				
				@Override
				public void onSuccess(ArrayList<Feature> results) {
					loadingIndicator.setVisible(false);
					hide();
					Feature feature = results.get(0);
					MapWidget mapWidget = ClientGlobals.mainMapWidget;
					mapWidget.getMapComponent().ensureVisible(feature.envelope, true, feature.getGeometryType().isPoint());
					FeatureEditor.getInstance(mapWidget, GeopediaLight.sideBar)
					.openFeatureEditor(feature, mapWidget);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					loadingIndicator.setVisible(false);
					handleThrowable(caught);
				}
			});
		}
	}
	
	
	FlowPanel contentPanel;

	public ImportGPXDialog(Table table) {
		addStyleName("importDialog");
		setHeaderTitle(ProConstants.INSTANCE.importGPX());
		
		contentPanel = new FlowPanel();
		setContent(contentPanel);
		contentPanel.add(new ImportGPXPanel(table));
	}


	
}
