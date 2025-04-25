package com.sinergise.gwt.gis.query.filter;

import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.util.lang.AsyncFunction;
import com.sinergise.common.util.lang.SGAsyncCallback;

public interface SupportsLayerFiltering {

	void setLayerFilter(String layerName, FilterDescriptor filter, SGAsyncCallback<Void> callback);
	
	public static class SetLayerFilterFunction implements AsyncFunction<Object, Void> {
		
		final SupportsLayerFiltering src;
		final String layerName;
		final FilterDescriptor filter;
		
		public SetLayerFilterFunction(SupportsLayerFiltering src, String layerName, FilterDescriptor filter) {
			this.src = src;
			this.layerName = layerName;
			this.filter = filter;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void executeAsync(Object param, SGAsyncCallback<? super Void> callback) {
			src.setLayerFilter(layerName, filter, (SGAsyncCallback<Void>)callback);
		}
		
	}
	
}
