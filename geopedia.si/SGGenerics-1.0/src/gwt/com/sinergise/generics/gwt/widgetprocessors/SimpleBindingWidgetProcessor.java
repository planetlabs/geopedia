package com.sinergise.generics.gwt.widgetprocessors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.Util;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityObject.Status;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.IsFilterable;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgetbuilders.MutableWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.helpers.DefaultValueProvider;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;

public class SimpleBindingWidgetProcessor extends WidgetProcessor{
	
	private final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(SimpleBindingWidgetProcessor.class); 
	
	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	private DefaultValueProvider defaultValueProvider = null;
	private String missingRequiredAttributeStyle = "missingRequired";
	
	public class BoundAttribute {
		public BoundAttribute(GenericObjectProperty prop, Widget w) {
			this.w =w;
			this.prop=prop;
		}
		GenericObjectProperty prop;
		Widget w;
	}
	protected Map<String,BoundAttribute> boundAttributes = 
		new HashMap<String,BoundAttribute>();
	protected MutableWidgetBuilder widgetBuilder;
	
	
	public SimpleBindingWidgetProcessor (MutableWidgetBuilder wb) {
		widgetBuilder = wb;
	}
	
	public void setDefaultValueProvider(DefaultValueProvider dvp){
		defaultValueProvider = dvp;
	}
	
	
	public BoundAttribute getBoundAttribute(String name) {
		return boundAttributes.get(name);
	}
	
	public Object getWidgetValue(BoundAttribute ba) {
		return widgetBuilder.getWidgetValue(ba.w, ba.prop.getName(), ba.prop.getAttributes());
		
	}

	@Override
	public Widget bind(Widget widget, int idx, GenericObjectProperty property, GenericWidget gw) {
		
		// don't bind to stubs!
		if (MetaAttributes.isType(property.getAttributes(), Types.STUB)) {
			logger.trace("Skipping STUB {}.",property.getName());
			return widget;
		}

		boundAttributes.put(property.getName(),new BoundAttribute(property, widget));
		
		return widget;
	}
	
	@Override
	public boolean unBind(int idx, GenericObjectProperty property) {
		
		boundAttributes.remove(property.getName());
		if (boundAttributes.size()==0)
			return true;
		return false;
	}
	
	public Widget getWidgetForAttribute(String attributeName) {
		BoundAttribute ba = boundAttributes.get(attributeName);
		if (ba==null)
			return null;
		if (ba.w instanceof NoticeableWidgetWrapper<?>) {
			return ((NoticeableWidgetWrapper<Widget>)ba.w).getWrappedWidget();
		}
		return ba.w;
	}

	
	public void updateMissingAttributesStyle(ArrayList<String> missingAttributes) {
		if (missingRequiredAttributeStyle==null)
			return;
		for (String attribName:missingAttributes) {
			BoundAttribute ba=boundAttributes.get(attribName);
			if (ba!=null) {
				ba.w.addStyleName(missingRequiredAttributeStyle);
			}
		}
	}
	
	/**
	 * @param missingAttributes
	 * @return false if there are empty required fields
	 */
	public boolean checkRequiredAtributes(ArrayList<String> missingAttributes) {
		boolean isRequiredEmpty = false;
		for (String attribName:boundAttributes.keySet()) {
			BoundAttribute ba=boundAttributes.get(attribName);
			if (ba.prop.isAction())
				continue;
			if (MetaAttributes.isType(ba.prop.getAttributes(), Types.BOOLEAN))
				continue;

			String value = (String) widgetBuilder.getWidgetValue(ba.w, ba.prop.getName(), ba.prop.getAttributes());
			if (missingRequiredAttributeStyle!=null) {
				ba.w.removeStyleName(missingRequiredAttributeStyle);
			}
			if (value==null || value.length()==0) {
				if (MetaAttributes.isTrue(ba.prop.getAttributes(), MetaAttributes.REQUIRED)) {
					if (missingAttributes!=null)
						missingAttributes.add(attribName);
					isRequiredEmpty = true;
				}	
			}
		}
		
		return !isRequiredEmpty;
	}
	
	
	public void updateMetaAttributeForAll(String metaAttribute, String value) {
		for (BoundAttribute ba: boundAttributes.values()) {
			widgetBuilder.updateWidgetMetaAttribute(ba.w, ba.prop.getAttributes(), metaAttribute,value);
		}
	}
	
	public void updateMetaAttribute(String attributeName, String metaAttribute, String value) {
		BoundAttribute ba = boundAttributes.get(attributeName);
		if (ba==null) {
			logger.error("updateMetaAttribute failed: Unable to find attribute '"+attributeName+"'");
			return;
		}
		widgetBuilder.updateWidgetMetaAttribute(ba.w, ba.prop.getAttributes(), metaAttribute,value);
	}
	
	
	public void setMetaAttributeEnabled(String attributeName, boolean enabled) {
		updateMetaAttribute(attributeName, MetaAttributes.DISABLED, Boolean.toString(!enabled));
	}
	
	
	public boolean haveValuesChanged(ValueHolder holder) {
		EntityObject eo = (EntityObject)holder;
		if (eo==null) return true;
		
		EntityType et = eo.getType();
		for (String attribName:boundAttributes.keySet()) {
			BoundAttribute ba=boundAttributes.get(attribName);
			if (ba.prop.isAction())
				continue;

			TypeAttribute attribute = et.getAttribute(attribName);
			if (attribute == null) {
				logger.error("Save error! Unable to find attribute '"+attribName+"' in EntityType "+et.getName()+" ID:"+et.getId());
				continue;
			}
			int attrId = attribute.getId();
			Object widgetValue = widgetBuilder.getWidgetValue(ba.w, ba.prop.getName(), ba.prop.getAttributes());
			String pValue = eo.getPrimitiveValue(attrId);
			if (attribute.getPrimitiveType()==Types.STRING) {
				String strWgtValue = (String)widgetValue;
				if (pValue==null && (strWgtValue==null || strWgtValue.length()==0))
					continue;
			}
			if (!Util.safeEquals(widgetValue, eo.getPrimitiveValue(attrId)))
					return true;			
		}
		return false;
	}
	public void save(ValueHolder genericValue) {
		save(genericValue, false);
	}
	
	public void save(ValueHolder genericValue, boolean saveDefaults) {
		EntityObject eo = (EntityObject)genericValue;
		if (eo==null) return;
		EntityType et = eo.getType();
		if (eo.getStatus()==Status.STORED)
			eo.setStatus(Status.UPDATED);
		for (String attribName:boundAttributes.keySet()) {
			BoundAttribute ba=boundAttributes.get(attribName);
			if (ba.prop.isAction())
				continue;

			TypeAttribute attribute = et.getAttribute(attribName);
			if (attribute == null) {
				logger.error("Save error! Unable to find attribute '"+attribName+"' in EntityType "+et.getName()+" ID:"+et.getId());
				continue;
			}
			int attrId = attribute.getId();
			Object value = widgetBuilder.getWidgetValue(ba.w, ba.prop.getName(), ba.prop.getAttributes());
			if (saveDefaults && defaultValueProvider!=null) {
				if (value==null) {
					value = defaultValueProvider.getDefaultValue(ba.prop);
				}					
			}
			eo.setPrimitiveValue(attrId, (String)value);
		}
	
	}
	
	// TODO: move to external type
	public void loadByAttribute(EntityType entityType, final TypeAttribute attribute, final String value, final String datasourceId) {
		EntityObject filterObject = new AbstractEntityObject(entityType.getId());
		filterObject.setPrimitiveValue(attribute.getId(), value);
		
		genericsService.getCollectionValues(new SimpleFilter(filterObject), datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.error(SimpleBindingWidgetProcessor.class.getName()+": Failed to load EntityObject by Attribute: "+attribute.getName()+"='"+value+"' from datasource "+datasourceId);
				NotificationHandler.instance().handleException(caught);
			}

			@Override
			public void onSuccess(ArrayValueHolder result) {
				if (result == null || result.size()==0) { // TODO handle "missing key"!?
					System.out.println("empty");
					return;
				}
				load(result.get(0));
			}
		});
	}
	
	public void load(ValueHolder genericValue) {
		load(genericValue,false);
	}

	private EntityObject lastLoadedEO = null;
	public EntityObject getLastLoadedEntityObject() {
		return lastLoadedEO;
	}

	public void load(ValueHolder genericValue, ArrayList<String>attributesToLoad, boolean loadDefaults) {
		EntityObject eo = (EntityObject)genericValue;
		if (eo==null) return;
		EntityType et = eo.getType();
		lastLoadedEO = new GwtEntityTypesStorage().createEntityObject(et);
		EntityUtils.cloneEntityObject(eo, lastLoadedEO);
		loadAttribute(eo, et, attributesToLoad.iterator(), loadDefaults);
	}
	
	
	private void loadAttribute(final EntityObject eo, final EntityType et, final Iterator<String> attributeNameIt, final boolean loadDefaults) {
		if (!attributeNameIt.hasNext())
			return;
		String attribName = attributeNameIt.next();
		final BoundAttribute ba=boundAttributes.get(attribName);
		if (!ba.prop.isAction()) {
			TypeAttribute attribute = et.getAttribute(attribName);
			if (attribute != null) {
				int attrId = attribute.getId();
				
				Object attValue = eo.getPrimitiveValue(attrId);
				if (loadDefaults && defaultValueProvider!=null) {
					if (attValue==null) {
						attValue = defaultValueProvider.getDefaultValue(ba.prop);
					}
						
				}
				final Object value = attValue;
				if (ba.w instanceof IsFilterable) {
					((IsFilterable)ba.w).onStateChanged(eo, new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							//TODO: report error?
							loadAttribute(eo, et, attributeNameIt, loadDefaults);
						}

						@Override
						public void onSuccess(Void result) {
							widgetBuilder.setWidgetValue(ba.w, ba.prop.getName(), ba.prop.getAttributes(),  value);
							loadAttribute(eo, et, attributeNameIt, loadDefaults);
						}
					});
					return;
				} else {
					widgetBuilder.setWidgetValue(ba.w, ba.prop.getName(), ba.prop.getAttributes(),  value);
				}
			} else {
				logger.error("Error while loading EntityObject! Unable to find attribute '"+attribName+"'");
			}
		}
		loadAttribute(eo, et, attributeNameIt, loadDefaults);
	}
	
	
	public void load(ValueHolder genericValue, boolean loadDefaults) {
		ArrayList<BoundAttribute> boundAttrList = new ArrayList<BoundAttribute>(boundAttributes.values());
		java.util.Collections.sort(boundAttrList, new Comparator<BoundAttribute>() {

			@Override
			public int compare(BoundAttribute o1, BoundAttribute o2) {
				if (o1.prop.getPosition() < o2.prop.getPosition())
					return -1;
				else if (o1.prop.getPosition() > o2.prop.getPosition())
					return 1;
				return 0;
			}

		});
		ArrayList<String> attrNames = new ArrayList<String>();
		for (BoundAttribute ba:boundAttrList) {
			attrNames.add(ba.prop.getName());
		}
		
		load(genericValue, attrNames ,loadDefaults);
	}
	

}
