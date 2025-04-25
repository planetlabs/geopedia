package com.sinergise.common.gis.feature;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.ogc.wfs.response.WFSTransactionResponse;
import com.sinergise.common.gis.query.Query;


public interface CFeatureDataSource extends FeaturesSource {
	public static interface FeatureAccessCallback {
		void onError(FeatureAccessException error);
	}
	public static interface FeatureDescriptorCallback extends FeatureAccessCallback {
		void onSuccess(CFeatureDescriptor[] result);
	}
	
	public static interface FeatureCollectionCallback extends FeatureAccessCallback {
		void onSuccess(FeatureInfoCollection features);
	}
	
	public static interface TransactionCallback extends FeatureAccessCallback {
		void onSuccess(WFSTransactionResponse responnse);
	}
	
	/**
	 * This may return cached descriptors if they are all available. Otherwise it must fetch the descriptor from the server and update the cache.
	 * 
	 * @param featureTypeName
	 * @param cb
	 * @throws FeatureAccessException
	 */
	void getDescriptor(String featureTypeName[], FeatureDescriptorCallback cb) throws FeatureAccessException;
	void getFeatureById(CFeatureIdentifier[] featureIds, FeatureCollectionCallback cb) throws FeatureAccessException;
	
    /**
     * @param layerName Name of feature layer to test
     * @param operationsMask Query filter operations mask to test.
     * @param geomTypeMask Mask for supported geometry types for spatial operations.
     * @return <code>true</code> if feature type supports query using provided operations masks.
     */
    public boolean supportsQueryOnLayer(String layerName, int operationsMask, int geomTypeMask);
    
    /**
     * @param query to perform.
     * @param cb callback, which receives CFeatures[] in its onSuccess() method
     */
    void queryFeatures(Query[] queries, FeatureCollectionCallback cb) throws FeatureAccessException;
    
    //TODO: redefine to user proper result callback when results are defined
    void insertOrUpdateFeatures(TransactionCallback cb, CFeature... cFeature) throws FeatureAccessException;
    
    //TODO: redefine to user proper result callback when results are defined
    void deleteFeatures(TransactionCallback cb, CFeatureIdentifier... featureIds) throws FeatureAccessException;
}
