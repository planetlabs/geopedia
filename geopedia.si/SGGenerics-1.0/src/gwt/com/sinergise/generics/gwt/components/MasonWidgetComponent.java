package com.sinergise.generics.gwt.components;

import java.util.HashMap;

import com.google.gwt.user.client.ui.Composite;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.RemoteInspector;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgetbuilders.FormWidgetBuilder;
import com.sinergise.generics.gwt.widgetbuilders.GenericWidgetFactory;
import com.sinergise.generics.gwt.widgetprocessors.SimpleBindingWidgetProcessor;
import com.sinergise.generics.gwt.widgets.MasonWidget;

public class MasonWidgetComponent extends Composite{
	private MasonWidget mWidget;
	private SimpleBindingWidgetProcessor  bindProcessor;
	private String widgetName;
	
	public MasonWidgetComponent (String widgetName) {
		this(widgetName, new FormWidgetBuilder());
	}
	
	public MasonWidgetComponent(String widgetName, FormWidgetBuilder builder) {
		this(widgetName, builder, new MasonWidget());
	}
	public MasonWidgetComponent(String widgetName, FormWidgetBuilder builder, MasonWidget masonWidget) {
		this.widgetName = widgetName;
		mWidget = masonWidget;

		bindProcessor = new SimpleBindingWidgetProcessor(builder);
		mWidget.addWidgetProcessor(bindProcessor);
		mWidget.setWidgetBuilder(builder);
		initWidget(mWidget);
	}

	public void build(){
		GenericWidgetFactory.buildWidget(
				mWidget,
				new RemoteInspector(widgetName),
				new HashMap<String,String>());
	}
	
	public MasonWidget getMasonWidget() {
		return mWidget;
	}

	public SimpleBindingWidgetProcessor getBindingProcessor() {
		return bindProcessor;
	}
	
	public void addWidgetProcessor(WidgetProcessor processor) {
		mWidget.addWidgetProcessor(processor);
	}


	public void addCreationListener(CreationListener listener) {
		mWidget.addCreationListener(listener);
	}
	
	public EntityType getEntityType() {
		return mWidget.getEntityType();
	}

	public void saveToValueHolder(ValueHolder valueHolder) {
		getBindingProcessor().save(valueHolder);
	}

	public void loadFromValueHolder(ValueHolder valueHolder, boolean loadDefaults) {
		getBindingProcessor().load(valueHolder,loadDefaults);		
	}
	
	/**
	 * Toggles fields MetaAttributes.DISABLE attribute. 
	 * Only for non MetaAttributes.READONLY, MetaAttributes.IGNORE, MetaAttributes.HIDDEN fields
	 * @param enable
	 */
	public void enableFields(boolean enable) {
		for (GenericObjectProperty gop:getMasonWidget().getGenericObjectPropertyMap().values()) {
			if (MetaAttributes.isTrue(gop.getAttributes(),MetaAttributes.READONLY) ||
					MetaAttributes.isTrue(gop.getAttributes(),MetaAttributes.IGNORE) ||
					MetaAttributes.isTrue(gop.getAttributes(),MetaAttributes.HIDDEN))
				continue;
			bindProcessor.updateMetaAttribute(gop.getName(), MetaAttributes.DISABLED, Boolean.toString(!enable));			
		}
		
	}
	
	public boolean isCreated() {
		return mWidget.isCreated();
	}

}
