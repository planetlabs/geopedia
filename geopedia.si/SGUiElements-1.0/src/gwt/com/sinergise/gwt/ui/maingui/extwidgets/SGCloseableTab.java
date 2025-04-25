/**
 * @author mtrebizan
 * 
 * Use only with SGTabLayoutPanel
 */
package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Tooltips;

public class SGCloseableTab extends Composite implements HasText {
	
	protected SGTabLayoutPanel 	detailsParent;
	protected Widget     		window;
	protected InlineLabel   	lbTitle;
	protected Anchor			butClose;
	protected Image 			tabImg;
	
	protected FlowPanel pTab;
	
	protected HandlerRegistration closeHandlerRegistration;
	
	protected final ClickHandler  DEFAULT_CLOSE_CLICK_HANDLER = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			close();
		}
	}; 
	
	public SGCloseableTab(SGTabLayoutPanel detailsParent, Widget window, String tabTitle) {
		this.detailsParent = detailsParent;
		this.window        = window;
		
		butClose = new Anchor();
		closeHandlerRegistration = butClose.addClickHandler(DEFAULT_CLOSE_CLICK_HANDLER);
		butClose.setStyleName("closeTab");
		butClose.setTitle(Tooltips.INSTANCE.tab_close());
		
		pTab = new FlowPanel();
		lbTitle = new InlineLabel(tabTitle);
		pTab.add(lbTitle);
		pTab.add(butClose);
		pTab.addStyleName("closeableTab");
		this.setTitle(tabTitle);
		
		initWidget(pTab);
	}
	
	public SGCloseableTab(SGTabLayoutPanel detailsParent, Widget window, String tabTitle, ImageResource tabImg) {
		this(detailsParent, window, tabTitle);
		setImage(tabImg);
	}

	@Override
	public void setTitle(String tabTitle) {
		lbTitle.setTitle(tabTitle);
	}
	
	public void setImage(ImageResource imgRes) {
		if (tabImg == null) {
			tabImg = new Image(imgRes);
			pTab.insert(tabImg,0);
		} else {
			tabImg.setResource(imgRes);
		}
	}
	
	@Override
	public String getTitle() {
		return lbTitle.getText();
	}
	
	public boolean canClose() {
		return true;
	}
	
	public void close() {
		if (canClose()) {
			this.detailsParent.closeTab(window);
			onClose();
		}
	}
	
	protected void onClose() { }

	@Override
	public String getText() {
		return lbTitle.getText();
	}

	@Override
	public void setText(String text) {
		lbTitle.setText(text);
	}
		
	
}