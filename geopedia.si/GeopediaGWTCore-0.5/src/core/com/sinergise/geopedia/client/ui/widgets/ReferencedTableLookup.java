package com.sinergise.geopedia.client.ui.widgets;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.service.FeatureServiceAsync;
import com.sinergise.gwt.ui.ListBoxExt;

public class ReferencedTableLookup extends Composite{
	private static final String NULL_VALUE = "";
	private static final FeatureServiceAsync service = RemoteServices.getFeatureServiceInstance();
	
	private ListBoxExt listbox;
	private Long valueToSet = null;
	private boolean built = false;
	public ReferencedTableLookup (Field field) {
		listbox = new ListBoxExt(false);
		service.getForeignReferences(field.refdTableId, null, new AsyncCallback<ArrayList<ForeignReferenceProperty>>() {
			
			@Override
			public void onSuccess(ArrayList<ForeignReferenceProperty> result) {
				listbox.addItem("", NULL_VALUE);
				for (ForeignReferenceProperty vh:result) {
					listbox.addItem(vh.getReptext(), String.valueOf(vh.getValue()));
				}
				built = true;
				if (valueToSet!=null) {
					internalSetValue(valueToSet);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
		initWidget(listbox);
	}
	public void setEditorValue(Long long1) {
		if (built) {
			internalSetValue(long1);
		} else {
			valueToSet = long1;
		}
	}
	
	public Long getEditorValue() {
		String strVal = listbox.getValue();
		if (StringUtil.isNullOrEmpty(strVal))
			return null;
		else {
			try {
			return Long.parseLong(strVal);
			} catch (NumberFormatException ex) {
				return null;
			}
		}
	}
	private void internalSetValue(Long value) {
		if (value!=null)
			listbox.setValue(String.valueOf(value));
		else {
			listbox.setValue(NULL_VALUE);
		}
	}
}
