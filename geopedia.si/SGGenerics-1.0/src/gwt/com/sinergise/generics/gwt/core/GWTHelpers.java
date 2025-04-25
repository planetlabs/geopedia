package com.sinergise.generics.gwt.core;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.event.ActionPerformedListener;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;

public class GWTHelpers {

	public static void loadFilteredData (String datasourceId, CompoundDataFilter cdf, final ActionPerformedListener<ArrayValueHolder> listener) {
			GenericsServiceAsync genericsService = GWT.create(GenericsService.class);		
			NotificationHandler.instance().processingStart();
			genericsService.getCollectionValues(cdf, datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {

				@Override
				public void onFailure(Throwable caught) {
					NotificationHandler.instance().processingStop();
					NotificationHandler.instance().handleException(caught);
				}

				@Override
				public void onSuccess(ArrayValueHolder result) {
					NotificationHandler.instance().processingStop();
					listener.onActionPerformed(result);
				}
			});
	}

	public static void loadDataByAttributeValue (EntityType entityType, TypeAttribute attribute, String value,
			String datasourceId,CompoundDataFilter cdf,  final ActionPerformedListener<ArrayValueHolder> listener) {
		EntityObject filterObject = new AbstractEntityObject(entityType.getId());
		filterObject.setPrimitiveValue(attribute.getId(), value);
		GenericsServiceAsync genericsService = GWT.create(GenericsService.class);		
		NotificationHandler.instance().processingStart();
		DataFilter dataFilter = null;
		if (cdf!=null) {
			cdf.addDataFilter(new SimpleFilter(filterObject), DataFilter.OPERATOR_AND);
			dataFilter = cdf;
		} else {
			dataFilter = new SimpleFilter(filterObject);
		}
		genericsService.getCollectionValues(dataFilter, datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {
			
			@Override
			public void onFailure(Throwable caught) {
				NotificationHandler.instance().processingStop();
				NotificationHandler.instance().handleException(caught);
			}
			
			@Override
			public void onSuccess(ArrayValueHolder result) {
				NotificationHandler.instance().processingStop();
				listener.onActionPerformed(result);
			}
		});
	}
	
	public static void loadDataByAttributeValue (EntityType entityType, TypeAttribute attribute, String value,
			String datasourceId, final ActionPerformedListener<ArrayValueHolder> listener) {
			EntityObject filterObject = new AbstractEntityObject(entityType.getId());
			filterObject.setPrimitiveValue(attribute.getId(), value);
			GenericsServiceAsync genericsService = GWT.create(GenericsService.class);		
			NotificationHandler.instance().processingStart();
			genericsService.getCollectionValues(new SimpleFilter(filterObject), datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {

				
				@Override
				public void onFailure(Throwable caught) {
					NotificationHandler.instance().processingStop();
					NotificationHandler.instance().handleException(caught);
				}

				
				@Override
				public void onSuccess(ArrayValueHolder result) {
					NotificationHandler.instance().processingStop();
					listener.onActionPerformed(result);
				}
			});
	}
	
	
	public static void loadOrderedDataByAttributeValue (EntityType entityType, TypeAttribute attribute, TypeAttribute orderingAttribute, String value,
			String datasourceId, final ActionPerformedListener<ArrayValueHolder> listener) {
		
		 	
			EntityObject filterObject = new AbstractEntityObject(entityType.getId());
			
			CompoundDataFilter cdf = new CompoundDataFilter();
			cdf.addDataFilter(new SimpleFilter(filterObject),DataFilter.NO_FILTER);		
			OrderFilter orderFilter = OrderFilter.createForAttribute(orderingAttribute,entityType, OrderOption.ASC);
		
			cdf.setOrderFilter(orderFilter);
			
			filterObject.setPrimitiveValue(attribute.getId(), value);
			GenericsServiceAsync genericsService = GWT.create(GenericsService.class);		
			NotificationHandler.instance().processingStart();
			genericsService.getCollectionValues(cdf, datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {
				
				@Override
				public void onFailure(Throwable caught) {
					NotificationHandler.instance().processingStop();
					NotificationHandler.instance().handleException(caught);
				}
				
				@Override
				public void onSuccess(ArrayValueHolder result) {
					NotificationHandler.instance().processingStop();
					listener.onActionPerformed(result);
				}
			});
	}
}
