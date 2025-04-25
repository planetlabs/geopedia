package com.sinergise.gwt.ui.maingui.extwidgets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

@SuppressWarnings("serial")
public class SGRadioButtonGroup<T> implements Serializable, HasValueChangeHandlers<T> {
	
	private final ValueChangeHandler<Boolean> handler = new ValueChangeHandler<Boolean>() {
		@SuppressWarnings("unchecked")
		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			selected = (SGRadioButton<T>) event.getSource();//this fires only when the radioButton is selected
		}
	};
	
	private final String name;
	private final List<SGRadioButton<T>> rbs = new ArrayList<SGRadioButton<T>>();
	
	private SGRadioButton<T> selected;

	private HandlerManager handlerManager;
	
	public SGRadioButtonGroup(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public SGRadioButton<T> getSelected(){
		return selected;
	}
	
	public T getSelectedValue(){
		if(selected == null){
			return null;
		}
		return selected.getInherentValue();
	}
	
	public void setSelectedValue(T value){
		for(SGRadioButton<T> rb : rbs){
			boolean eq = rb.getInherentValue().equals(value);
			if(eq){
				selected = rb;
			}
			rb.setValue(Boolean.valueOf(eq));
		}
	}
	
	public List<SGRadioButton<T>> getRadioButtons(){
		return rbs;
	}
	
	public void add(SGRadioButton<T> rb){
		rb.setName(name);
		rb.addValueChangeHandler(handler);
		rbs.add(rb);
		if(Boolean.TRUE.equals(rb.getValue())){
			selected = rb;
		}
	}

	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	@SuppressWarnings("hiding")
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		if(handlerManager == null){
			handlerManager = new HandlerManager(this);
		}
		return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
	}
	
	
}