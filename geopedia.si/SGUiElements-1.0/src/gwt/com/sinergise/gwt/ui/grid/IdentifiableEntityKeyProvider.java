package com.sinergise.gwt.ui.grid;

import com.google.gwt.view.client.ProvidesKey;
import com.sinergise.common.util.naming.IdentifiableEntity;

public class IdentifiableEntityKeyProvider<T extends IdentifiableEntity> implements ProvidesKey<T> {
	@Override
	public Object getKey(T item) {
		return item.getLocalID();
	}
	
	public static class ByQualifiedID<T extends IdentifiableEntity> implements ProvidesKey<T> {
		@Override
		public Object getKey(T item) {
			return item.getQualifiedID();
		}
	}
	
}