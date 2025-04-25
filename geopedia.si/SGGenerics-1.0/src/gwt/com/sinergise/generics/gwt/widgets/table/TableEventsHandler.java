package com.sinergise.generics.gwt.widgets.table;

import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.sinergise.generics.core.ArrayValueHolder;

public interface TableEventsHandler {
	/**
	 * Fired when table column label is clicked
	 */
	void onColumnLabelClicked(TableColumn column);
	/**
	 * Fired when table cell is selected
	 * 
	 */
	void onCellClicked(Cell cell, int rowIndex);
	
	/**
	 * Fired whenever new data is requested (page flip, filter change, ...)
	 */
	void newTableDataRequested();
	
	/**
	 * Called after new table data is received and applied to table model.
	 *   
	 * @param tableData Table data
	 */
	void newTableDataReceived(ArrayValueHolder tableData);
	
	/**
	 * Called if table data request has failed 
	 */
	void newTableDataRequestFailed (Throwable th);
	
	/**
	 * Table filters changed 
	 */
	void onFiltersChanged();
}
