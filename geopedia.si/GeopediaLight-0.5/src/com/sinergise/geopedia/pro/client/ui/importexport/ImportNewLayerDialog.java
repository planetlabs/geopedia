package com.sinergise.geopedia.pro.client.ui.importexport;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.theme.dialogs.ProDialogsStyle;

public class ImportNewLayerDialog extends CenteredBox {
	FlowPanel contentPanel;
	ImportTablePanel ilPanel;
	MapWidget mapWidget;
	public ImportNewLayerDialog(MapWidget mapWidget) {
		ProDialogsStyle.INSTANCE.importExport().ensureInjected();
		addStyleName("importDialog");
		setHeaderTitle(ProConstants.INSTANCE.Import_ImportDialogTitle());
		
		this.mapWidget = mapWidget;
		contentPanel = new FlowPanel();
		setContent(contentPanel);
		ilPanel = new ImportTablePanel(mapWidget.getMapLayers());
		contentPanel.add(ilPanel);
	}


	protected boolean onBeforeClose() {
		return ilPanel.canTerminate();
	}
	
}
