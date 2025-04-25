package com.sinergise.generics.gwt.widgetprocessors;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.editor.IntegerEditor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextArea;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;


public abstract class AbstractWidgetActionsProcessor extends WidgetProcessor {
	
	
	@Override
	public Widget bind(final Widget widget, final int idx, final GenericObjectProperty property,
			GenericWidget gw) {
		if (widget==null)
			return null;
		
		if (MetaAttributes.isType(property.getAttributes(), Types.STUB)) {
			return widget;
		}
		
		// ignore readonly widgets
		if (!MetaAttributes.isFalse(property.getAttributes().get(MetaAttributes.READONLY))) {
			return widget;
		}
		
		if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
			Widget wrappedWidget = ((NoticeableWidgetWrapper<Widget>)widget).getWrappedWidget();
			return bind(wrappedWidget, idx, property, gw);
		}
		
			 if (widget instanceof SGTextBox)		addValueChangeHandler((SGTextBox)widget, idx, property);
		else if (widget instanceof CheckBox) 		addValueChangeHandler((CheckBox)widget, idx, property);
		else if (widget instanceof SGTextArea) 		addValueChangeHandler((SGTextArea)widget, idx, property);
		else if (widget instanceof IntegerEditor) 	addValueChangeHandler((IntegerEditor)widget, idx, property);
		else if (widget instanceof DoubleEditor) 	addValueChangeHandler((DoubleEditor)widget, idx, property);
		else if (widget instanceof HasValueChangeHandlers<?>) addValueChangeHandler((HasValueChangeHandlers<?>)widget, idx, property);
		else if (widget instanceof Anchor) 			addValueChangeHandler((Anchor)widget, idx, property);
		else if (widget instanceof Label) {
			// ignore labels
		}else {
			System.out.println("Unable to bind actions to unsupported widget: "+widget.getClass().getName()+" Widget: '"+gw.getWidgetName()+"' Attribute='"+property.getName()+"'");
		}
		return widget;
	}
	
	protected void addValueChangeHandler(final SGTextBox widget, final int idx, final GenericObjectProperty property) {
		widget.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				widgetValueChanged(widget, idx, property.getName(), property.getAttributes());					
			}
		});
	}
	
	protected void addValueChangeHandler(final SGTextArea widget, final int idx, final GenericObjectProperty property) {
		widget.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				widgetValueChanged(widget, idx, property.getName(), property.getAttributes());					
			}
		});
	}
	
	protected void addValueChangeHandler(final CheckBox widget, final int idx, final GenericObjectProperty property) {
		widget.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				widgetValueChanged(widget, idx, property.getName(), property.getAttributes());			
				
			}
		});
	}
	
	protected void addValueChangeHandler(final IntegerEditor widget, final int idx, final GenericObjectProperty property) {
		widget.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				widgetValueChanged(widget, idx, property.getName(), property.getAttributes());					
			}
		});
	}
	
	protected void addValueChangeHandler(final DoubleEditor widget, final int idx, final GenericObjectProperty property) {
		widget.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				widgetValueChanged(widget, idx, property.getName(), property.getAttributes());					
			}
		});
	}
	
	protected void addValueChangeHandler(final HasValueChangeHandlers<?> widget, final int idx, final GenericObjectProperty property) {
		widget.addValueChangeHandler(new ValueChangeHandler() {

			@Override
			public void onValueChange(ValueChangeEvent event) {
				widgetValueChanged((Widget)widget, idx, property.getName(), property.getAttributes());	
			}
		});
	}
	
	protected void addValueChangeHandler(final Anchor widget, final int idx, final GenericObjectProperty property) {
		widget.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				widgetValueChanged(widget, idx,  property.getName(), property.getAttributes());
			}
		});
	}

	protected  void widgetValueChanged(Widget w, int idx,  String attributeName, Map<String,String> metaAttributes) {
		widgetValueChanged(w,attributeName,metaAttributes);	
	}
	protected abstract void widgetValueChanged(Widget w, String attributeName, Map<String,String> metaAttributes);
}
