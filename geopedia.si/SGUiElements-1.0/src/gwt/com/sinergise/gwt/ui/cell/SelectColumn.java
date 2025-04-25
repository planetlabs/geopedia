package com.sinergise.gwt.ui.cell;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.SelectionModel;

public class SelectColumn<T> extends Column<T, Boolean> {

	SelectionModel<T> selectionModel;
	
	public SelectColumn(SelectionModel<T> selectionModel){
		super(new CheckboxCell(true, false));
		this.selectionModel = selectionModel; 
	}
	
	@Override
	public  Boolean getValue(T object) {
	    // Get the value from the selection model.
	    return Boolean.valueOf(selectionModel.isSelected(object));
    }

}
