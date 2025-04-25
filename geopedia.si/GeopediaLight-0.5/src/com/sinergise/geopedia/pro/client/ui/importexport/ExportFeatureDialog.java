package com.sinergise.geopedia.pro.client.ui.importexport;

import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.service.params.ExportSettings;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.SettingsStack;

public class ExportFeatureDialog extends ExportTableDialog {

	protected Feature feature;
	
	protected ExportFeatureDialog(Feature feature, Table featureAsTable) {
		super(featureAsTable);
		setHeaderTitle(ProConstants.INSTANCE.exportFeature());
		
		this.feature = feature;
	}
	
	public static ExportFeatureDialog createFor(Feature feature){
		Table temp = new Table();
		
		temp.setId(feature.getTableId());
		temp.fields = feature.getFields();
		temp.setGeomType(feature.getGeometryType());
		temp.envelope = feature.envelope;
		
		return createFor(feature, temp);
	}
	
	public static ExportFeatureDialog createFor(Feature feature, Table table){
		return new ExportFeatureDialog(feature, table);
	}
	
	@Override
	protected ExportTablePanel getExportTablePanel(Table table) {
		return new ExportFeaturePanel(table);
	}
	
	protected class ExportFeaturePanel extends ExportTablePanel {
		
		public ExportFeaturePanel(Table table) {
			super(table);
		}
		
		@Override
		protected void addExportFieldsSP(SettingsStack exportSettingsStack, Table table){
			//for returning GPX or SHP there is no need to choose fields
			//we initialize it only because of NPE
			exportFields = new ExportFieldsSP();
			return;
		}
		

		@Override
		protected void addFormatSettings(SettingsStack exportSettingsStack, Table table){
			formatSettings = new FormatSettings(table);
			exportSettingsStack.add(formatSettings);
		}
		@Override
		protected void addExtraSettings(SettingsStack exportSettingsStack, Table table){
			//for returning GPX or SHP there is no need to have extra settings at least not now
			//we initialize it only because of NPE
			extraSettings = new ExtraSettings();
			return;
		}

		public class FormatSettings extends ExportTablePanel.FormatSettings {
			public FormatSettings(Table table) {
				super(table);
			}

			@Override
			protected void populateFormatBox(Table table, ListBox lbFormat) {
				if (table.hasGeometry()) {
					lbFormat.addItem("GPX",String.valueOf(ExportSettings.FMT_GPX));
				}
			}
			
		}

		@Override
		protected ExportSettings prepareExportSettings() {
			ExportSettings es = super.prepareExportSettings();
			es.filterDescriptor = FilterFactory.createIdentifierDescriptor(es.tableID, feature.id);
			return es;
		}
		
	}

}
