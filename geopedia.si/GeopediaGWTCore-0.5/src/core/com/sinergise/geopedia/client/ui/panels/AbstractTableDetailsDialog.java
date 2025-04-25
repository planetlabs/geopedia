package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TabPanel;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.CenteredBox;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.core.entities.Table;

public class AbstractTableDetailsDialog extends CenteredBox {
	protected FlowPanel contentPanel = null;
	
	
	private LoadingIndicator loadingPanel = null; 
	//private MapLayers mapLayers;
	
	
	public AbstractTableDetailsDialog() {
		contentPanel = new FlowPanel();
		setContent(contentPanel);
	}
	
	protected boolean onBeforeClose() {
		return true;
	}
	
	private void showLoadingPanel(boolean show) {
		if (show) {
			if (loadingPanel==null) {
				loadingPanel=new LoadingIndicator(true, true);
				loadingPanel.setSize("100%", "300px");
				contentPanel.add(loadingPanel);
			}
		} else {
			if (loadingPanel!=null) {
				contentPanel.clear();
				loadingPanel=null;
			}
		}
	}
	
	
	
	public void setTableId (int tableId, long metaTS) {
		showLoadingPanel(true);
		Repo.instance().getTable(tableId, metaTS, new AsyncCallback<Table>() {
			
			@Override
			public void onSuccess(Table result) {
				showLoadingPanel(false);
				showContent(result);
				show();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showLoadingPanel(false);
				showError(Messages.INSTANCE.LayerInfoDialog_LoadError());				
			}
		});
//		GMStats.stats(GMStats.DIALOG_LAYER_INFO, new String[]{GMStats.PARAM_THEME, GMStats.PARAM_LAYER}, 
//              new String[]{GMStats.getThemeID(mapLayers.getDefaultTheme()), String.valueOf(tableId)});
	}
	
	private void showError(String message) {
		contentPanel.clear();
		HTML error = new HTML();
		error.setStyleName("error");
		contentPanel.add(error);
	}
	
	
	protected TabPanel tabPanel = null;
	
	protected void showContent(Table tbl) {
		setHeaderTitle(tbl.getName());
		contentPanel.clear();
		tabPanel = new TabPanel();
		tabPanel.addStyleName("dialogTabPanel");
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {		
				ActivatableTabPanel tab = (ActivatableTabPanel)	tabPanel.getWidget(event.getSelectedItem());
				if (!tab.isActive())
					tab.activate();
			}
		});
		contentPanel.add(tabPanel);
	}
	
}