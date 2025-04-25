package com.sinergise.generics.core.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MasterDetailsHolder;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.services.GenericsService.DataExportTypes;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GenericsServiceAsync {
  void getEntityTypes(AsyncCallback<EntityType[]> callback);
  void getCollectionValues(DataFilter params, String datasourceID, int startIdx, int stopIdx, AsyncCallback<ArrayValueHolder> callback);
  void getEntityMetadata(String entityTypeName, AsyncCallback<String> callback);
  void getWidgetMetadata(String widgetName, String language, AsyncCallback<String> callback);
  void processEntities(ValueHolder values, String datasourceID, AsyncCallback<ProcessResultSet> callback);
  @Deprecated
  void processEntitiesWithReturnInformation(ValueHolder values, String datasourceID, AsyncCallback<String[]> callback);  
  void processMasterDetails(MasterDetailsHolder holder, AsyncCallback<ProcessResultSet> callback);
  void invalidateCache(String databaseId, AsyncCallback<Void> callback);
  
  void updateSessionLocale(String localeToSelect, AsyncCallback<String> callback);
  
  void prepareExportFile(DataFilter filter, String datasourceID, GenericObjectProperty[] properties, DataExportTypes type, AsyncCallback<String> callback);
}
