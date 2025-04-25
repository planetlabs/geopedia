/**
 * 
 */
package com.sinergise.gwt.ui.maingui.extwidgets;

import static com.google.gwt.user.client.ui.HasVerticalAlignment.ALIGN_MIDDLE;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Tooltips;

public class CloseableDetailTab extends Composite {
	
	protected SGTabPanel detailsParent;
	protected Widget     window;
	protected Label      lbTitle;
	protected HTML 		 butClose;
	
	protected HorizontalPanel pTab;
	
	public CloseableDetailTab(SGTabPanel detailsParent, Widget window, String tabTitle) {
		this.detailsParent = detailsParent;
		this.window        = window;
		
		butClose = new HTML("");
		butClose.setStyleName("sgwebui-detailsTab-close");
		butClose.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				close();
			}
		});
		butClose.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				butClose.addStyleDependentName("hover");
			}
		});
		
		butClose.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				butClose.removeStyleDependentName("hover");
			}
		});
		butClose.setTitle(Tooltips.INSTANCE.tab_close());
		
		pTab = new HorizontalPanel();
		pTab.setVerticalAlignment(ALIGN_MIDDLE);
		lbTitle = new Label(tabTitle);
		pTab.add(lbTitle);
		pTab.add(butClose);
		pTab.setStyleName("sgwebui-detailsTab");
		this.setTitle(tabTitle);
		
		initWidget(pTab);
	}

	@Override
	public void setTitle(String tabTitle) {
		lbTitle.setText(tabTitle);
	}
	
	@Override
	public String getTitle() {
		return lbTitle.getText();
	}
	
	public void close() {
		this.detailsParent.closeTab(window);
		onClose();
	}
	
	protected void onClose() { }
	
}