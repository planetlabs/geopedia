package com.sinergise.geopedia.client.ui.panels;

import java.util.Stack;

import com.google.gwt.user.client.ui.FlowPanel;


public class StackableTabPanel extends ActivatableTabPanel {
	private Stack<ActivatableStackPanel> panelStack = new Stack<ActivatableStackPanel>();
	private ActivatableStackPanel activePanel = null;
	
	
	
	protected int stackCount() {
		return panelStack.size();
	}
	
	public boolean addActivatablePanel (ActivatableStackPanel panel) {
		if (activePanel!=null) {
			if (!removePanel(activePanel)) 
				return false;
			panelStack.add(activePanel);
		}
		activatePanel(panel);
		mainWrap.onResize();
		return true;
	}

	
	@Override
	public boolean canDeactivate() {
		if (activePanel==null)
			return true;
		return activePanel.canDeactivate();
	}
	
	@Override
	protected void internalActivate() {
		if (activePanel!=null) {
			activePanel.onActivate();
		}
	}

	
	@Override
	protected boolean internalDeactivate() {
		if (activePanel!=null) {
			if (activePanel.canDeactivate()) {
				activePanel.onDeactivate();
			} else {
				return false;
			}
		}
		return true;
	}
	
	private void activatePanel(ActivatableStackPanel panel) {
		panel.setContainer(this);
		activePanel = panel;
		addContent(panel);
		panel.onActivate();
	}
	
	
	private boolean removePanel(ActivatableStackPanel panel) {
		if (!panel.canDeactivate())
			return false;
		panel.onDeactivate();
		panel.removeFromParent();
		return true;
	}
	
	public boolean removeStackPanel(ActivatableStackPanel activatableStackPanel) {
		if (activatableStackPanel==null)
			return false;
		if (!removePanel(activatableStackPanel))
			return false;
		
		if (activatableStackPanel.equals(activePanel)) {
			activePanel=null;
			if (panelStack.isEmpty()) {
				return true;
			}
			activatePanel(panelStack.pop());
		} else {
			panelStack.remove(activatableStackPanel);
		}
		return true;
	}

	public FlowPanel getTitleHolder() {
		return tabTitleWrapper;
	}
	
}
