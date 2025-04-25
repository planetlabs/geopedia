package com.sinergise.geopedia.core.service;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.service.result.FeatureHeightResult;


public interface FeatureService extends RemoteService
{
	/** for tables with geometry, causes the geometry to be retrieved; ignored in other tables */
	public static final int RETR_GEOMETRY = 1;
	/** causes meta fields to be retrieved (user, timestamp, envelope, etc.) */
	public static final int RETR_METAFIELDS = 2;
	/** causes user data to be retrieved */
	public static final int RETR_USERFIELDS = 4;
	/** causes representative text to be retrieved */
	public static final int RETR_REPTEXT = 8;
	/** causes deleted features to also be included */
	public static final int RETR_DELETED = 16;
	
	/** causes full feature data to be retrieved */
	public static final int RETR_FULL = 15;
	
	public static final int RETR_ALL_FLAGS = 31;
    
    /** fields needed for attribute data updates - causes user fields and meta fields to be retrieved */
    public static final int RETR_EDIT_ATTRS = RETR_USERFIELDS | RETR_METAFIELDS;

    /** fields needed for graphical data updates - causes geometry and meta fields to be retrieved */
    public static final int RETR_EDIT_GEOM = RETR_GEOMETRY | RETR_METAFIELDS;
public ArrayList<ForeignReferenceProperty> getForeignReferences(int tableId, String filter) throws GeopediaException;
	
	
	
	public static final String SERVICE_URI = "feat";

	
	
	

	
	
	
	
	
	
	/*----------------------------*/	
	
	public List<FeatureHeightResult> queryFeatureHeights(int tableId, int featureId)throws UpdateException, GeopediaException;
	public Feature saveFeature(Feature feature) throws GeopediaException;
	public ArrayList<Feature> createFeatureFromGPX(int tableId, String fileToken) throws GeopediaException;
	
	
	public FeaturesQueryResults executeQuery (Query query) throws GeopediaException;	
}