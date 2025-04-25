package com.sinergise.geopedia.light.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;
import com.sinergise.common.util.event.ActionPerformedListener;

public class SearchPanel extends FlowPanel {
	
	
	private TextBox searchBox;
	private Button btnSearch;
	
	private ActionPerformedListener<String> searchHandler = null;
	
	public SearchPanel() {
		setStyleName("search");
		addStyleName("bubble");
		InlineHTML searchLeft = new InlineHTML();
		searchLeft.setStyleName("b_left");
		InlineHTML searchMiddle = new InlineHTML();
		searchMiddle.setStyleName("b_middle");
		InlineHTML searchRight = new InlineHTML();
		searchRight.setStyleName("b_right");
		searchBox = new TextBox();
		searchBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doSearch();
				}
			}
		});
		btnSearch=new Button();
		searchBox.setStyleName("searchBox");
		btnSearch.setStyleName("btnSearch");
		
		add(searchLeft);
		add(searchMiddle);
		add(searchRight);
		add(searchBox);
		add(btnSearch);
		
		btnSearch.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doSearch();				
			}
			
		});
	}
	
	public void setFocus() {
		if (searchBox!=null) {
			searchBox.setFocus(true);
		}
	}
	
	public void setSearchHandler(ActionPerformedListener<String> handler) {
		searchHandler = handler;
	}
	

	private void doSearch() {
		String query = searchBox.getText();
		if (query==null || query.length()==0)
			return;
		
		if (searchHandler != null)
			searchHandler.onActionPerformed(query);
		
	}
}
