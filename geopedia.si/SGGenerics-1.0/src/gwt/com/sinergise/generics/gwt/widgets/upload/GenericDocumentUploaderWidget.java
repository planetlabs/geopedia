package com.sinergise.generics.gwt.widgets.upload;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sinergise.generics.core.upload.entities.GenericDocument;
import com.sinergise.generics.core.upload.entities.GenericFile;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;
import com.sinergise.gwt.ui.resources.Theme;

public class GenericDocumentUploaderWidget extends FlowPanel {

	DecoratedAnchor button;
	GenericDocumentUploader gdu;
	final VerticalPanel documentHolder;
	WidgetLabels widgetLabels;
	

	public GenericDocumentUploaderWidget(WidgetLabels widgetLabels) {
		documentHolder = new VerticalPanel();
		documentHolder.setStyleName("doc-holder");

		if (widgetLabels != null) {
			this.widgetLabels = widgetLabels;
		} else {
			initDefaultWidgetLabels();
		}

		gdu = new GenericDocumentUploader(widgetLabels, this);
		gdu.setVisible(false);
		
		button = new DecoratedAnchor(getButtonLabel(), Theme.getTheme().standardIcons().edit(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				gdu.center();
				gdu.setVisible(true);
				gdu.show();
			}
		});
		this.add(documentHolder);
		this.add(button);
	}

	public void initDefaultWidgetLabels() {
		this.widgetLabels = new WidgetLabels() {
			@Override
			public String ok() {
				return "OK";
			}

			@Override
			public String docFetchingError() {
				return "Error while fetching file info:";
			}

			@Override
			public String save() {
				return "Save";
			}

			@Override
			public String remove() {
				return "Remove";
			}

			@Override
			public String add() {
				return "Add";
			}

			@Override
			public String editDocuments() {
				return "Edit documents";
			}
		};
	}

	public String getButtonLabel() {
		return widgetLabels.editDocuments();
	}

	public static void buildFileList(final VerticalPanel documentHolder, GenericDocument doc) {
		documentHolder.clear();
		if (doc.fileList != null && doc.fileList.size() > 0) {
			for (int i = 0; i < doc.fileList.size(); i++) {
				final GenericFile file = doc.fileList.get(i);
				
				if(file.fileName == null) continue;
				
				final Anchor anch = new Anchor();
				anch.setText(file.fileName.trim());
				anch.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						String moduleBaseURL = GWT.getModuleBaseURL();
						if (!moduleBaseURL.endsWith("/")) {
							moduleBaseURL += "/";
						}
						Window.open(moduleBaseURL + "documentDownload?documentId=" + file.fileId, "_blank", null);
					}
				});
				documentHolder.add(anch);
			}
		}
	}

	public void reloadList(GenericDocument doc) {
		buildFileList(documentHolder, doc);
	}

	public String getValue() {
		return gdu.getValue();
	}

	public void setValue(Integer value) {
		if (value == null || value.intValue() == -1) {
			return;
		}

		gdu.setValue(value, new AsyncCallback<GenericDocument>() {

			@Override
			public void onSuccess(GenericDocument result) {
				GenericDocumentUploaderWidget.buildFileList(documentHolder, result);
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
}
