package com.sinergise.gwt.ui.maingui.extwidgets;

import static com.sinergise.common.util.lang.TypeUtil.boxB;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureChildVisibility;
import com.sinergise.common.ui.controls.SourcesVisibilityChangeEvents;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.gwtmod.TabPanel;
import com.sinergise.gwt.util.UtilGWT;


public class SGTabPanel extends TabPanel implements CanEnsureChildVisibility, SourcesVisibilityChangeEvents {
	
	public static final String DECORATED_STYLENAME = "gwt-DecoratedTabPanel";
	public static final String DECORATED_TABBAR_STYLE = "gwt-DecoratedTabBar";
	
	public static final String[] H_TAB_ROW_STYLES = {"tabTop", "tabMiddle"};
	public static final String[] H_TAB_COL_SUFFIXES = {"Left", "Center", "Right"};

	public static final String[] V_TAB_ROW_STYLES = {"tabTop", "tabMiddle", "tabBottom"};
	public static final String[] V_TAB_COL_SUFFIXES = {"Left", "Center"};

	public static final String DISABLED_TABITEM_STYLENAME = "gwt-DisabledTabPanel";
	public static final String ERRONEOUS_TABITEM_STYLENAME = "gwt-ErroneousTabPanel";
	
	protected List<VisibilityChangeListener> visibilityListeners = new ArrayList<VisibilityChangeListener>();
	
	public static class SGUnmodfTabBar extends UnmodifiableTabBar {
		public SGUnmodfTabBar(boolean horizontal, boolean flow) {
			super(flow ? new FlowPanel() : horizontal ? new HorizontalPanel():new VerticalPanel());
			if (!flow && !horizontal) {
		    	((VerticalPanel)panel).setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		    	((VerticalPanel)panel).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			}
		}
		
		@Override
		protected void styleFirstRest(ComplexPanel container, HTML first, HTML rest) {
			if (container instanceof VerticalPanel) {
			    first.setWidth("100%");
			    rest.setWidth("100%");
			    ((VerticalPanel)container).setCellWidth(first, "100%");
//			    ((VerticalPanel)panel).setCellHeight(rest, "100%");
			} else {
				super.styleFirstRest(container, first, rest);
			}
		}
		
		@Override
		public boolean selectTab(int index) {
			if(!isTabVisible(index)) {
				setTabVisible(index, true);
			}
			return super.selectTab(index);
		}
		
		public void setTabVisible(int index, boolean visible) {
		  //take into account the first-left widget
		  panel.getWidget(index+1).setVisible(visible);
		}
	  
		public boolean isTabVisible(int index) {
		//take into account the first-left widget
		  return panel.getWidget(index+1).isVisible();
		}
		
		public void setTabWidget(int index, Widget wgt) {
			assert (index >= 0) && (index < getTabCount()) : "Tab index out of bounds";

			ClickDelegatePanel delPanel = (ClickDelegatePanel)panel.getWidget(index + 1);
			SimplePanel focusablePanel = delPanel.getFocusablePanel();
			focusablePanel.setWidget(wgt);
		}

		public Widget getTabWidget(int index) {
			assert (index >= 0) && (index < getTabCount()) : "Tab index out of bounds";

			ClickDelegatePanel delPanel = (ClickDelegatePanel)panel.getWidget(index + 1);
			SimplePanel focusablePanel = delPanel.getFocusablePanel();
			return focusablePanel.getWidget();
		}
	}
	
	public static final Logger logger = LoggerFactory.getLogger(SGTabPanel.class);
	
	
	public final boolean decorated;
	public final boolean horizontal;

	public String[] decorator_tabRowStyles;
	public String[] decorator_tabColSuffixes;
	public int 		decorator_contentIndex = 1;
	public boolean 	decorator_createInner = true;
	public String   decorator_styleName = "gwt-DecoratorPanel";
	
	public SGTabPanel() {
		this(true, true);
	}
	
	@Override
	public void onSelection(SelectionEvent<Integer> event) {
		logger.trace("On selection ({}).", event.getSelectedItem());
		try {
			Widget oldWidget = getSelectedWidget();
			if (oldWidget != null) {
				if (oldWidget instanceof CanBeNotifiedWhenShown) {
					((CanBeNotifiedWhenShown) oldWidget).onHiddenByAncestor(this);
				}
				oldWidget.setVisible(false);
			}
			
			super.onSelection(event);
			
			Widget newWidget = getWidget(event.getSelectedItem().intValue());
			if (newWidget != null) {
				newWidget.setVisible(true);
				if (newWidget instanceof CanBeNotifiedWhenShown) {
					((CanBeNotifiedWhenShown) newWidget).onShownByAncestor(this);
				}
			}
		} catch (IndexOutOfBoundsException ignore) {} 
	}
	
	public SGTabPanel(boolean decorated, boolean horizontal) {
		this(decorated, horizontal, false);
	}
	public SGTabPanel(boolean decorated, boolean horizontal, boolean flow) {
		super(horizontal?new VerticalPanel():new HorizontalPanel(), new SGUnmodfTabBar(horizontal, flow));
		this.decorated = decorated;
		this.horizontal = horizontal;
		if(!horizontal) {
			addStyleName("verticalTabPanel");
		}

		if (decorated) {
			setStylePrimaryName(DECORATED_STYLENAME);
			getTabBar().setStylePrimaryName(DECORATED_TABBAR_STYLE);
			if(flow) {
				addStyleDependentName("flow");
				getTabBar().addStyleDependentName("flow");
			}
			
			if (horizontal) {
				decorator_tabRowStyles = H_TAB_ROW_STYLES;
				decorator_tabColSuffixes = H_TAB_COL_SUFFIXES;
			} else {
				decorator_tabRowStyles = V_TAB_ROW_STYLES;
				decorator_tabColSuffixes = V_TAB_COL_SUFFIXES;
			}
		}
	}
	
	@Override
	protected SimplePanel createTabTextWrapper() {
		if (decorated) {
			SGDecoratorPanel ret = new SGDecoratorPanel(decorator_tabRowStyles, decorator_tabColSuffixes, decorator_contentIndex, true, false);
			ret.setStyleName(decorator_styleName);
			return ret;
		}
		return super.createTabTextWrapper();
	}
	
	public void closeTab(Widget w) {
		int i = getWidgetIndex(w);
		if (i>= 0) {
			int selected = getTabBar().getSelectedTab();
			remove(w);
			if(selected != i) {
				return; //do not select other tab if not closing the selected tab
			}
			while (i >= getWidgetCount() && i>0) i--;
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
	
	public void selectTab(Widget w) {
		int i = getWidgetIndex(w);
		if (i >= 0) {
			selectTab(i);
		}
	}
	
	@Override
	public void ensureVisible() {
		logger.trace("Ensure visible (was {}).", boxB(this.isVisible()));
		EnsureVisibilityUtil.ensureVisibility(this);
	}
	
	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}
	
	@Override
	public void ensureChildVisible(Object child) {
		Widget childW = (Widget)child;
		while (childW != null && getWidgetIndex(childW) < 0) {
			childW = childW.getParent();
		}
		if (childW != null) {
			setTabVisible(childW, true);
			selectTab(childW);
		}
	}
	
	@Override
	public boolean isChildVisible(Object child) {
		return UtilGWT.isOrHasDescendant(getSelectedWidget(), (Widget)child);
	}
	
	@Override
	public SGUnmodfTabBar getTabBar() {
		return (SGUnmodfTabBar)super.getTabBar();
	}
	
	public Widget getSelectedWidget() {
		if(getTabBar().getSelectedTab() < 0) {
			return null;
		}
		return getWidget(getTabBar().getSelectedTab());
	}
	
	public void setTabVisible(Widget w, boolean visible, boolean changeAlsoTabFocus) {
		  int index = getWidgetIndex(w);
		  getTabBar().setTabVisible(index, visible);
		  
		  if (!changeAlsoTabFocus)
		      return;
		  
		  if (visible) {
			  selectTab(index);
		  } else if (index-1 > 0) { //select other tab
			  while (index-- > 0) {
				  if (isTabVisible(getWidget(index))) {
					  selectTab(index);
					  break;
				  }
			  }
		  } else if (index+1 < getWidgetCount()) {
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
	
	public void setTabVisible(Widget w, boolean visible) {
		setTabVisible(w, visible, true);
	}
	  
	protected void selectNone() {
		selectTab(-1, true);
	}

	public boolean isTabVisible(Widget w) {
	  return getTabBar().isTabVisible(getWidgetIndex(w));
	}
	
	@Override
	public void setStyleName(String style) {
		super.setStyleName(style);
		String[] styles = style.split(" ");
		for (int i = 0; i < styles.length; i++) {
			styles[i] = styles[i].trim()+"-tabBar";
		}
		getTabBar().setStyleName(StringUtil.arrayToString(styles, " "));
	}
	
	@Override
	public void setStylePrimaryName(String style) {
		getTabBar().removeStyleName(getStylePrimaryName()+"-tabBar");
		super.setStylePrimaryName(style);
		getTabBar().addStyleName(style+"-tabBar");
	}

	@Override
	public void addStyleName(String style) {
		super.addStyleName(style);
		getTabBar().addStyleName(style+"-tabBar");
	}
	
	@Override
	public void removeStyleName(String style) {
		super.removeStyleName(style);
		getTabBar().removeStyleName(style+"-tabBar");
	}
	
	/**
	 * Turns highlight on/off when showing/hiding the panel. 
	 */
	@Override
	public void setVisible(boolean visible) {
		boolean oldVisible = isVisible();
		super.setVisible(visible);
		if (oldVisible != visible) {
			notifyVisibilityChange(visible);
		}
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
}
