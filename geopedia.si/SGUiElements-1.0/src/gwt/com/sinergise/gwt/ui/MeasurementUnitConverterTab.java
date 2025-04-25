package com.sinergise.gwt.ui;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.ui.i18n.Labels;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGCloseableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTitledPanel;
import com.sinergise.gwt.ui.resources.Theme;

/**
 * @author tcerovski
 *
 */
public class MeasurementUnitConverterTab extends SGFlowPanel {
	
	private final SGTabLayoutPanel tabPanel;
	private final MeasurementUnitConverter wConverter;
	
	public MeasurementUnitConverterTab(SGTabLayoutPanel tabPanel, MeasurementUnitConverter wConverter) {
		this.tabPanel = tabPanel;
		this.wConverter = wConverter;
		
		init();
	}
	
	private void init() {
		SGTitledPanel titledPanel = new SGTitledPanel(Labels.INSTANCE.unitConverter(), 
				new Image(Theme.getTheme().standardIcons().unitConverter()));
		
		titledPanel.setWidget(wConverter);
		titledPanel.setHeight("");
		
		SimplePanel outer = new SimplePanel();
		outer.setWidget(titledPanel);
        add(outer);
	}
	
	public void showControl() {
		if(tabPanel.getWidgetIndex(this) < 0) {
			tabPanel.add(this, new SGCloseableTab(tabPanel, this, Labels.INSTANCE.unitConverter_short()));
		}
		ensureVisible();
	}
	
	public void hideControl() {
		tabPanel.closeTab(this);
	}

	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}
	
	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}

}
