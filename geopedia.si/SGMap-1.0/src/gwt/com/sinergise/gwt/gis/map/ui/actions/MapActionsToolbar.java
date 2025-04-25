/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.ui.Toolbar;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.util.UtilGWT;


public class MapActionsToolbar extends Toolbar {
	
	public static class OpenURLAction extends Action {
		public static final String OPENED_WINDOW_NAME = "openedWindowName";
		
		public OpenURLAction(String name, String url, ImageResource icon) {
			super(name);
			setIcon(icon);
			setActionURL(url);
		}
		
		public OpenURLAction(String name, String url, String icon) {
			super(name);
			setIconURL(icon);
			setActionURL(url);
		}
		@Override
		protected void actionPerformed() {
			Window.open(UtilGWT.getAbsoluteUrlFromHostPageBase(getActionURL()), getWindowName(), "");
		}
		
		public void setActionURL(String url) {
			setProperty(ACTION_URL, url);
		}
		
		public void setWindowName(String windowName) {
			setProperty(OPENED_WINDOW_NAME, windowName);
		}
		
		public void setTarget(String target) {
			setProperty(OPENED_WINDOW_NAME, target);
		}
		
		public String getWindowName() {
			String ret = (String)getProperty(OPENED_WINDOW_NAME);
			if (ret == null) return getName();
			return ret;
		}
	}
	
	public static class HelpAction extends OpenURLAction {
		public HelpAction() {
			this(null);
		}
		public HelpAction(String url) {
			this(url, "helpAction", Theme.getTheme().standardIcons().help());
		}

		public HelpAction(String url, String style, ImageResource icon) {
			super(Tooltips.INSTANCE.toolbar_help(), url, icon);
			setWindowName("help");
			setTarget("_blank");
			if (style != null) {
				setStyle(style);
			}
		}
	}
	
	public static class PrintAction extends OpenURLAction {
		
		/**
		 * Override getURL() to provide URL when it's needed
		 */
		public PrintAction() {
			this(null);
		}
		
		public PrintAction(String url) {
			super(Tooltips.INSTANCE.toolbar_print(), url, Theme.getTheme().standardIcons().print());
			setWindowName("print");
		}
	}
	
	public static final String CSS_CLASS_OUTER = "sgwebgis-mapToolbar";
	
    
    public MapActionsToolbar() {
    	super();
    	setStylePrimaryName(CSS_CLASS_OUTER);
    	//sink mouse events to toolbar to cancel bubbles
		sinkEvents(Event.MOUSEEVENTS);
    }
    
    public MapActionsToolbar(ToolbarType type) {
    	super(type);
    	setStylePrimaryName(CSS_CLASS_OUTER);
    	//sink mouse events to toolbar to cancel bubbles
		sinkEvents(Event.MOUSEEVENTS);
    }
    
	/** 
	 *  Prevent toolbar events from propagating to the map.
	 *  */
	@Override
    public void onBrowserEvent(Event event) {  
         int t = DOM.eventGetType(event);  
         if (Integer.bitCount(t & (Event.ONMOUSEDOWN | Event.ONMOUSEUP)) > 0) {  
              DOM.eventPreventDefault(event);  
              DOM.eventCancelBubble(event, true);  
         }  
         super.onBrowserEvent(event);  
    }   
}
