package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Tooltips;

/**
 * @author tcerovski
 *
 */
public abstract class PinnableTab extends CloseableDetailTab {

	private HTML butPin = null;
	
	public PinnableTab(SGTabPanel detailsParent, Widget window, String tabTitle) {
		super(detailsParent, window, tabTitle);
		
		init();
	}
	
	private void init() {
		butPin = new HTML("");
		butPin.setStyleName("sgwebui-detailsTab-pin");
		butPin.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				pin();
			}
		});
		butPin.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				butPin.addStyleDependentName("hover");
			}
		});
		
		butPin.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				butPin.removeStyleDependentName("hover");
			}
		});
		butPin.setTitle(Tooltips.INSTANCE.tab_pin());
		
		pTab.insert(butPin, pTab.getWidgetIndex(butClose));
	}
	
	public void pin() {
		butPin.setStyleName("sgwebui-detailsTab-pinned");
		butPin.setTitle("");
		onPinned();
	}
	
	public abstract void onPinned();
	
}
