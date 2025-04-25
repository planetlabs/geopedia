package com.sinergise.generics.gwt.widgets.table;

import java.util.Collection;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.OrderFilter;

public interface GenericTableModel {

	/**
	 * Total count of possible dataset data
	 * @param totalRowCount
	 */
	public void setRowCount (int rowCount);
	public void getRowData (int startRow, int endRow);
	public DataFilter getSearchParameters();
	public void clearSearchParameters();
	public OrderFilter getOrderParameters();
	public void updateTable();
	public void setDataLocation(int startDataIdx, int endDataIdx, boolean hasMoreData);
	public void setTotalRecordsCount(int count);
	
	
	public void addEventsHandler(TableEventsHandler handler);
	public Collection<TableEventsHandler> getEventsHandlerCollection();
	
	public  Collection<GenericObjectProperty> getAttributes();
	
}
