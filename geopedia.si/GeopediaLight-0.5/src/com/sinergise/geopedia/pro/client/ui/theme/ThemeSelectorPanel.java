package com.sinergise.geopedia.pro.client.ui.theme;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.service.MetaServiceAsync;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.ui.AbstractEntitySelectorPanel;
import com.sinergise.gwt.ui.BoldText;

public class ThemeSelectorPanel extends AbstractEntitySelectorPanel<Theme> {

	private  static MetaServiceAsync metaService = RemoteServices.getMetaServiceInstance();
	
	@Override
	protected void queryEntities(Integer categoryId, String name, int startIdx, int stopIdx,final int page) {
		metaService.queryThemes(categoryId, name, startIdx, stopIdx, new AsyncCallback<PagableHolder<ArrayList<Theme>>>() {

			@Override
			public void onFailure(Throwable caught) {
				statusPanel.addException(caught);	
			}

			@Override
			public void onSuccess(PagableHolder<ArrayList<Theme>> result) {
				entityList.newPageData(result, page);
				
			}

	
		});
	}

	@Override
	protected void buildListItem(FlowPanel itemPanel, Theme item) {
		itemPanel.add(new BoldText());
		itemPanel.add(new InlineLabel(item.getName()));
		
	}

	@Override
	protected String entityNameEmptyText() {
		return GeopediaTerms.INSTANCE.themeName();
		
	}

}
