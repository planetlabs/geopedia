package com.sinergise.geopedia.pro.client.ui.table;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.service.MetaServiceAsync;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.ui.AbstractEntitySelectorPanel;
import com.sinergise.gwt.ui.BoldText;

public class TableSelectorPanel extends AbstractEntitySelectorPanel<Table> {

private  static MetaServiceAsync metaService = RemoteServices.getMetaServiceInstance();
	
	@Override
	protected void queryEntities(Integer categoryId, String name, int startIdx, int stopIdx,final int page) {
		metaService.queryTables(categoryId, name, startIdx, stopIdx, new AsyncCallback<PagableHolder<ArrayList<Table>>>() {

			@Override
			public void onFailure(Throwable caught) {
				statusPanel.addException(caught);	
			}

			@Override
			public void onSuccess(PagableHolder<ArrayList<Table>> result) {
				entityList.newPageData(result, page);
				
			}

	
		});
	}

	@Override
	protected void buildListItem(FlowPanel itemPanel, Table item) {
		itemPanel.addStyleName("type "+item.getGeomType().name());
		itemPanel.add(new BoldText());
		itemPanel.add(new InlineLabel(item.getName()));
		
	}

	@Override
	protected String entityNameEmptyText() {
		return GeopediaTerms.INSTANCE.layerName();
	}
}
