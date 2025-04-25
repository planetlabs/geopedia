package com.sinergise.generics.core.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MasterDetailsHolder;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.throwable.ProcessingException;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("generics")
public interface GenericsService extends RemoteService {
	public static final String PARAM_CMD="cmd";
	public static final String PARAM_FILE="file";	
	public static final String CMD_DOWNLOAD="dl";

	
	public static enum DataExportTypes {CSV,XLSX};
	
	
	String updateSessionLocale(String localeToSelect);
	EntityType[] getEntityTypes();  
	String getEntityMetadata(String entityTypeName);
	String getWidgetMetadata(String widgetName, String language);
	ArrayValueHolder getCollectionValues(DataFilter params,  String datasourceID, int startIdx, int stopIdx);
	ProcessResultSet processEntities(ValueHolder values, String datasourceID) throws Exception, ProcessingException;
	/* use processEntities */
	@Deprecated
	String[] processEntitiesWithReturnInformation(ValueHolder values, String datasourceID) throws Exception, ProcessingException;	
	ProcessResultSet processMasterDetails(MasterDetailsHolder holder) throws Exception, ProcessingException;	
	void invalidateCache(String databaseId);
	
	
	public String prepareExportFile(DataFilter filter, String datasourceID, GenericObjectProperty[] properties, DataExportTypes type);
}
