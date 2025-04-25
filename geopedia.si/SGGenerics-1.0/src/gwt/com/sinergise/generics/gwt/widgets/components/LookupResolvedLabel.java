package com.sinergise.generics.gwt.widgets.components;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.NotificationHandler;

public class LookupResolvedLabel extends Label{
	
	public static final String META_ATTRIBUTE_DISABLELOOKUPCACHING="disableLookupCaching";
	
	final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LookupResolvedLabel.class); 
	  
	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	Map<String, String> lookupMap = new HashMap<String, String>();
	String valueKey = null;

	private String lookupDSid = null;
	private boolean localCaching=true;
	
	private String lookupKey = null;
	private String lookupValue = null;
	private EntityType lookupEntityType = null;
	
	public LookupResolvedLabel(Map<String,String> metaAttributes) {
		lookupKey = metaAttributes.get(MetaAttributes.LOOKUP_KEYS);
		lookupValue = metaAttributes.get(MetaAttributes.LOOKUP_LABELS);
		String lookupETStr = metaAttributes.get(MetaAttributes.LOOKUP_ENTITYTYPE);
		if (lookupETStr!=null) {
			lookupEntityType = GwtEntityTypesStorage.getInstance().getEntityType(lookupETStr);
		}
		
		if (MetaAttributes.isTrue(metaAttributes, META_ATTRIBUTE_DISABLELOOKUPCACHING))
			localCaching=false;
			
		
		lookupDSid = metaAttributes.get(MetaAttributes.LOOKUP_SOURCE);
		if (lookupKey!=null && lookupValue!=null && lookupDSid==null){
			String [] keysArr = lookupKey.split(",");
			String [] valuesArr = lookupValue.split(",");
			if (keysArr.length!=valuesArr.length)
				throw new RuntimeException("Lookup keys and lookup labels do not contain same amount of elements!");			
			for (int i=0;i<keysArr.length;i++) {
				lookupMap.put(keysArr[i], valuesArr[i]);
			}
			
		}else if (localCaching && lookupDSid!=null) {
			
			genericsService.getCollectionValues(new SimpleFilter(), lookupDSid, -1, -1, new AsyncCallback<ArrayValueHolder>() {
				
				@Override
				public void onSuccess(ArrayValueHolder result) {
					if (result==null)
						return;
					EntityType et = result.getType();					
					TypeAttribute keyAttribute = et.getAttribute(lookupKey);
					TypeAttribute valueAttribute = et.getAttribute(lookupValue);
					if (keyAttribute==null) {
						logger.error("TypeAttribute '"+lookupKey+"' does not exist for EntityType '"+et.getName()+"'");
						throw new RuntimeException("KeyAttribute not found!");
					}
					if (valueAttribute==null) {
						logger.error("TypeAttribute '"+lookupValue+"' does not exist for EntityType '"+et.getName()+"'");
						throw new RuntimeException("ValueAttribute not found!");
					}
					
					for (ValueHolder val:result) {
						EntityObject eo = (EntityObject)val;
						String key = eo.getPrimitiveValue(keyAttribute.getId());
						String value = eo.getPrimitiveValue(valueAttribute.getId());
						if (key!=null && value!=null)
							lookupMap.put(key,value);
					}
					
					if (valueKey!=null)
						setText(valueKey);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					NotificationHandler.instance().handleException(caught);
				}
			});
		}
		
	}
	
	
	private void queryLookup() {
		if (lookupEntityType==null)
			throw new RuntimeException("You have to specify "+MetaAttributes.LOOKUP_ENTITYTYPE+" attribute for non-cached labels.");
		EntityObject filterObject = new AbstractEntityObject(lookupEntityType);
		EntityUtils.setStringValue(filterObject, lookupKey, valueKey);
		NotificationHandler.instance().processingStart(this);
		genericsService.getCollectionValues(new SimpleFilter(filterObject), lookupDSid, -1, -1, new AsyncCallback<ArrayValueHolder>() {

			@Override
			public void onFailure(Throwable caught) {
				NotificationHandler.instance().processingStop(LookupResolvedLabel.this);
				setLabelText("Lookup failed!");
			}

			@Override
			public void onSuccess(ArrayValueHolder result) {
				NotificationHandler.instance().processingStop(LookupResolvedLabel.this);
				if (result==null || result.size()!=1) {
					setLabelText("Lookup failed!");
				} else {
					EntityObject eo = (EntityObject) result.get(0);
					setLabelText(EntityUtils.getStringValue(eo, lookupValue));
				}
			}
			
		});
	}
	
	private void setLabelText(String text) {
		super.setText(text);
	}
	@Override
	public String getText() {
		return valueKey;
	}
	
	@Override
	public void setText(String text) {
		valueKey=text;
		if (text == null) {
			super.setText(text);
		} else {
			if (localCaching && lookupMap.size()>0) {
				String label = lookupMap.get(text);
				if (label!=null) {
					setLabelText(label);
				}else {
					setLabelText("Lookup failed!");
					logger.error("Lookup for key:'"+text+"' failed!");
				}
			} else if (!localCaching){
				queryLookup();
			}
		}
	}
}
