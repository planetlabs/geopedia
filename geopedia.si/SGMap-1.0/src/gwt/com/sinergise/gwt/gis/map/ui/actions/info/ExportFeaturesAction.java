package com.sinergise.gwt.gis.map.ui.actions.info;

import static com.sinergise.common.util.collections.CollectionUtil.first;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.io.rpc.ExportFeaturesRequest;
import com.sinergise.common.gis.io.rpc.ExportFeaturesResponse;
import com.sinergise.common.gis.io.rpc.FeaturesIOService;
import com.sinergise.common.gis.io.rpc.FeaturesIOServiceAsync;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.map.ui.attributes.AbstractFeatureActionsProvider;
import com.sinergise.gwt.ui.ListBoxExt;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class ExportFeaturesAction extends Action {
	
	public static class ExportFeaturesActionProvider extends AbstractFeatureActionsProvider {
		@Override
		public List<ExportFeaturesAction> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
			if(!(CFeatureUtils.areAllCFeatures(fRep))) {
				return Collections.emptyList();
			}
			HasFeatures hasFeatures = (HasFeatures) fRep;
			boolean hasGeoms = hasFeatures.getFeatures().isEmpty() ? false : first(hasFeatures.getFeatures()).getDescriptor().hasGeometry();
			
			List<MimeType> mime = new ArrayList<MimeType>();
			if (hasGeoms) {
				mime.add(MimeType.MIME_ZIPPED_ESRI_SHP);
				mime.add(MimeType.MIME_GPX);
			}
			mime.add(MimeType.MIME_OPENDOCUMENT_SPREADSHEET);
			mime.add(MimeType.MIME_CSV);
			
			return Collections.singletonList(new ExportFeaturesAction(mime.toArray(new MimeType[mime.size()]), hasFeatures));
		}
		
	}

	private class ExportDialog extends AbstractDialogBox {
		private MimeType[] availableExportFormats;
		
		public ExportDialog(MimeType[] supportedExportFormats) {
			super(false, true);
			this.availableExportFormats = supportedExportFormats;
			setText(Labels.INSTANCE.exportFeatures_ExportDialogTitle());
			FlowPanel contentPanel = new FlowPanel();
			contentPanel.setStyleName("featuresExportDialog");
			
			contentPanel.add(new Label(Labels.INSTANCE.exportFeatures_Format()));
			final ListBoxExt lbFormat = new ListBoxExt();
			for (int i=0;i<availableExportFormats.length;i++) {
				String label = availableExportFormats[i].description+" ("+availableExportFormats[i].getDefaultFileExtension()+")";
				lbFormat.addItem(label,String.valueOf(i));
			}
			contentPanel.add(lbFormat);
			
			Breaker br = new Breaker();
			contentPanel.add(br);
			
			
			final FlowPanel pnlErrorHolder = new FlowPanel();
			pnlErrorHolder.setStyleName("errorHolder");
			contentPanel.add(pnlErrorHolder);
			
			FlowPanel btnHolder = new FlowPanel();
			btnHolder.add(new SGPushButton(Labels.INSTANCE.exportFeatures_Export(), Theme.getTheme().standardIcons().export(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					pnlErrorHolder.clear();
					int selectedMimeIdx = Integer.parseInt(lbFormat.getValue());
					final MimeType selectedMIME = availableExportFormats[selectedMimeIdx];
					featuresIOService.exportFeatures(
						new ExportFeaturesRequest(features.getFeatures(), selectedMIME), 
						new AsyncCallback<ExportFeaturesResponse>() {
							@Override
							public void onSuccess(ExportFeaturesResponse result) {
								hide();
								Window.Location.replace(GWT.getModuleBaseURL()+"featuresIOService?"+result.getQueryString());
							}
							
							@Override
							public void onFailure(Throwable caught) {
								FlowPanel content = new FlowPanel();
								content.add(new Label(Messages.INSTANCE.exportException()));
								pnlErrorHolder.add(content);
								caught.printStackTrace();
							}
					});

					
				}
			}));
			
			btnHolder.add(new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonClose(), Theme.getTheme().standardIcons().close(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			}));

			btnHolder.setStyleName("buttons");
			contentPanel.add(btnHolder);
			contentPanel.add(createCloseButton());
			setWidget(contentPanel);
		}
		
	}


	final FeaturesIOServiceAsync featuresIOService = GWT.create(FeaturesIOService.class);

	private HasFeatures features;
	private MimeType[] exportFormats;
	
	public ExportFeaturesAction(MimeType[] supportedExportFormats, HasFeatures features) {
		super(Tooltips.INSTANCE.featuresIO_export());		
		setDescription(getName());
		setIcon(Theme.getTheme().standardIcons().export());
		setStyle("exportFeatures");
		this.exportFormats = supportedExportFormats;
		this.features = features;
	}

	@Override
	protected void actionPerformed() {
		ExportDialog expDialog = new ExportDialog(exportFormats);
		expDialog.center();		
	}

	

}
