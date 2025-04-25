package com.sinergise.generics.gwt.widgets.table;

import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.sinergise.generics.core.ArrayValueHolder;

/**
 * @author tcerovski
 *
 */
public class TableEventsAdapter implements TableEventsHandler {

	/* (non-Javadoc)
	 * @see com.sinergise.generics.gwt.widgets.table.TableEventsHandler#onColumnLabelClicked(com.sinergise.generics.gwt.widgets.table.TableColumn)
	 */	
	@Override
	public void onColumnLabelClicked(TableColumn column) { }

	/* (non-Javadoc)
	 * @see com.sinergise.generics.gwt.widgets.table.TableEventsHandler#onCellClicked(com.google.gwt.user.client.ui.HTMLTable.Cell, int)
	 */
	@Override
	public void onCellClicked(Cell cell, int rowIndex) { }

	/* (non-Javadoc)
	 * @see com.sinergise.generics.gwt.widgets.table.TableEventsHandler#newTableDataRequested()
	 */
	@Override
	public void newTableDataRequested() { }

	/* (non-Javadoc)
	 * @see com.sinergise.generics.gwt.widgets.table.TableEventsHandler#newTableDataReceived(com.sinergise.generics.core.ArrayValueHolder)
	 */
	@Override
	public void newTableDataReceived(ArrayValueHolder tableData) { }

	/* (non-Javadoc)
	 * @see com.sinergise.generics.gwt.widgets.table.TableEventsHandler#newTableDataRequestFailed(java.lang.Throwable)
	 */
	@Override
	public void newTableDataRequestFailed(Throwable th) { }

	/*
	 * (non-Javadoc)
	 * @see com.sinergise.generics.gwt.widgets.table.TableEventsHandler#onFiltersChanged()
	 */
	@Override
	public void onFiltersChanged() {}

}
