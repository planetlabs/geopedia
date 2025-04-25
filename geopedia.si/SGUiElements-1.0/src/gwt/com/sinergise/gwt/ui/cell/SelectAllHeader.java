package com.sinergise.gwt.ui.cell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

public abstract class SelectAllHeader extends Header<Boolean> {

	public SelectAllHeader(){
		super(new CheckboxCell());
	}
	
	
	public static class ListBased<T> extends SelectAllHeader {
		MultiSelectionModel<T> 		selectionModel;
		ListDataProvider<T> 		dataProvider;
		
		public ListBased(MultiSelectionModel<T> selectionModel, ListDataProvider<T> dataProvider) {
			super();
			this.selectionModel = selectionModel;
			this.dataProvider = dataProvider;
		}

		@Override
		public Boolean getValue() {
			return Boolean.valueOf(selectionModel.getSelectedSet().size() == dataProvider.getList().size());
		}
		
		@Override
	    public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
	        InputElement input = elem.getFirstChild().cast();
	        boolean isChecked = input.isChecked();
	        for (T element : dataProvider.getList()) {
	            selectionModel.setSelected(element, isChecked);
	        }
	    }
	}
	
	

}
