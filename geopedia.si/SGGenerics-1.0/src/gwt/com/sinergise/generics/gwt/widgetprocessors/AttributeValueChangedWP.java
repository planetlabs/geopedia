package com.sinergise.generics.gwt.widgetprocessors;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.editor.IntegerEditor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public abstract class AttributeValueChangedWP  extends WidgetProcessor{
	
	
	protected SimpleBindingWidgetProcessor widgetBindingWP;
	
	
	public AttributeValueChangedWP(SimpleBindingWidgetProcessor bindingWP) {
		this.widgetBindingWP = bindingWP;
	}
	@Override
	public Widget bind(final Widget widget, final int idx, final GenericObjectProperty property,
			final GenericWidget gw) {
		if (widget==null)
			return null;
		
		if (MetaAttributes.isType(property.getAttributes(), Types.STUB)) {
			return widget;
		}
		
		if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
			Widget wrappedWidget = ((NoticeableWidgetWrapper<Widget>)widget).getWrappedWidget();
			return bind(wrappedWidget, idx, property, gw);
		}
		
		if(widget instanceof SGTextBox){
			((SGTextBox)widget).addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					widgetValueChanged(widget, idx, property.getName(), property, gw);					
				}
			});
		} else if (widget instanceof CheckBox){
			((CheckBox)widget).addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					widgetValueChanged(widget, idx, property.getName(), property, gw);			
					
				}
			});
		} else if (widget instanceof IntegerEditor) {
			((IntegerEditor)widget).addValueChangeHandler(new ValueChangeHandler<String>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					widgetValueChanged(widget, idx, property.getName(), property, gw);					
				}
			});
		} else if (widget instanceof DoubleEditor) {
			((DoubleEditor)widget).addValueChangeHandler(new ValueChangeHandler<String>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					widgetValueChanged(widget, idx, property.getName(), property, gw);					
				}
			});
		} else if (widget instanceof HasValueChangeHandlers<?>) {
			((HasValueChangeHandlers<?>)widget).addValueChangeHandler(new ValueChangeHandler() {

				@Override
				public void onValueChange(ValueChangeEvent event) {
					widgetValueChanged(widget, idx, property.getName(), property, gw);	
					
				}
			});
		} else if (widget instanceof Anchor) {
			((Anchor)widget).addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					widgetValueChanged(widget, idx,  property.getName(), property, gw);
				}
			});
		}else if (widget instanceof Label) {
				// ignore labels
		}else {
			System.out.println("Unable to bind actions to unsupported widget: "+widget.getClass().getName()+" Widget: '"+gw.getWidgetName()+"' Attribute='"+property.getName()+"'");
		}
		return widget;
	}
	
	protected  void widgetValueChanged(Widget w, int idx,  String attributeName, GenericObjectProperty property, GenericWidget gw) {
		EntityObject currentEO = widgetBindingWP.getLastLoadedEntityObject();
		EntityObject widgetEO = GwtEntityTypesStorage.getInstance().createEntityObject(currentEO.getType());
		EntityUtils.cloneEntityObject(currentEO, widgetEO);
		widgetBindingWP.save(widgetEO);
		widgetValueChanged(w,attributeName,  idx, property, widgetEO,gw);
	}
	
	protected void loadEntityObject(EntityObject eo) {
		widgetBindingWP.load(eo);
	}
	
	protected abstract void widgetValueChanged(Widget w, String attributeName, int idx, GenericObjectProperty property, EntityObject widgetEO, GenericWidget gw);
	
	protected void enableAttribute(String attributeName, boolean enable) {
		widgetBindingWP.updateMetaAttribute(attributeName, MetaAttributes.DISABLED, Boolean.toString(enable));
	}

	
	protected SimpleBindingWidgetProcessor getBindingWidgetProcessor() {
		return widgetBindingWP;
	}

}
