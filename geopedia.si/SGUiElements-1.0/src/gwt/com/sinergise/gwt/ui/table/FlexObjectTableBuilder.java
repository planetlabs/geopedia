package com.sinergise.gwt.ui.table;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

/**	
 * Row object aware FlexTableBuilder. 
 * 
 * @author tcerovski
 */
public class FlexObjectTableBuilder<T> extends FlexTableBuilder {
	
	public static final String SELECTED_STYLE = "selected";
	
	private final Map<T, FlexTableRow> objectRowMap = new HashMap<T, FlexTableRow>();
	
	private SelectionModel<T> selectionModel = new NoSelectionModel<T>();
	private HandlerRegistration selectionHandlerReg = null;

	public FlexObjectTableBuilder() {
		super();
	}

	public FlexObjectTableBuilder(String tableStyle) {
		super(tableStyle);
	}
	
	public void bindObjectWithCurrentRow(final T object) {
		objectRowMap.put(object, getCurrentRow());
		
		addCurrentRowClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				selectionModel.setSelected(object, true);
			}
		});
	}
	
	public void setSelectionModel(SelectionModel<T> selectionModel) {
		this.selectionModel = selectionModel;
		
		if (selectionHandlerReg  != null) {
			selectionHandlerReg.removeHandler();
		}
		
		updateSelected();
		selectionHandlerReg = selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				updateSelected();
			}
		});
	}
	
	public SelectionModel<T> getSelectionModel() {
		return selectionModel;
	}
	
	private void updateSelected() {
		for (T object : objectRowMap.keySet()) {
			objectRowMap.get(object).setStyleName(SELECTED_STYLE, selectionModel.isSelected(object));
		}
	}
	
	
	
}
