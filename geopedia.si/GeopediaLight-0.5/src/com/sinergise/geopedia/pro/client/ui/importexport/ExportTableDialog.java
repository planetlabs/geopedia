package com.sinergise.geopedia.pro.client.ui.importexport;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.theme.dialogs.ProDialogsStyle;

public class ExportTableDialog  extends CenteredBox{
	FlowPanel contentPanel;
	ExportTablePanel  exportPanel;
	
	public ExportTableDialog(Table table) {
		ProDialogsStyle.INSTANCE.importExport().ensureInjected();
		addStyleName("importDialog");
		setHeaderTitle(ProConstants.INSTANCE.exportTable());
		
		contentPanel = new FlowPanel();
		setContent(contentPanel);
		exportPanel = getExportTablePanel(table);
		contentPanel.add(exportPanel);
	}
	
	protected ExportTablePanel getExportTablePanel(Table table){
		return new ExportTablePanel(table);
	}


	protected boolean onBeforeClose() {
		return exportPanel.canTerminate();
	}
	
}
