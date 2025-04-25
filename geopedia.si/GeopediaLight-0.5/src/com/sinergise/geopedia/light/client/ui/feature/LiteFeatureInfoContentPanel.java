package com.sinergise.geopedia.light.client.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.sinergise.geopedia.client.components.heightprofile.HeightProfileDialog;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.search.CoordSearcher;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.feature.DivFeatureInfoContentPanel;
import com.sinergise.geopedia.client.ui.feature.FeatureInfoContentPanel;
import com.sinergise.geopedia.client.ui.panels.results.FeatureInfoWidget;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.ui.ButtonFactory;
import com.sinergise.geopedia.pro.client.ui.importexport.ExportFeatureDialog;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

public class LiteFeatureInfoContentPanel extends DivFeatureInfoContentPanel {
	
	private PushButton btnEditFeature;
	public LiteFeatureInfoContentPanel(FeatureInfoWidget featureInfoWidget) {
		super(featureInfoWidget);
	}



	public static class Creator implements PanelCreator {
		@Override
		public FeatureInfoContentPanel createPanel(FeatureInfoWidget featureInfoWidget) {
			return new LiteFeatureInfoContentPanel(featureInfoWidget);
		}
	}


	
	@Override
	protected void placeFields(final Feature feature,  final Table featureTable) {
		
		if (btnEditFeature!=null)
			btnEditFeature.removeFromParent();
		btnEditFeature = ButtonFactory.FeatureButtons.createEditFeatureButton(featureTable, feature);
		if (btnEditFeature!=null) {
			if(!CoordSearcher.isFeatureFromSearcher(feature)){
				featureInfoWidget.getButtonsHolder().add(btnEditFeature);	
			}
		}
		addBasicFields(feature);
		addLineProfile(feature, featureTable);
		addExportGPX(feature);
		loadAndAddManyLinks(feature);
		
	}
	
	protected void addExportGPX(final Feature feature){
		Repo.instance().getTable(feature.getTableId(), 0L, new AsyncCallback<Table>() {
			public void onSuccess(Table result){
				//this also checks for the public permissions
				if(!ClientSession.hasTablePermission(result, Permissions.ADMINPERMS)){
					return;
				}
				if(feature != null && feature.getGeometryType() != null && !feature.getGeometryType().equals(GeomType.NONE)){
					FlowPanel div = createRow(Messages.INSTANCE.FeatureInfoLabel_Export());
					final SGPushButton btnAnchor = new SGPushButton(
							Messages.INSTANCE.FeatureInfoValue_Export(), GeopediaStandardIcons.INSTANCE.export());
					btnAnchor.addStyleName("exportFeature");
					div.add(btnAnchor);
					btnAnchor.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							GWT.runAsync(new RunAsyncCallback() {
								
								@Override
								public void onSuccess() {
									ExportFeatureDialog efd = ExportFeatureDialog.createFor(feature);
									efd.show();
								}
								
								@Override
								public void onFailure(Throwable reason) {							
								}
							});
						}
					});
					add(div);
				}
			}
			public void onFailure(Throwable caught){
			}
		});
		
	}

	private static final String PROP_SHOW_HEIGHT_PROFILE = "showHeightProfile";
	
	protected void addLineProfile(final Feature feature, Table featureTable) {
		if (hasLineProfile(feature, featureTable)) {
			FlowPanel div = createRow(Messages.INSTANCE
					.FeatureInfoLabel_Height());
			final SGPushButton btnAnchor = new SGPushButton(
					Messages.INSTANCE.FeatureInfoValue_Profile(), GeopediaCommonStyle.INSTANCE.profile());
			btnAnchor.addStyleName("profile");
			div.add(btnAnchor);
			btnAnchor.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					GWT.runAsync(new RunAsyncCallback() {
						
						@Override
						public void onSuccess() {
							HeightProfileDialog spd = new HeightProfileDialog();
							spd.center();
							spd.showProfile(feature.tableId, feature.id);
						}
						
						@Override
						public void onFailure(Throwable reason) {							
						}
					});
				}
			});
			add(div);
		}
	}
	
	private boolean hasLineProfile(Feature feature, Table featureTable){
		return feature.geomType.isLine() && (featureTable.properties == null || featureTable.properties.getBoolean(PROP_SHOW_HEIGHT_PROFILE, true));
	}

}
