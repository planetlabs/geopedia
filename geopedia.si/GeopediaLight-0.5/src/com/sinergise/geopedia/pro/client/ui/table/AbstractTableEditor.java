package com.sinergise.geopedia.pro.client.ui.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditor;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;

public abstract class AbstractTableEditor extends AbstractEntityEditor<Table> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTableEditor.class);
	
	public Table getTable() {
		return entity;
	}
	
	
	public AbstractEntityEditorPanel<Table> addPanel (AbstractEntityEditorPanel<Table> panel) {
		tabs.add(panel);
		return panel;
	}

	protected void saveEntity(Table entity) {
		Repo.instance().saveTable(entity, new AsyncCallback<Table>() {
			
			@Override
			public void onSuccess(Table result) {
				showLoadingIndicator(false);
				setEntity(result);
				close(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showLoadingIndicator(false);
				showError(ExceptionI18N.getLocalizedMessage(caught), true);
				logger.error("Save failed!",caught);				
			}
		});
	}
	
	
	public void setTable(int tableId, long metadataTS) {
		showLoadingIndicator(true);
		showError(null,false);
		Repo.instance().getTable(tableId, metadataTS, new AsyncCallback<Table>() {

			@Override
			public void onFailure(Throwable caught) {
				showLoadingIndicator(false);
				showError(ExceptionI18N.getLocalizedMessage(caught), true);
				logger.error("Loading failed!",caught);
			}

			@Override
			public void onSuccess(Table result) {
				showLoadingIndicator(false);
				setEntity(result);
			}
		});
	}
}
