package com.sinergise.gwt.ui.controls;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.ui.i18n.Tooltips;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

/**
 * @author tcerovski
 *
 */
public abstract class FilterInputWidget extends Composite {
	
	private FlowPanel panel = null;
	private SGTextBox textBox = new SGTextBox();
	private InlineHTML imgContainer = null;

	public FilterInputWidget() {
		initUI();
		updateUI();
	}
	
	private void initUI() {
		panel = new FlowPanel();
		panel.setStyleName(StyleConsts.FILTER_WIDGET);
		
		imgContainer = new InlineHTML();
		SimplePanel rSide = new SimplePanel();
		rSide.setStyleName("rSide");
		panel.add(imgContainer);
		panel.add(textBox);
		panel.add(rSide);
		initWidget(panel);
		
		textBox.setEmptyText(Tooltips.INSTANCE.filterWidget_emptyText());
		textBox.setTitle(Tooltips.INSTANCE.filterWidget_title());
		
		//set styles
		textBox.setStyleName("textbox");
		imgContainer.setStyleName("icon");
		
		//add event handlers
		imgContainer.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isFilterOn()) {
					clearFilter();
				}
			}
		});
		
		textBox.addKeyUpHandler(new KeyUpHandler() {
			
			Timer keyUpTimer = new Timer() {
				@Override
				public void run() {
					doApplyFilter(textBox.getText());
					updateUI();
				}
			};
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				keyUpTimer.schedule(400);
			}
		});
	}
	
	private void updateUI() {
		if (isFilterOn()) {
			panel.addStyleName("on");
			imgContainer.setTitle(Tooltips.INSTANCE.filterWidget_clear());
		} else {
			panel.removeStyleName("on");
			imgContainer.setTitle(Tooltips.INSTANCE.filterWidget_title());
		}
	}
	
	public void applyFilter(String filterText) {
		textBox.setText(filterText); //set to UI
		doApplyFilter(filterText);
		updateUI();
	}
	
	public void clearFilter() {
		textBox.setText(""); //set to UI
		doApplyFilter(null);
		updateUI();
	}
	
	protected abstract void doApplyFilter(String filterText);
	
	protected abstract boolean isFilterOn();
	
}
