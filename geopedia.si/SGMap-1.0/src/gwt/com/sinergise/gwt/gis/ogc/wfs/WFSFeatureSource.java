package com.sinergise.gwt.gis.ogc.wfs;

import static com.sinergise.common.gis.ogc.OGCRequest.PARAM_LOCALE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.wfs.WFSService;
import com.sinergise.common.gis.ogc.wfs.WFSServiceAsync;
import com.sinergise.common.gis.ogc.wfs.request.WFSDeleteFeatureRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSDescribeFeatureTypeRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSGetFeatureRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSInsertOrUpdateRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSRequest;
import com.sinergise.common.gis.ogc.wfs.request.WFSTransactionRequest;
import com.sinergise.common.gis.ogc.wfs.response.WFSDescribeFeatureTypeResponse;
import com.sinergise.common.gis.ogc.wfs.response.WFSGetFeatureResponse;
import com.sinergise.common.gis.ogc.wfs.response.WFSTransactionResponse;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.gwt.gis.ogc.combined.OGCCombinedLayersSource;
import com.sinergise.gwt.util.event.RemoteReqEvent;
import com.sinergise.gwt.util.event.RemoteReqEventHM;

//TODO: Move to common
public class WFSFeatureSource implements CFeatureDataSource, FeatureInfoSource {
	public static final class DescriptorsStore {
		
	}
	
	
	public static final String EVENT_CODE_GET_DESCRIPTOR = "wfs-getDescriptor";
	public static final String EVENT_CODE_GET_FEATURE_BY_ID = "wfs-getFeatureById";
	public static final String EVENT_CODE_QUERY_FEATURES = "wfs-queryFeatures";
	public static final String EVENT_CODE_TRANSACTION = "wfs-transaction";
	
	public static final String TYPE = "WFS";
	Identifier wfsID;
	WFSServiceAsync rpcService;

	// TODO: Support stand-alone mode; Introduce an abstract OGCFeatureSource if necessary.
	protected OGCCombinedLayersSource wmsSource;
	protected WFSRequest requestDefaults = null;
	
	protected DescriptorsStore descCache = new DescriptorsStore();

	public WFSFeatureSource(String baseURL, String sourceID) {
		this.wfsID = new Identifier(Identifier.ROOT, sourceID == null ? baseURL : sourceID);
		try {
			rpcService = WFSService.Util.createInstance(baseURL + "_rpc");
		} catch (Exception e) {
		}
	}

	public void asyncInitialize(final AsyncCallback<? super WFSFeatureSource> cb) {
		updateDescriptors(new FeatureDescriptorCallback() {
			@Override
			public void onError(FeatureAccessException error) {
				cb.onFailure(error);
			}
			
			@Override
			public void onSuccess(CFeatureDescriptor[] result) {
				cb.onSuccess(WFSFeatureSource.this);
			}
		});
	}

	@Override
	public boolean supportsInfoType(MimeType type) {
		return wmsSource == null ? false : wmsSource.supportsInfoType(type);
	}

	@Override
	public <T> void getFeatureInfo(Layer[] visibleLayers, FeatureInfoLayer[] queryLayers, CRS crs, double wx, double wy, double pxRadius, double scale, MimeType type, SGAsyncCallback<T> cb) {
		if (wmsSource == null)
			cb.onFailure(new RuntimeException("WMS Source not set on the WFSFeatureSource instance"));
		else {
			wmsSource.getFeatureInfo(visibleLayers, queryLayers, crs, wx, wy, pxRadius, scale, type, cb);
		}
	}

	public OGCRequest getRequestDefaults() {
		if (requestDefaults == null) {
			if (wmsSource != null) {
				requestDefaults = new WFSRequest(wmsSource.getRequestDefaults().getProperties());
			} else {
				requestDefaults = new WFSRequest(new StateGWT());
			}
			requestDefaults.set(PARAM_LOCALE, LocaleInfo.getCurrentLocale().getLocaleName());
		}
		return requestDefaults;
	}

	public WFSFeatureSource(String baseURL, OGCCombinedLayersSource combinedSource) {
		this(baseURL, combinedSource.getLocalID());
		this.wmsSource = combinedSource;
	}

	public void updateDescriptors(final CFeatureDataSource.FeatureDescriptorCallback cb) {
		
		getDescriptor(wmsSource.getDataLayerIDs(), new FeatureDescriptorCallback() {
			@Override
			public void onError(FeatureAccessException error) {
				cb.onError(error);
			}
			@Override
			public void onSuccess(CFeatureDescriptor[] result) {
				for (CFeatureDescriptor d : result) {
					wmsSource.descriptorLoaded(d);
				}
				cb.onSuccess(result);
			}
		});
	}
	
	protected void bindDescriptor(CFeatureDescriptor d) {
		d.getQualifiedID().bindTo(getQualifiedID());
	}

	/**
	 * The descriptor that comes as a result of this call is already bound to this data source.
	 */
	@Override
	public void getDescriptor(String[] featureTypeName, final FeatureDescriptorCallback cb) {
		if (featureTypeName == null || featureTypeName.length==0) {
			cb.onSuccess(new CFeatureDescriptor[0]);
			return;
		}
		WFSDescribeFeatureTypeRequest req = new WFSDescribeFeatureTypeRequest();
		req.setDefaults(getRequestDefaults());
		req.setTypeNames(featureTypeName);
		
		final RemoteReqEvent rrEvent = new RemoteReqEvent(EVENT_CODE_GET_DESCRIPTOR);
		RemoteReqEventHM.getInstance().fireEvent(rrEvent);
		
		rpcService.describeFeatureType(req, new SGAsyncCallback<WFSDescribeFeatureTypeResponse>() {
			@Override
			public void onSuccess(WFSDescribeFeatureTypeResponse result) {
				CFeatureDescriptor[] descs = result.getFeatureDescriptors();
				//cache descriptor
				for(CFeatureDescriptor d : descs) {
					bindDescriptor(d);
					wmsSource.descriptorLoaded(d);
				}
				cb.onSuccess(descs);
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}

			@Override
			public void onFailure(Throwable caught) {
				cb.onError(new FeatureAccessException(caught.getMessage(), caught.getCause()));
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}
		});
	}

	public Query[] createFeatureIDQueries(CFeatureIdentifier[] featureIds) throws InvalidFilterDescriptorException {
		if (featureIds == null)
			return null;
		if (featureIds.length == 0)
			return new Query[0];
		if (featureIds.length == 1) {
			CFeatureIdentifier id = featureIds[0];
			return new Query[] { new Query(id.getFeatureTypeName(), new IdentifierOperation(id.getLocalID())) };
		}
		HashMap<String, ArrayList<IdentifierOperation>> map = new HashMap<String, ArrayList<IdentifierOperation>>();
		for (int i = 0; i < featureIds.length; i++) {
			String fType = featureIds[i].getFeatureTypeName();
			ArrayList<IdentifierOperation> ops = map.get(fType);
			if (ops == null) {
				ops = new ArrayList<IdentifierOperation>();
				map.put(fType, ops);
			}
			ops.add(new IdentifierOperation(featureIds[i].getLocalID()));
		}
		Query[] retQry = new Query[map.size()];
		int i = 0;
		for (Iterator<Map.Entry<String, ArrayList<IdentifierOperation>>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, ArrayList<IdentifierOperation>> en = it.next();
			String fType = en.getKey();
			ArrayList<IdentifierOperation> lst = en.getValue();
			IdentifierOperation[] idOps = ArrayUtil.toArray(lst, new IdentifierOperation[lst.size()]);
			Query qry = new Query(fType, new LogicalOperation(idOps, FilterCapabilities.SCALAR_OP_LOGICAL_OR));
			retQry[i++] = qry;
		}
		return retQry;
	}

	@Override
	public void getFeatureById(CFeatureIdentifier[] featureIds, final FeatureCollectionCallback cb) {
		WFSGetFeatureRequest req = new WFSGetFeatureRequest();
		req.setDefaults(getRequestDefaults());
		req.setFeatureIds(featureIds);

		final RemoteReqEvent rrEvent = new RemoteReqEvent(EVENT_CODE_GET_FEATURE_BY_ID);
		RemoteReqEventHM.getInstance().fireEvent(rrEvent);
		
		rpcService.getFeature(req, new SGAsyncCallback<WFSGetFeatureResponse>() {
			@Override
			public void onSuccess(WFSGetFeatureResponse result) {
				wmsSource.prepareResult(result, getQualifiedID());
				cb.onSuccess(result);
				
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}

			@Override
			public void onFailure(Throwable caught) {
				cb.onError(new FeatureAccessException(caught.getMessage(), caught.getCause()));
				
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}
		});
	}

	@Override
	public void queryFeatures(Query[] queries, final FeatureCollectionCallback cb) {
		WFSGetFeatureRequest req = new WFSGetFeatureRequest();
		req.setDefaults(getRequestDefaults());
		req.setQueries(queries);
		
		final RemoteReqEvent rrEvent = new RemoteReqEvent(EVENT_CODE_QUERY_FEATURES);
		RemoteReqEventHM.getInstance().fireEvent(rrEvent);
		
		rpcService.getFeature(req, new SGAsyncCallback<WFSGetFeatureResponse>() {
			@Override
			public void onSuccess(WFSGetFeatureResponse result) {
				try {
					wmsSource.prepareResult(result, getQualifiedID());
					cb.onSuccess(result);
				} finally {
					RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				try {
					cb.onError(new FeatureAccessException(caught.getMessage(), caught.getCause()));
				} finally {
					RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
				}
			}
		});
	}
	
	@Override
	public void insertOrUpdateFeatures(final TransactionCallback cb, CFeature ...features) {
		WFSTransactionRequest req = new WFSInsertOrUpdateRequest(features);
		req.setDefaults(getRequestDefaults());
		
		final RemoteReqEvent rrEvent = new RemoteReqEvent(EVENT_CODE_TRANSACTION);
		RemoteReqEventHM.getInstance().fireEvent(rrEvent);
		
		rpcService.transaction(req, new SGAsyncCallback<WFSTransactionResponse>() {
			@Override
			public void onSuccess(WFSTransactionResponse result) {
				cb.onSuccess(result);
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}

			@Override
			public void onFailure(Throwable caught) {
				cb.onError(new FeatureAccessException(caught.getMessage(), caught.getCause()));
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}
		});
	}
	
	@Override
	public void deleteFeatures(final TransactionCallback cb, CFeatureIdentifier... featureIds) {
		WFSTransactionRequest req = new WFSDeleteFeatureRequest(featureIds);
		req.setDefaults(getRequestDefaults());
		
		final RemoteReqEvent rrEvent = new RemoteReqEvent(EVENT_CODE_TRANSACTION);
		RemoteReqEventHM.getInstance().fireEvent(rrEvent);
		
		rpcService.transaction(req, new SGAsyncCallback<WFSTransactionResponse>() {
			@Override
			public void onSuccess(WFSTransactionResponse result) {
				cb.onSuccess(result);
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}

			@Override
			public void onFailure(Throwable caught) {
				cb.onError(new FeatureAccessException(caught.getMessage(), caught.getCause()));
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}
		});
	}

	@Override
	public boolean supportsQueryOnLayer(String layerName, int operationsMask, int geomTypeMask) {
		return true;
	}

	@Override
	public String getLocalID() {
		return wfsID.getLocalID();
	}

	@Override
	public Identifier getQualifiedID() {
		return wfsID;
	}

	@Override
	public String getFeaturesSourceType() {
		return TYPE;
	}

	public OGCCombinedLayersSource getLayersSource() {
		return wmsSource;
	}
}
