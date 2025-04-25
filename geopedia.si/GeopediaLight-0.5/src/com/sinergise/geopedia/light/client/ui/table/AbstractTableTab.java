package com.sinergise.geopedia.light.client.ui.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.ui.panels.StackableTabPanel;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource.EntityChangedListener;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;

public abstract class AbstractTableTab  extends StackableTabPanel {

	
	protected ImageAnchor btnCloseTab;
	protected Table table;
	public AbstractTableTab() {
		btnCloseTab = new ImageAnchor(GeopediaStandardIcons.INSTANCE.closeWhite());
		btnCloseTab.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (canDeactivate())
					onDestroy();	
			}
		});
		btnCloseTab.addStyleName("fl-right");
		btnCloseTab.setTitle(LightMessages.INSTANCE.closeTab());
		addButton(btnCloseTab);
		
		Repo.instance().addTableChangedListener(new EntityChangedListener<Table>() {
			
			@Override
			public void onEntityChanged(IsEntityChangedSource source, Table value) {
				if (value!=null && value.equals(table)) {
					setTable(value);
				}
			}
		});
	}
	
	public Table getTable() {
		return table;
	}
	
	public void setTable(Table table) {
		//TODO: fix copying types
		if (StringUtil.isNullOrEmpty(table.descDisplayableHtml)) {
			showLoadingIndicator(true);
			Repo.instance().getTable(table.getId(), table.lastMetaChange, new AsyncCallback<Table>() {
				
				@Override
				public void onSuccess(Table result) {
					showLoadingIndicator(false);
					AbstractTableTab.this.table=result;
					internalSetTable(AbstractTableTab.this.table);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					showLoadingIndicator(false);
					//TODO: show error
				}
			});
		} else {
			this.table=table;
			internalSetTable(table);
		}
	}
	protected abstract void internalSetTable(Table table);
}
