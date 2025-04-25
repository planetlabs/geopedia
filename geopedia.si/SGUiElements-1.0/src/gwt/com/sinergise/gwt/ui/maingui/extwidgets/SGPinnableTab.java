package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Tooltips;

/**
 * @author tcerovski
 *
 */
public abstract class SGPinnableTab extends SGCloseableTab {

	private Anchor butPin = null;
	
	public SGPinnableTab(SGTabLayoutPanel detailsParent, Widget window, String tabTitle) {
		super(detailsParent, window, tabTitle);
		init();
	}
	
	public SGPinnableTab(SGTabLayoutPanel detailsParent, Widget window, String tabTitle, ImageResource tabImg) {
		this(detailsParent, window, tabTitle);
		setImage(tabImg);
	}
	
	private void init() {
		butPin = new Anchor();
		butPin.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				pin();
			}
		});
		butPin.setStyleName("pin");
		butPin.setTitle(Tooltips.INSTANCE.tab_pin());
		pTab.addStyleName("pinnableTab");
		pTab.insert(butPin, pTab.getWidgetIndex(butClose));
	}
	
	public void pin() {
		butPin.addStyleName("pinned");
		butPin.setTitle("");
		onPinned();
	}
	
	public abstract void onPinned();
	
}
