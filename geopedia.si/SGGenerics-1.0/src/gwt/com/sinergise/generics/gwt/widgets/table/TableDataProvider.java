package com.sinergise.generics.gwt.widgets.table;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.gwt.core.IWidgetProcessor;

public interface TableDataProvider extends HasTableData {
	public void provideRowData(int startRow, int endRow, GenericTableModel table);
	public void provideRowData(int startRow, int endRow, GenericTableModel model,  AsyncCallback<ArrayValueHolder> callback);
	public IWidgetProcessor getWidgetProcessor();
}
