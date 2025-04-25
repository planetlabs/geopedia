/*
 *
 */
package com.sinergise.gwt.ui;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;


public class Toolbar extends Composite {
	
	public enum ToolbarType{TOOLBAR_VERTICAL, TOOLBAR_HORIZONTAL}
	
	public enum ToolbarPosition{
		TOP_LEFT("top-left"), TOP_RIGHT("top-right"), LEFT_TOP("left-top"), 
		LEFT_BOTTOM("left-bottom"), RIGHT_TOP("right-top"), RIGHT_BOTTOM("right-bottom");
		
		final String styleDepName;
		ToolbarPosition(String styleDepName) {
			this.styleDepName = styleDepName;
		}
		
	}
	
	public static final String STYLE_TOOLBAR_WIDGET = StyleConsts.TOOLBAR_WIDGET;
	private final CellPanel widgetsPanel;
    private HashMap<Action, Widget> widgetsForActions=new HashMap<Action, Widget>();
    
    private CellPanel outer = null;
    
    public Toolbar() {
    	this(ToolbarType.TOOLBAR_HORIZONTAL);
    }
    
    public Toolbar(ToolbarType type) {
        if(type.equals(ToolbarType.TOOLBAR_VERTICAL)) {
        	widgetsPanel = new ToolbarVerticalPanel();
        	((VerticalPanel)widgetsPanel).setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        } else { //default is horizontal panel
        	widgetsPanel = new ToolbarHorizontalPanel();
        	((HorizontalPanel)widgetsPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        }
        widgetsPanel.setStylePrimaryName(StyleConsts.TOOLBAR);
        widgetsPanel.setSpacing(0);
        widgetsPanel.setBorderWidth(0);
        
        SimplePanel first = new SimplePanel();
        first.setStyleName(StyleConsts.TOOLBAR_FIRST);
        SimplePanel last = new SimplePanel();
        last.setStyleName(StyleConsts.TOOLBAR_LAST);
        
        if(type.equals(ToolbarType.TOOLBAR_VERTICAL)) {
        	outer = new VerticalPanel();
        } else { //default is horizontal panel
        	outer = new HorizontalPanel();
        }
        outer.add(first);
        outer.add(widgetsPanel);
        outer.add(last);
        
        initWidget(outer);
    }
    
    public void add(Widget w) {
    	widgetsPanel.add(w);
    	w.addStyleName(STYLE_TOOLBAR_WIDGET);
    }
    
    protected void add(Widget child, Element container) {
    	((ToolbarPanel)widgetsPanel).add(child, container);
    	child.addStyleName(STYLE_TOOLBAR_WIDGET);
    }
    
    public void addAction(Action act) {
        Widget aW=createActionButton(act);
        widgetsForActions.put(act, aW);
        add(aW);
    }
    
    public void setActionVisible(Action act, boolean visible) {
        widgetsForActions.get(act).setVisible(visible);
    }
    
    public void removeAction(Action act) {
        Widget w=widgetsForActions.remove(act);
        if (w!=null) {
            w.removeFromParent();
        }
    }
    
    private static Widget createActionButton(final Action act) {
        if (act.hasIcon()) {
        	return ActionUtilGWT.createActionButton(act);
        }
		return  new ActionLink(act);
    }
    
    public void removeAllActions() {
    	for(Iterator<Widget> widgets = widgetsForActions.values().iterator(); widgets.hasNext();) {
    		Widget w= widgets.next();
            if (w!=null) {
                w.removeFromParent();
            }
    	}
    	widgetsForActions.clear();
    }
    
    /**
     * Specifies toolbar positions for toolbar styling purposes. 
     * It does not position the toolbar however! 
     */
    public void setToolbarPosition(ToolbarPosition pos) {
		for(int i=0; i<outer.getWidgetCount(); i++) {
			Widget w = outer.getWidget(i);
			for(ToolbarPosition p : ToolbarPosition.values()) {
				w.removeStyleDependentName(p.styleDepName);
			}
			w.addStyleDependentName(pos.styleDepName);
		}
    }
    
    
    private interface ToolbarPanel {
    	public void add(Widget child, Element container);
    }
    
    private class ToolbarHorizontalPanel extends HorizontalPanel implements ToolbarPanel {
    	/**
    	 * just expose as public
    	 */
    	@Override
    	public void add(Widget child, Element container) {
    		super.add(child, container);
    	}
    }
    
    private class ToolbarVerticalPanel extends VerticalPanel implements ToolbarPanel {
    	/**
    	 * just expose as public
    	 */
    	@Override
    	public void add(Widget child, Element container) {
    		super.add(child, container);
    	}
    }
    
}
