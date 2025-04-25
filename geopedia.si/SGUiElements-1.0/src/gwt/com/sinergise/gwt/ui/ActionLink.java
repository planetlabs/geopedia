package com.sinergise.gwt.ui;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ButtonBase;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.UIObjectInfo;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.util.html.ExtDOM;


public class ActionLink extends ButtonBase {
    Action myAction;
    public ActionLink(Action act) {
        super(DOM.createSpan());
        setStyleName("cosylab-ActionLink");
        myAction=act;
        addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
                if (isEnabled()) {
                    myAction.performAction();
                }
            }
        });
        myAction.addPropertyChangeListener(new PropertyChangeListener<Object>() {
            public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
                if (UIObjectInfo.ENABLED==propertyName) {
                    setEnabled(myAction.isEnabled());
                    
                } else if (UIObjectInfo.VISIBLE==propertyName) {
                    setVisible(myAction.isVisible());
                    
                } else if (UIObjectInfo.NAME==propertyName) {
                    setHTML(myAction.getName());
                    
                } else if (UIObjectInfo.DESC==propertyName) {
                    setTitle(myAction.getDescription());
                    
                }
            }
        });
        update();
    }

    private void update() {
        setHTML(myAction.getName());
        setTitle(myAction.getDescription());
        setVisible(myAction.isVisible());
        setEnabled(myAction.isEnabled());
    }
    
    @Override
	protected void onLoad() {
        update();
    }
    
    public void setDim(int w, int h) {
        ExtDOM.setSize(getElement(), w, h);
    }
    
    @Override
	public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK) {
            DOM.eventPreventDefault(event);
        }
        super.onBrowserEvent(event);
    }
    
    @Override
	public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            removeStyleName("cosylab-ActionLink-disabled");
        } else {
            addStyleName("cosylab-ActionLink-disabled");
        }
    }

    
    @Override
	public void setTitle(String title) {
        if (title==null) title="";
        super.setTitle(title);
    }
}
