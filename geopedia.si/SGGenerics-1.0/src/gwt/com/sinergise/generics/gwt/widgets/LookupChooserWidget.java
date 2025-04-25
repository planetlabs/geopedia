package com.sinergise.generics.gwt.widgets;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;

public class LookupChooserWidget extends FlowPanel{

	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	private Label label;
	
	private String value = null;
	private String lookupSource = null;
	private TypeAttribute lookupLabel = null;
	private TypeAttribute lookupKey = null;
	private EntityType lookupType;
	
	
	
	protected LookupChooserWidget() {
		label = new Label("<undef>");
		add(label);
	}
	public LookupChooserWidget(Map<String,String> metaAttributes) {
		this();
		String lookupEntityType = metaAttributes.get("lookupEntityType");
		lookupType = GwtEntityTypesStorage.getInstance().getEntityType(lookupEntityType);
		if (lookupType == null)
			throw new IllegalArgumentException("Unable to find entityType "+lookupEntityType);
		
		lookupSource = metaAttributes.get(MetaAttributes.LOOKUP_SOURCE);
		String lookupLabelStr = metaAttributes.get(MetaAttributes.LOOKUP_LABELS);
		lookupLabel = lookupType.getAttribute(lookupLabelStr);
		String lookupKeyStr = metaAttributes.get(MetaAttributes.LOOKUP_KEYS);
		lookupKey = lookupType.getAttribute( lookupKeyStr );
	}
	
	
	
	private void updateLookup(final String newValue) {
		
		
		EntityObject filterObject = new AbstractEntityObject(lookupType.getId());
		filterObject.setPrimitiveValue(lookupKey.getId(), newValue);
		genericsService.getCollectionValues(new SimpleFilter(filterObject), lookupSource, -1, -1, new AsyncCallback<ArrayValueHolder>() {

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(ArrayValueHolder result) {
				if (result == null || result.size()==0) { // TODO handle "missing key"!?
					System.out.println("empty");
					return;
				}
				
				EntityObject resultEO = (EntityObject) result.get(0);
				label.setText(resultEO.getPrimitiveValue(lookupLabel.getId()));
				value=newValue;
			}
		});
	}
	
	public void setValue(String strValue) {
		updateLookup(strValue);
	}
	
	public String getValue() {
		return value;
	}
}
