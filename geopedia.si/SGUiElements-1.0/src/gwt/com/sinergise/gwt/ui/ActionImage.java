package com.sinergise.gwt.ui;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.UIObjectInfo;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.util.html.CSS;


public class ActionImage extends Widget implements HasClickHandlers {
	public static final String ICON_SIZE="actionImageIconSize"; 
	protected Action myAction;
	
	public ActionImage(Action act) {
		super();
		myAction = act;
		setElement(ImageUtilGWT.createPNGElement());
		sinkEvents(Event.ONCLICK);
	    DOM.setEventListener(getElement(), this);
	    CSS.cursor(getElement(), CSS.CURSOR_HAND);
		
        myAction.addPropertyChangeListener(new PropertyChangeListener<Object>() {
            public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
                if (UIObjectInfo.ENABLED.equals(propertyName)) {
                    setEnabled(myAction.isEnabled());
                    
                } else if (UIObjectInfo.VISIBLE.equals(propertyName)) {
                    setVisible(myAction.isVisible());
                    
                } else if (UIObjectInfo.NAME.equals(propertyName)) {
                    setTitle(myAction.getName());
                    
                } else if (UIObjectInfo.ICON_16.equals(propertyName)) {
                    setIcon(GWT.getModuleBaseURL()+myAction.getIcon());
                    
                } else if (UIObjectInfo.DISABLED_ICON_16.equals(propertyName) && !isEnabled()) {
                    setIcon(GWT.getModuleBaseURL()+myAction.getIcon());
                }
                //TODO: support resource icons
            }
        });
		update();
		setStyleName("cosylab-ActionImage");
	}
	
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}
	
	boolean en=true;
	protected void setEnabled(boolean en) {
		if (this.en!=en) {
			this.en=en;
			setIcon(GWT.getModuleBaseURL()+(en?myAction.getIcon():myAction.getDisabledIcon()));
			myAction.setExternalEnabled(false);
		}
	}
	
	public boolean isEnabled() {
		return en;
	}
	
	@Override
	public void onBrowserEvent(Event event)
	{
		switch (DOM.eventGetType(event)) {
        case Event.ONCLICK:
        	if (myAction!=null) myAction.performAction();
	        break;
        default:
	        break;
        }
		super.onBrowserEvent(event);
	}
	
	protected void setFocus(boolean b) {
		setFocus(getElement(),b);
	} 
	protected static native void setFocus(Element el, boolean b) /*-{
		if (b) {
			el.focus();
		} else {
			el.blur();
		}	
    }-*/;

	protected void update() {
        setIcon(GWT.getModuleBaseURL()+(isEnabled()?myAction.getIcon():myAction.getDisabledIcon()));
        setTitle(myAction.getName());
        setVisible(myAction.isVisible());
        setEnabled(myAction.isEnabled());
    }

	protected String lastSrc=null;

	protected void setIcon(String imgpath) {
		if (lastSrc==imgpath || (lastSrc!=null && lastSrc.equals(imgpath))) {
			return;
		}
		lastSrc=imgpath;
		if (imgpath==null) {
			ImageUtilGWT.setSource(getElement(), GWT.getModuleBaseURL()+"trPix.gif");
        	return;
        }
        boolean isPng=imgpath.toLowerCase().endsWith("png");
        if (isPng) {
            Integer icSize=(Integer)myAction.getProperty(ICON_SIZE);
            if (icSize!=null) {
            	ImageUtilGWT.setSource(getElement(), imgpath, true, false);
            	ImageUtilGWT.setSize(getElement(), icSize.intValue(), icSize.intValue());
            	return;
            }
        }
       	ImageUtilGWT.setSource(getElement(), imgpath, isPng, true);
	}
	
	public Action getAction() {
		return myAction;
	}

}
