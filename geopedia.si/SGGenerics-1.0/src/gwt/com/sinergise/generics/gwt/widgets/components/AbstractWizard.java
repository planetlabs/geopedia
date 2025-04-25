package com.sinergise.generics.gwt.widgets.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.sinergise.generics.gwt.widgets.MasonWidget;
import com.sinergise.generics.gwt.widgets.NamedObjectActionListener;

public abstract class AbstractWizard extends Composite{
	
	public static final Integer WIZARD_ACTION_PART_COMPLETED = Integer.valueOf(1);
	
	protected HashMap<String, WizardPanel> wizardsMap = new HashMap<String,WizardPanel>();
	protected List<NamedObjectActionListener> wizardActionListeners = new ArrayList<NamedObjectActionListener>();
	
	protected WizardPanel firstPanel = null;
	
	protected WizardPanel lastPanel = null;
	public void addWizardPanel(WizardPanel panel) {
		if (panel==null)
			return;
		wizardsMap.put(panel.getName(),panel);
		if (panel.getNextPanelName()==null)
			lastPanel = panel;
	}
	
	protected void fixPanelOrder() {
		for (WizardPanel p:wizardsMap.values()) {
			if (p.getNextPanelName()!=null) {
				WizardPanel np=wizardsMap.get(p.getNextPanelName());
				if (np!=null && np.getPreviousPanelName()==null) {
						np.setPreviousPanelName(p.getName());
				}
			}
		}
		
		for (WizardPanel p:wizardsMap.values()) {
			if (p.getPreviousPanelName()==null) {
				firstPanel = p;
				return;
			}
		}
	}


	public void addWizardActionListener(NamedObjectActionListener listener) {
		wizardActionListeners.add(listener);
	}


	public static void addWizardActionListener(MasonWidget mWidget,
			NamedObjectActionListener listener) {
		if (mWidget==null || mWidget.getWizard() == null) return;
		mWidget.getWizard().addWizardActionListener(listener);
	}
	
	protected void wizardActionPerformed(String wizardName,Object action) {
		for (NamedObjectActionListener nol:wizardActionListeners) {
			nol.actionPerformed(wizardName, action);
		}
	}
	
	public abstract void create();

}
