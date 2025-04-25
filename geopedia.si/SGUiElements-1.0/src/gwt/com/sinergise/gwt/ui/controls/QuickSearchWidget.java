/**
 * 
 */
package com.sinergise.gwt.ui.controls;

import static com.sinergise.gwt.ui.StyleConsts.QUICK_SEARCH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.ui.i18n.Labels;
import com.sinergise.common.ui.i18n.Tooltips;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.MessageBox;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;


/**
 * @author tcerovski
 */
public abstract class QuickSearchWidget extends Composite {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected FlowPanel mainPanel = new FlowPanel();
	protected SGTextBox textBox = new SGTextBox();
	
	/** Message box for default error handling */
	protected MessageBox msgBox = new MessageBox();
	
	public QuickSearchWidget() {
		this(Labels.INSTANCE.quickSearch());
	}
	
	public QuickSearchWidget(String emptyText) {
		
		mainPanel.setStyleName(QUICK_SEARCH);
		
		final InlineHTML imgContainer = new InlineHTML("");
		mainPanel.add(textBox);
		mainPanel.add(imgContainer);
		
		SimplePanel rSide = new SimplePanel();
		rSide.setStyleName("rSide");
		mainPanel.add(rSide);
		
		FlowPanel outer = new FlowPanel();
		outer.add(mainPanel);
		outer.add(msgBox);
		initWidget(outer);
		
		//set styles
		textBox.setStyleName("textbox");
		imgContainer.setStyleName("icon");
		
		textBox.setEmptyText(emptyText);
		imgContainer.setTitle(Tooltips.INSTANCE.quickSearchButton());
		
		//add event handlers
		imgContainer.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int_doSearch();
			}
		});
		
		textBox.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					int_doSearch();
				}
			}
		});
	}
	
	protected void handleException(Exception e) {
		logger.error(e.getMessage(), e);
		
		if(msgBox != null) {
			msgBox.showErrorMsg(e.getMessage());
		}
	}
	
	private void int_doSearch() {
		String query = StringUtil.trimNullEmpty(textBox.getText());
		if(query == null || textBox.isEmpty())
			return;
		
		textBox.clear();
		if(msgBox != null) {
			msgBox.hide();
		}
		doSearch(query);
	}
	
	protected abstract void doSearch(String query);
}
