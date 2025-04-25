package com.sinergise.geopedia.light.client.ui.table;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.ui.panels.ActivatableStackPanel;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;

public class SimpleTableInfoTab extends AbstractTableTab {

	TableInfo infoPanel;
	protected SGHeaderPanel tableInfoContainer;
	protected SGFlowPanel tableFooterBtnPanel;
	
	public SimpleTableInfoTab() {
		super();
		infoPanel = new TableInfo(tabTitleWrapper);
		addActivatablePanel(infoPanel);
	}
	private class TableInfo  extends ActivatableStackPanel{
		HTML descriptionPanel;
		public TableInfo(SGFlowPanel tabTitleWrapper) {
			tableInfoContainer = new SGHeaderPanel();
			descriptionPanel = new HTML();
			tableFooterBtnPanel = new SGFlowPanel("tableFooterBtns");
			descriptionPanel.setStyleName("layerDescription");
			tableInfoContainer.setContentWidget(descriptionPanel);
			tableInfoContainer.setFooterWidget(tableFooterBtnPanel);
			add(tableInfoContainer);
			setHeight("100%");
		}

		public void setTable(Table table) {
			descriptionPanel.setHTML(table.descDisplayableHtml);
			setTabTitle(table.getName());
		}
	}
	

	protected void internalSetTable(Table table) {
		infoPanel.setTable(table);
	}

	public void setFooterBtns(Widget w) {
		tableFooterBtnPanel.add(w);
	}
	public void removeFooterBtn(Widget w) {
		tableFooterBtnPanel.remove(w);
	}
}
