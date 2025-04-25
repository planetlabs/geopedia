package com.sinergise.generics.gwt.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.gwt.widgetbuilders.WidgetBuilder;
import com.sinergise.generics.gwt.widgets.MasonWidget;
import com.sinergise.generics.gwt.widgets.table.GenercsTable;

public abstract class GenericWidget extends Composite implements IsCreationProvider{
	

	public static final String TYPE_MASONWIDGET = "MasonWidget";
	public static final String TYPE_GENERICTABLE= "GenericTable";
	
	private ArrayList<IWidgetProcessor> widgetProcessors = new ArrayList<IWidgetProcessor>();
	private ArrayList<CreationListener> creationListeners = new ArrayList<CreationListener>();
	protected WidgetBuilder widgetBuilder = null;
	private EntityType widgetEntityType = null;
	private boolean widgetIsCreated = false;
	protected Map<String,String> widgetMetaAttributes;
	protected Map<String,GenericObjectProperty> propertyMap = new HashMap<String, GenericObjectProperty>();

	
	
	public GenericObjectProperty getWidgetProperty(String name) {
		return propertyMap.get(name);
	}
	
	
	public static GenericWidget createWidgetForName (String widgetName) {
		if (TYPE_MASONWIDGET.equals(widgetName)) {
			return new MasonWidget();
		} else if (TYPE_GENERICTABLE.equals(widgetName)) {
			return new GenercsTable();
		}
		return null;
	}
	
	protected void setEntityType (EntityType entityType) {
		widgetEntityType = entityType;
	}
	public EntityType getEntityType() {
		if (!isCreated())
			throw new RuntimeException("Widget must be created before EntityType is available");
		return widgetEntityType;
	}
	public void setWidgetBuilder (WidgetBuilder widgetBuilder) {
		this.widgetBuilder = widgetBuilder;
	}
	public WidgetBuilder getWidgetBuilder () {
		return widgetBuilder;
	}
	
	@Override
	public void addCreationListener(CreationListener l) {
		creationListeners.add(l);
	}
	
	protected void widgetCreated() {
		widgetIsCreated = true;
		bindProcessors();
		for (CreationListener l:creationListeners) {
			l.creationCompleted(this);
		}
	}
	public void addWidgetProcessor (IWidgetProcessor wp) {
		widgetProcessors.add(wp);
	}
	
	
	protected Widget bindProcessors(Widget widget, int idx, GenericObjectProperty property, GenericWidget gw) {
		for (IWidgetProcessor wp:widgetProcessors) {
			widget=wp.bind(widget, idx, property, gw);
			if (widget==null) return null; // stop processing if any of the widget processors return null
		}
		return widget;
	}
	
	protected void unbindProcessors(int idx, GenericObjectProperty property ) {
		for (IWidgetProcessor wp:widgetProcessors) {
			wp.unBind(idx, property);
		}
	}
	
	public abstract void build(List<GenericObjectProperty> properties, EntityType entityType);
	
	/**
	 * Override if widget supports widget mason
	 * @param properties
	 * @param tableMetaAttr
	 * @param entityType
	 * @param masonElement
	 */
	public void build(List<GenericObjectProperty> properties, EntityType entityType, Element masonElement) {
		build(properties,entityType);
	}
	protected abstract void bindProcessors();

	
	@Override
	public boolean isCreated(){
		return widgetIsCreated;
	}
	
	
	public Map<String,GenericObjectProperty> getGenericObjectPropertyMap() {
		throw new RuntimeException("Method not supported for this widget type");
	}
	
	public Map<String,Widget> getWidgetMap() {
		throw new RuntimeException("Method not supported for this widget type");	
	}

	public void destroy() {
		creationListeners.clear();
		widgetBuilder = null;
		widgetProcessors.clear();
	}
	public void setWidgetMetaAttributes(Map<String,String> attributes) {
		this.widgetMetaAttributes=attributes;
	}
	
	public Map<String,String> getWidgetMetaAttributes() {
		return widgetMetaAttributes;
	}
	
	public String getWidgetName() {
		return widgetMetaAttributes.get(MetaAttributes.NAME);
	}
}
