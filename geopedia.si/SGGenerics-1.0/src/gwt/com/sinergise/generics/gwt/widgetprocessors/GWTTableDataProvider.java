package com.sinergise.generics.gwt.widgetprocessors;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.IWidgetProcessor;
import com.sinergise.generics.gwt.widgetprocessors.helpers.GWTFilterProcessor;
import com.sinergise.generics.gwt.widgets.table.GenericTableModel;
import com.sinergise.generics.gwt.widgets.table.TableDataProvider;
import com.sinergise.generics.gwt.widgets.table.TableEventsHandler;

public class GWTTableDataProvider implements TableDataProvider {

	private ArrayValueHolder tableData = null;

	TypeAttribute orderAttribute = null;
	OrderOption orderKind = OrderOption.OFF;
	private TableValueBinderWidgetProcessor twp;
	private static boolean ignoreCaseInFilter = true;
	
	public  GWTTableDataProvider (TableValueBinderWidgetProcessor twp) {
		this.twp=twp;
	}
	public void setTableData(ArrayValueHolder tableData) {
		this.tableData=tableData;
	}
	
	@Override
	public ArrayValueHolder getTableData() {
		return tableData;
	}
	
	@Override
	public IWidgetProcessor getWidgetProcessor() {
		return twp;
	}
	
	public static ArrayValueHolder processData(ArrayValueHolder tableData, DataFilter filter, int startRow, int endRow,
			Collection<GenericObjectProperty> propList) {
		if (tableData==null)
			return null;
		EntityType entityType =  GwtEntityTypesStorage.getInstance().getEntityType(tableData.getEntityTypeId());
		ArrayValueHolder filteredData = new ArrayValueHolder(tableData.getEntityTypeId());		
		GWTFilterProcessor gfp = getFilterProcessor(propList);
		SimpleFilter sFilter = null;
		OrderFilter orderFilter = null;
		if (filter!=null) {
			if (filter instanceof SimpleFilter)
				sFilter = (SimpleFilter)filter;
			else if (filter instanceof CompoundDataFilter) {
				CompoundDataFilter compFilter = (CompoundDataFilter)filter;
				orderFilter = compFilter.getOrderFilter();
				if (compFilter.getFilter(0) instanceof SimpleFilter) {
					sFilter = (SimpleFilter)compFilter.getFilter(0);
				}
			}
		}
		for (ValueHolder vh:tableData) {
			if (sFilter!=null && sFilter.getFilterData()!=null) {
				if (gfp.matchesFilter(sFilter,(EntityObject)vh)) {
					filteredData.add(vh);				
				} 
			} else {
				filteredData.add(vh);	
			}
		}
		
		if (orderFilter!=null) {
			TypeAttribute orderAttribute = null;
			OrderOption orderKind = OrderOption.OFF;
	
			
			// order filtered
			OrderOption[] orderBy = orderFilter.getOrderBy();
			for (TypeAttribute ta: entityType.getAttributes()) {
				int idx = ta.getId();
				if (orderBy[idx]!=null && orderBy[idx].isOn()) {				
					orderKind=orderBy[idx];
					orderAttribute=ta;
					break;
				}
			}
			final OrderOption orderKind_f = orderKind;
			
			if (orderAttribute!=null && orderKind != OrderOption.OFF) {
				final int oType = orderAttribute.getPrimitiveType();
				final int oId = orderAttribute.getId();
				Collections.sort(filteredData, new Comparator<ValueHolder>() {
		
					@Override
					public int compare(ValueHolder o1, ValueHolder o2) {
						if (oType == Types.STUB) return 0; // Ignore stubs
						
						final EntityObject eo1 = (EntityObject)o1;
						final EntityObject eo2 = (EntityObject)o2;					
						
						String pv1 = eo1.getPrimitiveValue(oId);	
						String pv2 = eo2.getPrimitiveValue(oId);
	
						int retVal = StringUtil.compare(pv1, pv2);
						if (retVal == 0) return 0;
						
						if (oType == Types.INT ||
							oType == Types.FLOAT ||
							oType == Types.DATE) {
							retVal = StringUtil.compareAsNumber(pv1, pv2);
						}
						if (orderKind_f == OrderOption.DESC) {
							retVal*=-1;
						}
						return retVal;
					}
				});
			}		
		}
		
		// cut filtered and ordered data to [startRow,endRow] window
		ArrayValueHolder avh = filteredData;
		avh.setHasMoreData(false);
		if (startRow>=0 && endRow>0 && filteredData.size()>0) {
			avh = new ArrayValueHolder(filteredData.getEntityTypeId());

			int start = startRow;
			int end=endRow;
			if (end>=(filteredData.size()-1)) {
				end=filteredData.size()-1;
				avh.setHasMoreData(false);
			} else {
				avh.setHasMoreData(true);
			}
			
			if (start<filteredData.size())
				for (int i=start;i<=end;i++) {
					avh.add(filteredData.get(i));
				}
		}
		avh.setTotalDataCount(filteredData.size());
		return filteredData;
	}
	
	private static GWTFilterProcessor getFilterProcessor(Collection<GenericObjectProperty> propList) {
		GWTFilterProcessor filterProcc = new GWTFilterProcessor(propList);
		filterProcc.setIgnoreCase(ignoreCaseInFilter);
		return filterProcc;
	}
	
	@Override
	public void provideRowData(int startRow, int endRow, final GenericTableModel model,  AsyncCallback<ArrayValueHolder> callback) {
		EntityType entityType =  GwtEntityTypesStorage.getInstance().getEntityType(tableData.getEntityTypeId());
		
		// filter data
		DataFilter searchFilterParameters = model.getSearchParameters();		
		OrderFilter orderFilter = model.getOrderParameters();
		// TODO: full filter support?
		GWTFilterProcessor gfp = getFilterProcessor(model.getAttributes());
		ArrayValueHolder filteredData = new ArrayValueHolder(tableData.getEntityTypeId());
		for (ValueHolder vh:tableData) {
			if (gfp.matchesFilter((SimpleFilter)searchFilterParameters,(EntityObject)vh)) {
				filteredData.add(vh);				
			} 
		}
		
		
		// order filtered
		OrderOption[] orderBy = orderFilter.getOrderBy();
		for (TypeAttribute ta: entityType.getAttributes()) {
			int idx = ta.getId();
			if (orderBy[idx]!=null && orderBy[idx].isOn()) {				
				orderKind=orderBy[idx];
				orderAttribute=ta;
				break;
			}
		}
		
		if (orderAttribute!=null && orderKind != OrderOption.OFF) {
			final int oType = orderAttribute.getPrimitiveType();
			final int oId = orderAttribute.getId();
			Collections.sort(filteredData, new Comparator<ValueHolder>() {
	
				@Override
				public int compare(ValueHolder o1, ValueHolder o2) {
					if (oType == Types.STUB) return 0; // Ignore stubs
					
					final EntityObject eo1 = (EntityObject)o1;
					final EntityObject eo2 = (EntityObject)o2;					
					
					String pv1 = eo1.getPrimitiveValue(oId);	
					String pv2 = eo2.getPrimitiveValue(oId);

					int retVal = StringUtil.compare(pv1, pv2);
					if (retVal == 0) return 0;
					
					if (oType == Types.INT ||
						oType == Types.FLOAT ||
						oType == Types.DATE) {
						retVal = StringUtil.compareAsNumber(pv1, pv2);
					}
					if (orderKind == OrderOption.DESC) {
						retVal*=-1;
					}
					return retVal;
				}
			});
		}		
		
		// cut filtered and ordered data to [startRow,endRow] window
		ArrayValueHolder avh = filteredData;
		avh.setHasMoreData(false);
		if (startRow>=0 && endRow>0 && filteredData.size()>0) {
			avh = new ArrayValueHolder(filteredData.getEntityTypeId());

			int start = startRow;
			int end=endRow;
			if (end>=(filteredData.size()-1)) {
				end=filteredData.size()-1;
				avh.setHasMoreData(false);
			} else {
				avh.setHasMoreData(true);
			}
			
			if (start<filteredData.size())
				for (int i=start;i<=end;i++) {
					avh.add(filteredData.get(i));
				}
		}
		avh.setTotalDataCount(filteredData.size());

		callback.onSuccess(avh);
	}
	@Override
	public void provideRowData(int startRow, int endRow, final GenericTableModel model) {
		if (tableData==null) {
			model.setRowCount(0);
			model.updateTable();
			return;
		}
		
		provideRowData(startRow, endRow, model, new AsyncCallback<ArrayValueHolder>() {

			@Override
			public void onFailure(Throwable caught) {
				// never happens
			}

			@Override
			public void onSuccess(ArrayValueHolder avh) {
				// apply data to table model
				twp.setTableData(avh);
				
				model.setRowCount(avh.size());
				model.setDataLocation(avh.getDataLocationStart(), avh.getDataLocationEnd(), avh.hasMoreData());
				model.setTotalRecordsCount(avh.getTotalDataCount());
				model.updateTable();

				for (TableEventsHandler teh:model.getEventsHandlerCollection()) {
					teh.newTableDataReceived(avh);
				}		
			}
		});
	}
	
	public void setIgnoreCaseInFilter(boolean ignoreCaseInFilter) {
		GWTTableDataProvider.ignoreCaseInFilter = ignoreCaseInFilter;
	}

}
