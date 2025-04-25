package com.sinergise.geopedia.pro.client.ui.table;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;

@Deprecated
public class AdvancedSearchDialog extends AbstractDialogBox {

	public AdvancedSearchDialog(Table table) {		
		super(false, true,false,true);
		if (Window.getClientHeight() < 800) {
			setSize(Window.getClientWidth()-150+"px", Window.getClientHeight()-150+"px");
		} else {
			setSize(Window.getClientWidth()-150+"px", "675px");
		}
		FlowPanel contentPanel = new FlowPanel();
		contentPanel.add(createCloseButton());
		contentPanel.add(new FeatureDataGrid(table));
		setWidget(contentPanel);				
	}
	
	@Override
	protected boolean dialogResizePending(int width, int height) {
		return true;
	}
}
