/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sinergise.gwt.ui.maingui.extwidgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.resources.client.CommonResources;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureChildVisibility;
import com.sinergise.common.ui.controls.SourcesVisibilityChangeEvents;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.gwtmod.TabLayoutPanel;
import com.sinergise.gwt.ui.resources.LayoutResources.TabLayoutCss;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.util.UtilGWT;
import com.sinergise.gwt.util.html.CSS;

/**
 * A panel that represents a tabbed set of pages, each of which contains another
 * widget. Its child widgets are shown as the user selects the various tabs
 * associated with them. The tabs can contain arbitrary text, HTML, or widgets.
 *
 * <p>
 * This widget will <em>only</em> work in standards mode, which requires that
 * the HTML page in which it is run have an explicit &lt;!DOCTYPE&gt;
 * declaration.
 * </p>
 *
 * <h3>CSS Style Rules</h3>
 * <dl>
 * <dt>.gwt-TabLayoutPanel
 * <dd>the panel itself
 * <dt>.gwt-TabLayoutPanel .gwt-TabLayoutPanelTabs
 * <dd>the tab bar element
 * <dt>.gwt-TabLayoutPanel .gwt-TabLayoutPanelTab
 * <dd>an individual tab
 * <dt>.gwt-TabLayoutPanel .gwt-TabLayoutPanelTabInner
 * <dd>an element nested in each tab (useful for styling)
 * <dt>.gwt-TabLayoutPanel .gwt-TabLayoutPanelContent
 * <dd>applied to all child content widgets
 * </dl>
 *
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.TabLayoutPanelExample}
 *
 * <h3>Use in UiBinder Templates</h3>
 * <p>
 * A TabLayoutPanel element in a {@link com.google.gwt.uibinder.client.UiBinder
 * UiBinder} template must have a <code>barHeight</code> attribute with a double
 * value, and may have a <code>barUnit</code> attribute with a
 * {@link com.google.gwt.dom.client.Style.Unit Style.Unit} value.
 * <code>barUnit</code> defaults to PX.
 * <p>
 * The children of a TabLayoutPanel element are laid out in &lt;g:tab>
 * elements. Each tab can have one widget child and one of two types of header
 * elements. A &lt;g:header> element can hold html, or a &lt;g:customHeader>
 * element can hold a widget. (Note that the tags of the header elements are
 * not capitalized. This is meant to signal that the head is not a runtime
 * object, and so cannot have a <code>ui:field</code> attribute.)
 * <p>
 * For example:<pre>
 * &lt;g:TabLayoutPanel barUnit='EM' barHeight='3'>
 *  &lt;g:tab>
 *    &lt;g:header size='7'>&lt;b>HTML&lt;/b> header&lt;/g:header>
 *    &lt;g:Label>able&lt;/g:Label>
 *  &lt;/g:tab>
 *  &lt;g:tab>
 *    &lt;g:customHeader size='7'>
 *      &lt;g:Label>Custom header&lt;/g:Label>
 *    &lt;/g:customHeader>
 *    &lt;g:Label>baker&lt;/g:Label>
 *  &lt;/g:tab>
 * &lt;/g:TabLayoutPanel>
 * </pre>
 */
/**
 * Copied from TabLayoutPanel because of:
 * 	-replacing tabBar with HeaderPanel for resizing header (before FlowPanel)
 * 	-adding span elements inside Tab for styling with ImageResources
 * 	-added function wrap() for wrapping elements (tabs fall into new row if not enough space, content is resized)
 */
public class SGTabLayoutPanel extends TabLayoutPanel implements
    CanEnsureChildVisibility, SourcesVisibilityChangeEvents {
	
	private static TabLayoutCss LAYOUTBUNDLE = Theme.getTheme().layoutBundle().tabLayout();
	static {
		LAYOUTBUNDLE.ensureInjected();
	}

  private class SGTab extends Tab {
    private Element right;
    private Element left;

    public SGTab(Widget child) {
      super(child);
      getElement().insertFirst(left = Document.get().createSpanElement());
      getElement().appendChild(right = Document.get().createSpanElement());

      setStyleName(LAYOUTBUNDLE.tabItem());
      inner.setClassName(LAYOUTBUNDLE.tabItemInner());
      left.setClassName(LAYOUTBUNDLE.left());
      right.setClassName(LAYOUTBUNDLE.right());

      left.addClassName(CommonResources.getInlineBlockStyle());
      right.addClassName(CommonResources.getInlineBlockStyle());
      getElement().addClassName(CommonResources.getInlineBlockStyle());
    }

    @Override
	public void setSelected(boolean selected) {
    	super.setSelected(selected);
    	if (selected) {
    		addStyleName(LAYOUTBUNDLE.selected());
    	} else {
    		removeStyleName(LAYOUTBUNDLE.selected());
    	}
    }
  }

  protected List<VisibilityChangeListener> visibilityListeners = new ArrayList<VisibilityChangeListener>();

  /**
   * Creates an empty tab panel.
   *
   */
  public SGTabLayoutPanel() {
	  this(false, 0);
  }
  public SGTabLayoutPanel(boolean isVertical, int width) {
	  super();
	  
	  if(isVertical) {
		  initTabContent(createVerticalPanel());
		  setStyleName(LAYOUTBUNDLE.sgVerticalTabLayoutPanel());
		  tabBar.getElement().setAttribute("style", "left:0; width: "+ width+"px; position: absolute; top: 0; bottom: 0;");
		  deckPanel.getElement().setAttribute("style", "left: "+ width+"px; position: absolute; top: 0; right: 0; bottom: 0;");
	  } else {
		  initTabContent(createHeaderPanel());
		  tabBar.setWidth("auto");
		  setStyleName(LAYOUTBUNDLE.sgTabLayoutPanel());
		  setHeight("100%");
	  }
	  contentStyle = LAYOUTBUNDLE.tabContent();
  }
  
  //Webkit BUG: tabBar musn't be too big (over spliter) or Chrome will scroll it
  @Override
  protected void initTabContent(Panel contentPanel) {
	  super.initTabContent(contentPanel);
	  CSS.width(tabBar, CSS.AUTO);
  }
  
  private SGHeaderPanel createHeaderPanel() {
    SGHeaderPanel panel = new SGHeaderPanel(tabBar, deckPanel);

    // Add the deck panel to the panel.
    deckPanel.addStyleName(LAYOUTBUNDLE.tabContainer());
    deckPanel.setHeight("100%");

    tabBar.setStyleName(LAYOUTBUNDLE.tabBar());
    return panel;
  }
  
  private SGFlowPanel createVerticalPanel() {
	    SGFlowPanel panel = new SGFlowPanel(tabBar, deckPanel);

	    // Add the deck panel to the panel.
	    deckPanel.addStyleName(LAYOUTBUNDLE.tabContainer());
	    deckPanel.setHeight("100%");

	    tabBar.setStyleName(LAYOUTBUNDLE.tabBar());
	    return panel;
	  }
  

  /**
   * Custom function that allows adding image resources to tabs 
   */
  public HasText add(Widget child, String text, ImageResource imgRes) {
	    SGImageLabel tabWidget = new SGImageLabel(text, new Image(imgRes));
		insert(child, tabWidget, getWidgetCount());
		return tabWidget;
  }

  @Override
  public void insert(Widget child, Widget tab, int beforeIndex) {
	  insert(child, new SGTab(tab), beforeIndex);
  }
  
  @Override
  public void selectTab(int index, boolean fireEvents) {
	  if (getSelectedWidget() != null) {
		  getSelectedWidget().setVisible(false);
	  }
	  super.selectTab(index, fireEvents);
	  getSelectedWidget().setVisible(true);
	  onResize();
  }

  public boolean isTabVisible(int index) {
	  return deckPanel.getWidget(index).isVisible();
	}

    public Widget getSelectedWidget() {
		if(getSelectedIndex() < 0) {
			return null;
		}
		return getWidget(getSelectedIndex());
	}
  
	public void closeTab(Widget w) {
		int i = getWidgetIndex(w);
		if (i >= 0) {
			int selected = getSelectedIndex();
			remove(w);
			if (selected != i) {
				return; //do not select other tab if not closing the selected tab
			}
			while (i >= getWidgetCount() && i>0) {
				i--;
			}
			while (i >= 0) {
				try {
					if (getWidget(i) != null) {
						selectTab(i);
						return;
					}
				} catch (Exception e) {
					// ignore the NPE if the tab is missing
				}
				i--;
			}
			selectNone();
		}
	}
	
	protected Tab getTab(int index) {
		return (Tab)getTabWidget(index).getParent();
	}
	
	public void selectNone() {
		if (getSelectedIndex() >=0) {
			// Fire the before selection event, giving the recipients a chance to
			// cancel the selection.
			BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(this, Integer.valueOf(-1));
			if ((event != null) && event.isCanceled()) {
				return;
			}
			getTab(getSelectedIndex()).setSelected(false);
		}
		deckPanel.showWidget(null);
		if (getSelectedIndex() >=0) {
			selectedIndex = -1;
			// Fire the selection event.
			SelectionEvent.fire(this, Integer.valueOf(-1));
		}
		onResize();
	}
	@Override
	public void setVisible(boolean visible) {
		boolean oldVisible = isVisible();
		super.setVisible(visible);
		if (oldVisible != visible) {
			notifyVisibilityChange(visible);
		}
	}
	
	/**
	   * Use this function to act tabPanel as main menu. 
	   */
	  public void setPrimary() {
		addStyleName(LAYOUTBUNDLE.mainMenu());
		tabBar.addStyleName(LAYOUTBUNDLE.mainMenu());
		deckPanel.addStyleName(LAYOUTBUNDLE.mainMenu());
	  }

	/**
	   * Use this function to act tabPanel as sub menu. 
	   */
	  public void setSubmenu() {
		addStyleName(LAYOUTBUNDLE.subMenu());
		tabBar.addStyleName(LAYOUTBUNDLE.subMenu());
		deckPanel.addStyleName(LAYOUTBUNDLE.subMenu());
	  }

	public void setTabVisibility(int i, boolean visible) {
		  if (!visible) {
			  tabBar.getWidget(i).addStyleName(LAYOUTBUNDLE.hidden());  
		  } else {
			  tabBar.getWidget(i).removeStyleName(LAYOUTBUNDLE.hidden());
		  }
		  
	  }
	
	public void addContentStyle(String styleName) {
		deckPanel.addStyleName(styleName);
	}
	
	public void addTabBarStyle(String styleName) {
		tabBar.addStyleName(styleName);
	}
	
	public void addTabBarTailWidget(Widget wgt) {
		tabBar.add(wgt);
	}
	public void addTabBarFirstWidget(Widget wgt) {
		tabBar.insert(wgt,0);
	}

	public void setTabVisible(int index, boolean visible) {
	  deckPanel.getWidget(index).setVisible(visible);
	}
	
	public void setTabVisible(Widget w, boolean visible) {
	  int index = getWidgetIndex(w);
	  tabBar.getWidget(index).setVisible(visible);
	  
	  if (visible) {
		  selectTab(index);
	  } else if(index > 0) { //select other tab
		  while (index-- > 0) {
			  if (isTabVisible(getWidget(index))) {
				  selectTab(index);
				  break;
			  }
		  }
	  } else if(index < getWidgetCount()) {
		  while (++index < getWidgetCount()) {
			  if (isTabVisible(getWidget(index))) {
				  selectTab(index);
				  break;
			  }
		  }
	  } else {
		  selectNone();
	  }
	}
	
	public boolean isTabVisible(Widget w) {
		  return isTabVisible(getWidgetIndex(w));
		}
	
	@Override
	protected void onUnload() {
		notifyVisibilityChange(false);
		super.onUnload();
	}

	protected void notifyVisibilityChange(boolean visible) {
		for (VisibilityChangeListener l : visibilityListeners) {
			l.visibilityChanged(visible);
		}
		
	}
	
	@Override
	public void addVisibilityChangeListener(VisibilityChangeListener listener) {
		visibilityListeners.add(listener);
	}
	
	@Override
	public void removeVisibilityChangeListener(VisibilityChangeListener listener) {
		visibilityListeners.remove(listener);
	}

	@Deprecated
	public void wrap() {
		  tabBar.getElement().getStyle().setWidth(100, Unit.PCT);
	}
	
	@Override
	public void ensureChildVisible(Object child) {
		Widget childW = (Widget)child;
		while (childW != null && getWidgetIndex(childW)<0) {
			childW = childW.getParent();
		}
		if (childW != null) {
			Widget selectedW = getSelectedWidget();
			if (selectedW != null && (selectedW != childW)) {
				//ensuring that the previously selected tab will be hidden first!
				selectedW.setVisible(false); 
			}
			setTabVisible(childW, true);
			selectTab(childW);
		}
	}
	
	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}
	
	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}
	
	@Override
	public boolean isChildVisible(Object child) {
		return UtilGWT.isOrHasDescendant(getSelectedWidget(), (Widget)child);
	}
	protected void setTabWidget(int index, Widget wgt) {
		((Tab)tabBar.getWidget(index)).setWidget(wgt);
	}
	
	public void addTabStyleName(Widget w, String style) {
		getTabWidget(w).getParent().addStyleName(style);
	}
}
