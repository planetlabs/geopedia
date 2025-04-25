package com.sinergise.generics.gwt.widgetprocessors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.core.IWidgetProcessor;
import com.sinergise.generics.gwt.widgets.table.GenericTableModel;
import com.sinergise.generics.gwt.widgets.table.TableDataProvider;

public class RemoteTableDataProvider extends TableValueBinderWidgetProcessor implements TableDataProvider{

	
	protected final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	protected String datasourceId;
	
	public RemoteTableDataProvider(String datasourceId) {
		this.datasourceId=datasourceId;
	}
	
	public String getDatasourceId() {
		return datasourceId;
	}
	
	@Override
	public IWidgetProcessor getWidgetProcessor() {
		return this;
	}
	
	@Override
	public void provideRowData(int startRow, int endRow, final GenericTableModel model,  AsyncCallback<ArrayValueHolder> callback) {
		DataFilter searchParameters = model.getSearchParameters();
		if (searchParameters == null) {
			searchParameters = new SimpleFilter();
		}
		
		CompoundDataFilter fltr = new CompoundDataFilter();
		fltr.setOrderFilter(model.getOrderParameters());
		fltr.addDataFilter(searchParameters, DataFilter.NO_FILTER);
		
		DataFilter userFilter = getUserFilter();
		if (userFilter != null) fltr.addDataFilter(userFilter, DataFilter.OPERATOR_AND);
		genericsService.getCollectionValues(modifyFilter(fltr), datasourceId, startRow, endRow, callback);
	}
	
	@Override
	public void provideRowData(int startRow, int endRow, final GenericTableModel model) {		
		notifyStartProcessing();
		provideRowData(startRow, endRow, model, new TableDataProviderCallback(model));		
	}
	
	public DataFilter getUserFilter() {
		return null;
	}
	
	protected DataFilter modifyFilter (DataFilter filter) {
		return filter;
	}
}
