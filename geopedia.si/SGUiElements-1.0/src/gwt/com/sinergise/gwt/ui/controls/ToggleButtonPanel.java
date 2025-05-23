package com.sinergise.gwt.ui.controls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ToggleButtonPanel extends Composite implements HasWidgets, HasClickHandlers {
	
	public ToggleButtonPanel() {
        this(new VerticalPanel());
    }

    public ToggleButtonPanel(Panel panel){
        this.panel = panel;
        initWidget(panel);
    }

    @Override
    public void add(Widget w) {
        if(w instanceof ToggleButton){
            ToggleButton button = (ToggleButton) w;
            button.addClickHandler(handler);
        }
        panel.add(w);
    }

    @Override
    public void clear() {
        panel.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return panel.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return panel.remove(w);
    }

    @Override
    public void setWidth(String width) {
        panel.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        panel.setHeight(height);
    }

    private final Panel panel;
    private ClickHandler handler = new ClickHandler(){
        @Override
        public void onClick(ClickEvent event) {
            Iterator<Widget> itr = panel.iterator();
            while(itr.hasNext()){
                Widget w = itr.next();
                if(w instanceof ToggleButton){
                    ToggleButton button = (ToggleButton) w;
                    if(event.getSource().equals(button) && button.getValue().booleanValue()) {
                        button.setDown(true);
                    } else {
                    	button.setDown(false);
                    }
                }
            }

            for(ClickHandler h : handlers){
                h.onClick(event);
            }
        }
    };

    private List<ClickHandler> handlers = new ArrayList<ClickHandler>();
    @Override
    public HandlerRegistration addClickHandler(final ClickHandler hndlr) {
        handlers.add(hndlr);
        return new HandlerRegistration() {

            @Override
            public void removeHandler() {
                handlers.remove(hndlr);
            }
        };
    }
    
    public int getSelectedIndex(){
    	int res = -1;
    	Iterator<Widget> itr = panel.iterator();
    	while(itr.hasNext()){
    		Widget w = itr.next();
            if(w instanceof ToggleButton){
            	res++;
                ToggleButton button = (ToggleButton) w;
                if(button.getValue().booleanValue()) return res;
            }
        }
    	return -1;
    }
    public int setSelectedIndex(int index){
    	int cur = -1;
    	Iterator<Widget> itr = panel.iterator();
    	while(itr.hasNext()){
    		Widget w = itr.next();
            if(w instanceof ToggleButton){
            	cur++;
            	ToggleButton button = (ToggleButton) w;
            	button.setDown(cur==index);
            }
        }
    	return -1;
    }

}
