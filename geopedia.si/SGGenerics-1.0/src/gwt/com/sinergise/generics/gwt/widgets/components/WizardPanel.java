package com.sinergise.generics.gwt.widgets.components;

import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.MetaAttributes;

public class WizardPanel extends FlowPanel{
	
	public static final String META_NEXT_PANEL="nextPanel";
	public static final String META_PREVIOUS_PANEL="previousPanel";
	public static final String STYLE_CORE ="WizardPanel";
	public static final String STYLE_TITLE="title";
	public static final String STYLE_CONTENT=STYLE_CORE+"-content";
	
	private SimplePanel labelPanel = null;
	private FlowPanel contentPanel = null;
	private FlowPanel actionsPanel = null;
	private String name = null;
	
	private String previousPanelName = null;
	private String nextPanelName=null;
	
	private ArrayList<String> wizardAttributeList;
	public WizardPanel() {
		init();
	}
	
	public WizardPanel(Map<String,String> wizardAttributes) {
		String label = MetaAttributes.readStringAttr(wizardAttributes, MetaAttributes.LABEL, null);
		this.name =MetaAttributes.readRequiredStringAttribute(wizardAttributes, MetaAttributes.NAME);

		nextPanelName = MetaAttributes.readStringAttr(wizardAttributes, META_NEXT_PANEL, null);
		previousPanelName = MetaAttributes.readStringAttr(wizardAttributes, META_PREVIOUS_PANEL, null);
		
		if (label!=null && label.length()>0) {
			labelPanel = new SimplePanel();
			labelPanel.getElement().setInnerHTML(label);
			labelPanel.setStyleName(STYLE_TITLE);
			super.add(labelPanel);
		}
		init();
	}
	
	
	private void init() {
		contentPanel = new FlowPanel();
		actionsPanel = new FlowPanel();
		actionsPanel.setStyleName("actionsPanel");
		contentPanel.setStyleName(STYLE_CONTENT);
		super.add(contentPanel);
		super.add(actionsPanel);
	}

	public void setAttributeList(ArrayList<String> list) {
		wizardAttributeList = list;
	}
	public ArrayList<String> getAttributeList() {
		return wizardAttributeList;
	}
	public FlowPanel getActionsPanel() {
		return actionsPanel;
	}
	
	public void hideContent(boolean hide) {
		if (hide) {
			contentPanel.addStyleDependentName("hidden");
		} else {
			contentPanel.removeStyleDependentName("hidden");
		}
	}
	
	  @Override
	  public void add(Widget w) {
		  contentPanel.add(w);
	  }

	  @Override
	  public void clear() {
		  contentPanel.clear();
	  }

	  @Override
	  public void insert(Widget w, int beforeIndex) {
		  contentPanel.insert(w, beforeIndex);
	  }
	  
	  @Override
	  public boolean remove(Widget w) {
		  return contentPanel.remove(w);
	  }
	  @Override
	  public boolean remove(int index) {
		  return contentPanel.remove(index);
	  }
	  
	  
	  public String getName() {
		  return name;
	  }
	  
	  public String getNextPanelName() {
		  return nextPanelName;
	  }
	  
	  public String getPreviousPanelName() {
		  return previousPanelName;
	  }

	public void setPreviousPanelName(String name) {
		previousPanelName = name;
	}
	public void setNextPanelname(String name) {
		nextPanelName = name;
	}
	
}
