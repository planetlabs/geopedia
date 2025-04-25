package com.sinergise.generics.gwt.widgets.components;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.generics.gwt.widgets.MasonWidget;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;

public class SinglePageWizard extends AbstractWizard{

	private FlowPanel basePanel;
	public SinglePageWizard() {
		basePanel = new FlowPanel();
		basePanel.addStyleName("singlePageWizard");
		basePanel.addStyleName("wizard");
		initWidget(basePanel);
		
	}
	@Override
	public void create() {

		fixPanelOrder();
		
		if (firstPanel ==null)
			return;
		
		addPanel(firstPanel);
		WizardPanel nextPanel = firstPanel;
		for (int i=0;i<wizardsMap.size();i++) {
			String nextPanelName = nextPanel.getNextPanelName();
			if (nextPanelName==null)
				break;
			nextPanel = wizardsMap.get(nextPanelName);
			if (nextPanel==null)
				break;
			addPanel(nextPanel);
		}
		
	}
	
	
	private void addPanel(final WizardPanel p) {
		FlowPanel actionsPanel = p.getActionsPanel();
		if (p.getNextPanelName()!=null) {
			Anchor next = new Anchor(WidgetConstants.widgetConstants.masonWidgetWizardButtonNext());
			next.setStyleName("next");
			next.addStyleDependentName(p.getName());
			next.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					wizardActionPerformed(p.getName(),WIZARD_ACTION_PART_COMPLETED);
				}
			});
			actionsPanel.clear();
			actionsPanel.add(next);
		}
		basePanel.add(p);
	}
	
	private Map<String,WizardPanel> getWizardPanelMap() {
		return wizardsMap;
	}
	
	public static void showContent(MasonWidget mw, String[] names, boolean show) {		
		if (names==null)
			  return;
		if (mw==null || mw.getWizard()==null)
			return;
		AbstractWizard aWizard = mw.getWizard();
		if (!(aWizard instanceof SinglePageWizard))
			return;
		SinglePageWizard wizard = (SinglePageWizard)aWizard;
		
		  for (String n:names) {
			  WizardPanel p = wizard.getWizardPanelMap().get(n);
			  if (p==null) continue;
			  p.hideContent(!show);
		  }
	  }
	
}
