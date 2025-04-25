package com.sinergise.generics.gwt.widgets.table;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericActionListener;
import com.sinergise.generics.gwt.core.EntityObjectToken;

public class TableRowSelectionHandler implements TableEventsHandler{
	
	
	private class TokenProcessor {
		public TokenProcessor(AsyncCallback<EntityObjectToken> callback, boolean first, boolean last) {
			this.first = first;
			this.last = last;
			this.callback = callback;
		}
		AsyncCallback<EntityObjectToken> callback;
		boolean first=false;
		boolean last=false;		
	}
	
	private ArrayValueHolder tableData = null;
	private EntityObjectToken selectedToken = EntityObjectToken.EMPTY_TOKEN;
	private TokenProcessor tokenToProcess = null;
	private List<GenericActionListener<EntityObjectToken>> rowSelectedListeners = new ArrayList<GenericActionListener<EntityObjectToken>>();
	
	private GenercsTable table = null;

	
	public TableRowSelectionHandler (GenercsTable table){
		this.table=table;
	}
	
	
	
	private boolean checkTokenValidity(EntityObjectToken token){
		if (token==null)
			return false;
		if (!token.exists())
			return false;
		if (token.equals(selectedToken))
			return true;
		return false;
	}
	public void getNextToken(EntityObjectToken token, AsyncCallback<EntityObjectToken> callback){
		if (!checkTokenValidity(token))
			return;
		// TODO: exception if table data is null
		// TODO: return error if 
		if ((token.getTokenIndex()+1)< tableData.size()) { // data already exists fetch not needed
			int newIdx = token.getTokenIndex()+1;
			token.setTokenIndex(newIdx);
			token.setEntityObject((EntityObject) tableData.get(newIdx));
			processRowSelection(token);
			callback.onSuccess(token);
		} else {
			if (!tableData.hasMoreData())
				return;
			tokenToProcess = new TokenProcessor(callback,true, false);
			table.getTableWidget().nextPage();
		}
		
	}
	
	public void getPreviousToken(EntityObjectToken token, AsyncCallback<EntityObjectToken> callback){
		if (!checkTokenValidity(token))
			return;
		// TODO: exception if table data is null
		// TODO: return error if 
		if ((token.getTokenIndex()-1)>=0) { // data already exists fetch not needed
			int newIdx = token.getTokenIndex()-1;
			token.setTokenIndex(newIdx);
			token.setEntityObject((EntityObject) tableData.get(newIdx));
			processRowSelection(token);
			callback.onSuccess(token);
		} else {
			if (table.getTableWidget().getCurrentPage()==0)
				return;
			tokenToProcess = new TokenProcessor(callback,false,true);
			table.getTableWidget().previousPage();
		}
		
	}
	
	
	public void addRowSelectedListener(GenericActionListener<EntityObjectToken> listener){
		rowSelectedListeners.add(listener);
	}
	
	public EntityObjectToken getSelected(){
		return selectedToken;
	}

	@Override
	public void onColumnLabelClicked(TableColumn column) {
		// ignore					
	}
	
	
	private void processRowSelection(EntityObjectToken token){
		PagingTable tableWidget = table.getTableWidget();
		tableWidget.setSelectedDataRows(false);
		
		if (token.exists()) {
			tableWidget.setSelectedDataRow(token.getTokenIndex(), true);
		}
		selectedToken = token;
		for (GenericActionListener<EntityObjectToken> listener:rowSelectedListeners)
			listener.actionPerformed(selectedToken);
	}
	
	@Override
	public void onCellClicked(Cell cell, int rowIndex) {
		PagingTable tableWidget = table.getTableWidget();
		tableWidget.setSelectedDataRows(false);
		
		if (selectedToken.getTokenIndex() == rowIndex)
			processRowSelection(EntityObjectToken.EMPTY_TOKEN);
		else {
			tableWidget.setSelectedDataRow(rowIndex, true);
			if (tableData!=null) {
				EntityObjectToken token = new EntityObjectToken(rowIndex, (EntityObject) tableData.get(rowIndex));
				processRowSelection(token);
			}
		}
	}

	@Override
	public void newTableDataRequested() {
		// deselect everything on data change
		PagingTable tableWidget = table.getTableWidget();
		tableWidget.setSelectedDataRows(false);
		processRowSelection(EntityObjectToken.EMPTY_TOKEN);
	}

	@Override
	public void newTableDataReceived(ArrayValueHolder tableData) {
		this.tableData = tableData;
		if (tokenToProcess != null) {
			
			int index = 0;
			if (tokenToProcess.last)
				index = tableData.size()-1;
			EntityObjectToken token = new EntityObjectToken(index,(EntityObject) tableData.get(index));
			processRowSelection(token);
			tokenToProcess.callback.onSuccess(token);
			tokenToProcess=null;
		}
	}



	@Override
	public void newTableDataRequestFailed(Throwable th) {
		if (tokenToProcess!=null){
			tokenToProcess.callback.onFailure(th);
			tokenToProcess = null;
		}
		
	}



	@Override
	public void onFiltersChanged() {
		// TODO Auto-generated method stub
		
	}
}
