package com.sinergise.geopedia.pro.client.ui.table;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.client.ui.widgets.style.JSStyleEditorWidget;

public class TableStyleEditorPanel extends AbstractEntityEditorPanel<Table> {

	/*
	public void saveValue(Table table) {
		
	}
	*/
	Table table;
	JSStyleEditorWidget styleEditor =new JSStyleEditorWidget();
	public TableStyleEditorPanel() {
		addStyleName("style");
		add(styleEditor);
	}
	
	
	@Override
	public void loadEntity(Table table) {
		this.table=table;
		setWidgetValue();
	}
	
	private void setWidgetValue() {
		styleEditor.setValue(table.getGeometryType(), table.getStyle());
	}

	@Override
	public boolean saveEntity(Table table) {
		try {
			table.setStyle(styleEditor.getValue());
		} catch (GeopediaException ex) {
			return false;
		}
		if (table.getStyle()==null)
			return false;
		return true;
	}

	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}

}
