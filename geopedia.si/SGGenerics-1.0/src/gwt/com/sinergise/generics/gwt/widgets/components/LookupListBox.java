package com.sinergise.generics.gwt.widgets.components;

import static com.sinergise.generics.core.MetaAttributes.LOOKUP_CACHED;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;
import com.sinergise.generics.core.filter.FilterUtils;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.IsFilterable;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.services.GenericsGWTCache;
import com.sinergise.gwt.ui.ListBoxExt;


public class LookupListBox extends ListBoxExt implements IsCreationProvider, IsFilterable {
	public static final String EMPTY_VALUE = "";

	public static Map<String,String> buildListBoxAttributesMap(String lookupSrc, String lookupKeys, String lookupLabels, Boolean hasEmptyChoice) {
		HashMap<String,String> attribs = new HashMap<String,String>();
		attribs.put(MetaAttributes.LOOKUP_HAS_EMPTY_CHOICE, MetaAttributes.BOOLEAN_TRUE);
		attribs.put(MetaAttributes.LOOKUP, hasEmptyChoice.toString());
		attribs.put(MetaAttributes.LOOKUP_SOURCE, lookupSrc);
		attribs.put(MetaAttributes.LOOKUP_KEYS, lookupKeys);
		attribs.put(MetaAttributes.LOOKUP_LABELS, lookupLabels);		
		return attribs;
	}
	
	
	private ArrayList<CreationListener> creationListeners = new ArrayList<CreationListener>();

	protected String[] lookupLabels;
	String lookupKey;
	
	private HandlerManager hManager;
	String lastValue = null;
	private boolean isRemote = false;
	private String lookupSource = null;
	private boolean creationCompleted = false;
	boolean hasEmptyChoice = false;
	
	String valueToSet = null;
	private boolean lookupCached = false;
	private boolean distinctKeys = false;

	private DataFilter filter;
	
	String lookupLabelFormat = null;
	String lookupFilterString = null;
	EntityType lookupEntityType = null;
	
	public LookupListBox(Map<String,String> metaAttributes) {
		this(metaAttributes, new SimpleFilter());
	}
	
	public LookupListBox(Map<String,String> metaAttributes, DataFilter filter) {
		this.filter = filter;
		hManager = new HandlerManager(this);
		hasEmptyChoice = MetaAttributes.isTrue(metaAttributes, MetaAttributes.LOOKUP_HAS_EMPTY_CHOICE);
		
		if (hasEmptyChoice) addItem("",EMPTY_VALUE); // Warning IE BUG: if null is used "null" is returned (a String) not <code>null</code>

		lookupSource = metaAttributes.get(MetaAttributes.LOOKUP_SOURCE);
		String labels = metaAttributes.get(MetaAttributes.LOOKUP_LABELS);
		lookupKey = metaAttributes.get(MetaAttributes.LOOKUP_KEYS);
		lookupLabelFormat = metaAttributes.get(MetaAttributes.LOOKUP_LABELS_FORMAT);
		lookupFilterString = metaAttributes.get(MetaAttributes.LOOKUP_FILTER);		
		lookupCached = metaAttributes.containsKey(LOOKUP_CACHED) ? MetaAttributes.isTrue(metaAttributes, LOOKUP_CACHED) : true;
		distinctKeys = metaAttributes.containsKey(MetaAttributes.LOOKUP_DISTINCTKEYS) ? MetaAttributes.isTrue(metaAttributes, MetaAttributes.LOOKUP_DISTINCTKEYS) : false;
		String lookupETString = metaAttributes.get(MetaAttributes.LOOKUP_ENTITYTYPE);
		
		if (!StringUtil.isNullOrEmpty(lookupETString)) {
			lookupEntityType = GwtEntityTypesStorage.getInstance().getEntityType(lookupETString);
		}
		
		if (labels!=null) lookupLabels = labels.split(",");
		else lookupLabels = new String[0];

		if (lookupFilterString!=null) {
			filter = buildCompoundDataFilter();
		}
		
		if (lookupSource!=null && lookupSource.length()>0) {
			isRemote = true;
			
			initRemoteData(lookupSource, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
				// TODO: handle?	
					String now = DateTimeFormat.getFormat("HH:mm:ss.SSS").format(new Date()) + ": ";
					System.out.println(now + "ERROR: LookupListBox filter didnt obtain data from remote source: " + lookupSource);
				}

				@Override
				public void onSuccess(Void result) {
					
					//TODO: implement GWT logging client side
//					String now = DateTimeFormat.getFormat("HH:mm:ss.SSS").format(new Date()) + ": ";
//					System.out.println(now + "LookupListBox filter obtained data from remote source: " + lookupSource);
					
					creationCompleted();
				}
			});
		} else {
			if(lookupKey == null) throw new IllegalArgumentException(MetaAttributes.LOOKUP_KEYS+": '"+lookupKey + " is null");

			isRemote = false;
			String[] lookupKeys = lookupKey.split(",");
			if (lookupKeys.length!=lookupLabels.length) 
				throw new IllegalArgumentException(MetaAttributes.LOOKUP_KEYS+": '"+lookupKey+"' and "+MetaAttributes.LOOKUP_LABELS+": '"+labels+"' have different item count!");
			for (int i=0;i<lookupKeys.length;i++) {
				addItem(lookupLabels[i], lookupKeys[i]);
			}
			creationCompleted();
		}
		
		addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				if (!Util.safeEquals(getValue(),lastValue)) {
					lastValue = getValue();
					ValueChangeEvent.fire(LookupListBox.this, getValue());
				}
			}
		});
	}
	public void setFilter (DataFilter filter) {
		setFilter(filter, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
			// TODO: handle?	
			}

			@Override
			public void onSuccess(Void result) {
				// TODO: handle?
			}
		});

	}
	public void setFilter (DataFilter filter, AsyncCallback<Void> callback) {
		if (!isRemote)
			return;
		this.filter = filter;
		initRemoteData(lookupSource, callback);
	}
	
	
	private void initRemoteData(String datasourceID, final AsyncCallback<Void> callback) {
		NotificationHandler.instance().processingStart(this);
//		final String ds = datasourceID;
//		
//		String now = DateTimeFormat.getFormat("HH:mm:ss.SSS").format(new Date()) + ": ";
//		System.out.println(now + "LookupListBox filter starting query for remote data source: " + lookupSource ); 

		GenericsGWTCache.getCollectionValues(filter, datasourceID, -1, -1, new AsyncCallback<ArrayValueHolder>() {
			
			
			@Override
			public void onSuccess(ArrayValueHolder result) {
				NotificationHandler.instance().processingStop(LookupListBox.this);
				if (result==null) {
					callback.onSuccess(null);
					return;
				}
				String selectedValue = null;
				if (StringUtil.isNullOrEmpty(valueToSet) && getItemCount()>0)
					selectedValue = getValue();
				clear();
				if (hasEmptyChoice) {
					addItem("",EMPTY_VALUE); // Warning IE BUG: if null is used "null" is returned (a String) not <code>null</code>
				}
				
				EntityType et = result.getType();
				ArrayList<TypeAttribute> displayTAs = new ArrayList<TypeAttribute>();

				for (String eaName:lookupLabels) {
					TypeAttribute ta = et.getAttribute(eaName);
					if (ta != null) displayTAs.add(ta);
				}
				
				
//				String now = DateTimeFormat.getFormat("HH:mm:ss.SSS").format(new Date()) + ": ";
//				System.out.println(now + "LookupListBox: " + ds + " obtained starting filling the box.");
				for (ValueHolder val:result) {
					EntityObject eo = (EntityObject)val;
					String itemText="";
					if (lookupLabelFormat!=null) {
						itemText = new String(lookupLabelFormat);
						for (TypeAttribute ta:displayTAs) {
							itemText = itemText.replaceFirst("\\{\\}", eo.getPrimitiveValue(ta.getId()));
						}
					} else {
						for (TypeAttribute ta:displayTAs) {
							if (itemText.length()>0) itemText += " ";
							itemText+=eo.getPrimitiveValue(ta.getId());
						}
					}

					PrimitiveValue pv = EntityUtils.getPrimitiveValue(eo, lookupKey);
					
					if (!pv.isNull())
						addItem(itemText, pv.value);
				}
				
//				now = DateTimeFormat.getFormat("HH:mm:ss.SSS").format(new Date()) + ": ";
//				System.out.println(now + "LookupListBox: " + ds + " box filled.");
				
				if (selectedValue!=null) {
					LookupListBox.this.setValue(selectedValue);
				}
				callback.onSuccess(null);
				if (valueToSet != null && valueToSet.length() > 0) {
					setValue(valueToSet);
					ValueChangeEvent.fire(LookupListBox.this, valueToSet);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				NotificationHandler.instance().processingStop(LookupListBox.this);
				caught.printStackTrace();
			}
		}, lookupCached);
	}

	@Override
	public String getValue() {
		if (!creationCompleted && valueToSet != null) {
			return valueToSet;
		}
		if (getSelectedIndex()==-1)
			return null;
		String val = getValue(getSelectedIndex());
		if (EMPTY_VALUE.equals(val))
			return null;
		return val;
	}

	@Override
	public void setValue(String strValue) {
		try {
			if (!creationCompleted) {
				valueToSet = strValue;
				return;
			}

			for (int i = 0; i < getItemCount(); i++) {
				if (getValue(i).equals(strValue)) {
					setSelectedIndex(i);
					return;
				}
			}
			setSelectedIndex(0);
		} finally {
			lastValue = getValue();
		}
	}
	
	@Override
	public void setSelectedIndex(int index) {
		try {
			super.setSelectedIndex(index);
		} finally {
			lastValue = getValue();
		}
	}
	
	@Override
	public void setValue(String strValue, boolean fireEvents) {
		setValue(strValue);
	}
	
	void creationCompleted() {
		for (CreationListener l:creationListeners)
			l.creationCompleted(this);
		creationCompleted = true;
	}
	
	@Override
	public boolean isCreated() {
		return creationCompleted;
	}
	
	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (event instanceof ValueChangeEvent<?>)
			hManager.fireEvent(event);
		else 
			super.fireEvent(event);
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}

	@Override
	public void addCreationListener(CreationListener l) {
		creationListeners.add(l);		
	}
	
	

	private CompoundDataFilter buildCompoundDataFilter () {
		CompoundDataFilter cdf = new CompoundDataFilter();
		
		
		TypeAttribute keyTA = lookupEntityType.getAttribute(lookupKey);
		cdf.addSelectionAttribute(keyTA);
		OrderFilter orderFilter = null;
		for (String eaName:lookupLabels) {
			TypeAttribute ta = lookupEntityType.getAttribute(eaName);
			cdf.addSelectionAttribute(ta);
			if (orderFilter == null) {
				orderFilter = OrderFilter.createForAttribute(ta, lookupEntityType, OrderOption.ASC);
			}
		}
		
		cdf.setOrderFilter(orderFilter);
		if (distinctKeys) {
			cdf.setDistinctAttribute(lookupEntityType.getAttribute(lookupKey));
		}	
		
		return cdf;
	}
	
	@Override
	public void onStateChanged(EntityObject entityObject,
			AsyncCallback<Void> callback) {
		
		// if values of filter are cached and there is not filter string we do nothing
		if (StringUtil.isNullOrEmpty(lookupFilterString) && lookupCached) {
			callback.onSuccess(null);
			return;
		}
		
		// if no filter is give just reset the filter with old value
		if (lookupEntityType==null) {
			setFilter(filter, callback);	
			return;
		}
		
		
		EntityObject newFilterEO = GwtEntityTypesStorage.getInstance().createEntityObject(lookupEntityType);
		
		CompoundDataFilter cdf  = buildCompoundDataFilter();
		cdf.addDataFilter(FilterUtils.buildFilterFromExpression(lookupFilterString, newFilterEO, entityObject), DataFilter.NO_FILTER);
		
		if (!cdf.equals(filter)) {
			setFilter(cdf, callback);
		} else {
			callback.onSuccess(null);
		}
		
	}
	
}
