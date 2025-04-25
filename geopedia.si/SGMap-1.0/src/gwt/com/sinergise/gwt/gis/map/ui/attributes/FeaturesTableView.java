package com.sinergise.gwt.gis.map.ui.attributes;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureUtils.FeatureInfoDisplayBuilder;
import com.sinergise.common.gis.feature.CFeatureUtils.PropertyDisplayData;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

//TODO: for performance reasons, refactor to use DataGrid when GWT 2.5 is used
//TODO: implement single selection model
public class FeaturesTableView extends SGFlowPanel {
	
	protected final CFeatureDescriptor featureDescriptor;
	protected final HasFeatures featuresProvider;
	protected final SelectionModel<CFeature> selection;
	
	protected CheckBox cbSelectAll;
	private SimplePanel tableHolder;
	
	private FeatureInfoDisplayBuilder infoBuilder;
	private PropertyWidgetFactory propWFactory = new ReadOnlyPropertyWidgetFactory();

	public FeaturesTableView(CFeatureDescriptor featureDescriptor, HasFeatures featuresProvider) {
		this(featureDescriptor, featuresProvider, null);
	}
	
	public FeaturesTableView(CFeatureDescriptor featureDescriptor, 
		HasFeatures featuresProvider, SelectionModel<CFeature> selection) 
	{
		this.featureDescriptor = featureDescriptor;
		this.featuresProvider = featuresProvider;
		this.selection = selection;
		
		init();
		refresh();
	}
	
	protected void init() {
		add(tableHolder = new SimplePanel());
		tableHolder.setStyleName("sortingTbl");
		
		infoBuilder = new FeatureInfoDisplayBuilder(featureDescriptor);
		infoBuilder.limitForSummary();
		
		if (useMultiSelectionModel()) {
			cbSelectAll = new CheckBox();
			cbSelectAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					setSelectionForAll(event.getValue().booleanValue());
				}
			});
		}
	}
	
	protected final boolean useMultiSelectionModel() {
		return selection instanceof MultiSelectionModel;
	}
	
	private void setSelectionForAll(boolean selected) {
		if (!useMultiSelectionModel()) {
			throw new IllegalStateException("Not a MultiSelectionModel");
		}
		
		for (CFeature f : featuresProvider.getFeatures()) {
			selection.setSelected(f, selected);
		}
	}
	
	public void refresh() {
		
		FlexTableBuilder ftb = new FlexTableBuilder();
		
		addHeaderRow(ftb);
		for (CFeature f : featuresProvider.getFeatures()) {
			addFeatureRow(f, ftb);
		}
		
		FlexTable table = ftb.buildTable();
		table.setWidth("100%");
		tableHolder.setWidget(table);
	}
	
	protected void addHeaderRow(FlexTableBuilder ftb) {
		
		if (useMultiSelectionModel()) {
			ftb.addTitleWidget(cbSelectAll);
		}
		
		for (PropertyDescriptor<?> pd : infoBuilder.getProperties()) {
			//TODO: add sortable column widget
			ftb.addTitle(pd.getTitle());
		}
		
		ftb.newRow();
	}
	
	protected void addFeatureRow(CFeature feature, FlexTableBuilder ftb) {
		
		addFeatureSelectionControl(feature, ftb);
		
		for (PropertyDisplayData<?> data : infoBuilder.getValues(feature)) {
			ftb.addFieldValueWidget(propWFactory.createValueWidget(data));
		}
		
		ftb.newRow();
	}
	
	protected void addFeatureSelectionControl(final CFeature feature, FlexTableBuilder ftb) {
		
		if (useMultiSelectionModel()) {
			
			final CheckBox cb = new CheckBox();
			cb.setValue(Boolean.valueOf(selection.isSelected(feature)));
			cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					selection.setSelected(feature, event.getValue().booleanValue());
				}
			});
			
			selection.addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					cb.setValue(Boolean.valueOf(selection.isSelected(feature)));
				}
			});
		
			ftb.addFieldValueWidget(cb);
		}
	}
	
	

}
