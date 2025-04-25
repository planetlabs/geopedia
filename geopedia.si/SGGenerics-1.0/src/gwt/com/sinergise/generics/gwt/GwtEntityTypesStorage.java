package com.sinergise.generics.gwt;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.util.lang.AsyncFunction;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.lang.SGAsyncCallback.AsyncCallbackWrapper;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.EntityTypeStorage;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.core.GenericsClientSession;

public class GwtEntityTypesStorage extends EntityTypeStorage {

	final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GwtEntityTypesStorage.class);

	Map<Integer, EntityType> entityTypesMap = new HashMap<Integer, EntityType>();
	int maxId = Integer.MIN_VALUE;

	final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	private static GwtEntityTypesStorage instance = null;

	public static GwtEntityTypesStorage getInstance() {
		if (instance == null) {
			instance = new GwtEntityTypesStorage();
		}
		return instance;
	}

	public void initializeGWTOnly(final EntryPoint entryPoint) {
		initializeStorage(GwtEntityTypesStorage.this);
		if (entryPoint != null) entryPoint.onModuleLoad();
	}

	@Override
	public void initializeStorage(EntityTypeStorage ets) {
		super.initializeStorage(ets);
	}


	public void initialize(final AsyncCallback<? super GwtEntityTypesStorage> callback) {

		genericsService.getEntityTypes(new AsyncCallback<EntityType[]>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.error("Failed to retrieve EntityTypes!", caught);
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(EntityType[] result) {
				try {
					GwtEntityTypesStorage.this.initializeStorage(GwtEntityTypesStorage.this);
					if (result != null) {
						for (EntityType e : result) {
							entityTypesMap.put(Integer.valueOf(e.getId()), e);
							if (e.getId() > maxId) maxId = e.getId();
						}
					}
					
					String locale = LocaleInfo.getCurrentLocale().getLocaleName();
					genericsService.updateSessionLocale(locale, new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							logger.error("Failed to set locale!", caught);
							callback.onFailure(caught);
						}

						@Override
						public void onSuccess(String  locale) {
							GenericsClientSession.getInstance().locale = locale;
							callback.onSuccess(GwtEntityTypesStorage.this);
						}
					});
				} catch(Throwable t) {
					logger.error("Client-side error after retrieving entity types...", t);
					callback.onFailure(t);
				}
			}

		});
	}


	public EntityType createNewEntityType(String name) {
		int id;
		if (maxId == Integer.MIN_VALUE) {
			id = 0;
			maxId = id;
		} else {
			maxId++;
			id = maxId;
		}
		EntityType et = new EntityType(name, id);
		entityTypesMap.put(id, et);
		return et;
	}

	@Override
	public EntityType getEntityType(Integer id) {
		return entityTypesMap.get(id);
	}


	public Integer getEntityTypeId(String name) {
		for (EntityType et : entityTypesMap.values()) {
			if (et.getName().equals(name)) return et.getId();
		}
		return null;
	}

	public EntityType getEntityType(String name) {
		for (EntityType et : entityTypesMap.values()) {
			if (et.getName().equals(name)) return et;
		}
		return null;
	}


	public EntityObject createEntityObject(int entityTypeId) {
		return new AbstractEntityObject(entityTypeId);
	}

	public EntityObject createEntityObject(EntityType entityType) {
		return createEntityObject(entityType.getId());
	}

	public EntityObject createEntityObject(String entityTypeName) {
		EntityType et = getEntityType(entityTypeName);
		if (et == null) return null;
		return createEntityObject(et);
	}

	public static class InitGwtEntityTypesStorageAsync implements AsyncFunction<Object, GwtEntityTypesStorage> {
		@Override
		public void executeAsync(Object param, final SGAsyncCallback<? super GwtEntityTypesStorage> callback) throws Exception {
			GwtEntityTypesStorage.getInstance().initialize(new AsyncCallbackWrapper<GwtEntityTypesStorage>(callback) {
				@Override
				public void onFailure(Throwable caught) {
					LoggerFactory.getLogger(getClass()).error("Error while initializing GwtEntityTypesStorage: "+caught.getMessage(), caught);
					super.onFailure(caught);
				}
			});
		}
	}

}
