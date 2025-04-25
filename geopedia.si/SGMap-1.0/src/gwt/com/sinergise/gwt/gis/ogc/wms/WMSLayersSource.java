/*
 *
 */
package com.sinergise.gwt.gis.ogc.wms;

import static com.sinergise.common.geometry.util.CoordUtil.PIX_SIZE_MICRO_OGC;
import static com.sinergise.common.gis.ogc.OGCRequest.PARAM_LOCALE;
import static com.sinergise.common.gis.ogc.wms.request.IWMSFeatureInfoRequest.PARAM_FEATURE_COUNT;
import static com.sinergise.common.gis.ogc.wms.request.WMSRequest.EXCEPTIONS_BLANK;
import static com.sinergise.common.gis.ogc.wms.request.WMSRequest.PARAM_EXCEPTIONS;
import static com.sinergise.common.util.web.MimeType.MIME_IMAGE_JPG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.map.model.ext.FeatureSelectionInfo;
import com.sinergise.common.gis.map.model.ext.NamedFeatureSelectionSource;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerSpec;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.map.model.layer.LegendImageSource;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.base.OGCLayersSource;
import com.sinergise.common.gis.ogc.wms.AbstractWmsLayerSpec;
import com.sinergise.common.gis.ogc.wms.request.WMSCapabilitiesRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSFeatureInfoRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSLegendImageRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSetNamedSelectionRequest;
import com.sinergise.common.gis.ogc.wms.response.WMSCapabilitiesResponse;
import com.sinergise.common.gis.ogc.wms.response.WMSFeatureInfoResponse;
import com.sinergise.common.gis.ogc.wms.response.ext.WMSSetNamedSelectionResponse;
import com.sinergise.common.gis.ogc.wms.rpc.WMSServiceRPC;
import com.sinergise.common.gis.ogc.wms.rpc.WMSServiceRPCAsync;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.gwt.util.SGRequestBuilder;
import com.sinergise.gwt.util.event.RemoteReqEvent;
import com.sinergise.gwt.util.event.RemoteReqEventHM;

public class WMSLayersSource implements OGCLayersSource, FeatureInfoSource, LegendImageSource, NamedFeatureSelectionSource {
	public static final String CAPABILITY_TILED_RENDERING = "TiledRendering";
	public static final String EVENT_CODE_GET_FEATURE_INFO = "wms-getFeatureInfo";
	public static final String SOURCE_TYPE_WMS = "WMS";

	protected HashMap<String, WMSLayerSpec> mySpecs = new HashMap<String, WMSLayerSpec>();

	public static final boolean isParentFeatureInfoEnabled(LayerTreeElement layer) {
		if (layer.isRoot()) return false;
		LayerTreeElement parentEl = layer.getParent();
		if (parentEl instanceof FeatureInfoLayer) {
			if (((FeatureInfoLayer) parentEl).isFeatureInfoEnabled()) return true;
		}
		return isParentFeatureInfoEnabled(parentEl);
	}

	protected final Logger logger;

	protected final Identifier identifier;
	protected final String baseURL;

	protected final OGCRequest requestDefaults = new OGCRequest(new StateGWT());
	protected final WMSServiceRPCAsync service;
	protected HashMap<String, Object> capabilities = new HashMap<String, Object>();

	protected long lastChanged = 0;

	public WMSLayersSource(String baseURL) {
		this(baseURL, baseURL);
	}

	public WMSLayersSource(String id, String baseURL) {
		this(id, baseURL, "1.3.0");
	}
	
	public WMSLayersSource(String id, String baseURL, String version) {
		this.baseURL = baseURL;
		logger = LoggerFactory.getLogger(WMSLayersSource.class.getName()+"."+id);
		identifier = new Identifier(Identifier.ROOT, id);
		requestDefaults.setVersion(version);
		requestDefaults.set(PARAM_EXCEPTIONS, WMSRequest.EXCEPTIONS_IN_IMAGE);
		requestDefaults.set(PARAM_LOCALE, LocaleInfo.getCurrentLocale().getLocaleName());

		this.service = createServiceInstance(baseURL + "_rpc");
		initDefaultCapabilities();
		init();
	}
	
	protected void initDefaultCapabilities() {
		capabilities.put(CAPABILITY_TILED_RENDERING, Boolean.TRUE);
		capabilities.put(CAPABILITY_FEATURE_INFO, Boolean.TRUE);
	}

	public String getBaseUrl() {
		return baseURL;
	}

	@Override
	public String getLocalID() {
		return identifier.getLocalID();
	}

	@Override
	public Identifier getQualifiedID() {
		return identifier;
	}

	/**
	 * Updates the timestamp of this source to force reload of images from the server on next repaint
	 */
	public void invalidate() {
		invalidate(System.currentTimeMillis());
	}
	
	@Override
	public void invalidate(long changeTimestamp) {
		lastChanged = Math.max(changeTimestamp, lastChanged); 
	}

	public OGCRequest getRequestDefaults() {
		return requestDefaults;
	}
	
	@Override
	public long getLastChanged() {
		return lastChanged;
	}
	
	protected WMSServiceRPCAsync createServiceInstance(String serviceBaseURL) {
		return WMSServiceRPC.Util.createInstance(serviceBaseURL);
	}

	@Override
	public <T extends LayersSource> void asyncInitialize(AsyncCallback<? super T> cb) {
		internalAsyncInit(cb);
	}

	@SuppressWarnings("unchecked")
	protected final <T extends LayersSource> void internalAsyncInit(AsyncCallback<? super T> cb) {
		//TODO: Fetch capabilities
//		getCapabilities(new WMSCapabilitiesRequest(), new AsyncCallbackAdapter<WMSCapabilitiesResponse, T>(cb) {
//			@Override
//			protected T processOriginalResult(WMSCapabilitiesResponse result) {
//				WMSLayersSource.this.capabilitiesLoaded(result);
//				return (T)WMSLayersSource.this;
//			}
//		});
		cb.onSuccess((T)this);
	}
	
	public void getCapabilities(WMSCapabilitiesRequest req, final AsyncCallback<? super WMSCapabilitiesResponse> cb) {
		//TODO: Move common requesting and XML-parsing code out of here
		try {
			SGRequestBuilder rb = new SGRequestBuilder(req.getMethod(), req.createRequestURL(baseURL));
			rb.setCallback(new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					Document parsed = XMLParser.parse(response.getText());
					
					Node root = parsed.getFirstChild();
					Node capability = getChildTag(root, "Capability");
					Node rootLayer = getChildTag(capability, "Layer");
					
					
					WMSCapabilitiesResponse resp = new WMSCapabilitiesResponse();
					resp.setLayers(new AbstractWmsLayerSpec[] {parseLayer(rootLayer)});
					capabilitiesLoaded(resp);
					cb.onSuccess(resp);
				}
				
				private AbstractWmsLayerSpec parseLayer(Node layerNode) {
					WMSLayerSpec spec = new WMSLayerSpec(WMSLayersSource.this, getTagValue(layerNode, "Name"));
					//TODO: Parse all WMS layer properties (including BBOXes and styles (with legend URL))
					spec.setProperty(AbstractWmsLayerSpec.PROP_TITLE, getTagValue(layerNode, "Title"));
					spec.setProperty(AbstractWmsLayerSpec.PROP_ABSTRACT, getTagValue(layerNode, "Abstract"));
					spec.setQueryable("1".equalsIgnoreCase(getAttributeValue(layerNode, "queryable")));
					List<Node> childNodes = getChildTags(layerNode, "Layer");
					for (Node node : childNodes) {
						spec.addChild(parseLayer(node));
					}
					return spec;
				}

				private List<Node> getChildTags(Node parent, String tagName) {
					List<Node> ret = new ArrayList<Node>();
					Node cur = parent.getFirstChild();
					while (cur != null) {
						if (tagName.equals(cur.getNodeName())) {
							ret.add(cur);
						}
						cur = cur.getNextSibling();
					}
					return ret;
				}

				private String getAttributeValue(Node node, String attName) {
					Node attNode = node.getAttributes().getNamedItem(attName);
					if (attNode == null) return null;
					return attNode.getNodeValue();
				}

				private String getTagValue(Node parent, String tagName) {
					return getChildTag(parent, tagName).getFirstChild().getNodeValue();
				}

				private Node getChildTag(Node parent, String tagName) {
					Node cur = parent.getFirstChild();
					while (cur != null) {
						if (tagName.equals(cur.getNodeName())) {
							return cur;
						}
						cur = cur.getNextSibling();
					}
					return null;
				}
				@Override
				public void onError(Request request, Throwable exception) {
					cb.onFailure(exception);
				}
			});
		rb.send();
		} catch (Throwable t) {
			cb.onFailure(t);
		}
	}

	protected void capabilitiesLoaded(WMSCapabilitiesResponse caps) {
		AbstractWmsLayerSpec[] layers = caps.getLayers();
		if (layers != null) {
			for (AbstractWmsLayerSpec newSpec : layers) {
				specLoaded(newSpec);
			}
		}
	}

	private void specLoaded(AbstractWmsLayerSpec newSpec) {
		WMSLayerSpec curSpec = mySpecs.get(newSpec.getLocalID());
		if (curSpec == null) {
			curSpec = createLayerSpec(newSpec.getLocalID());
		}
		curSpec.setFrom(newSpec);
		for (AbstractWmsLayerSpec childSpec : newSpec.getChildren()) {
			specLoaded(childSpec);
			curSpec.addChild(childSpec);
		}
	}

	public WMSLayerSpec getLayerSpec(String localID) {
		return mySpecs.get(localID);
	}

	public void getGetMapDefaults(WMSMapRequest toFill, CRS mapCRS) {
		toFill.setDefaults(getRequestDefaults());
		toFill.setCRS(mapCRS);
	}

	public WMSMapRequest getDefaultGetMapRequest(CRS mapCRS) {
		WMSMapRequest req = getEmptyGetMapReq();
		getGetMapDefaults(req, mapCRS);
		return req;
	}

	protected WMSMapRequest getEmptyGetMapReq() {
		return new WMSMapRequest();
	}

	public void setExceptions(String exceptions) {
		requestDefaults.set(WMSRequest.PARAM_EXCEPTIONS, exceptions);
	}

	protected void init() {
	}

	@Override
	public String getLegendImageURL(LayerTreeElement el, DimI size, boolean trans) {
		if (!el.showLegend()) {
			return null;
		}
		WMSLayer lyr = (WMSLayer) el;
		WMSLegendImageRequest req = new WMSLegendImageRequest();
		req.setDefaults(requestDefaults);
		req.setLayer(lyr.getWMSName());
		req.setStyle(lyr.getWMSStyleName());
		req.setImageSize(size);
		req.setExceptions(WMSRequest.EXCEPTIONS_BLANK);
		req.setFormat(MimeType.MIME_IMAGE_PNG);
		req.setTransparent(trans);
		return req.createRequestURL(baseURL);
	}

	@Override
	public String getTypeIdentifier() {
		return SOURCE_TYPE_WMS;
	}

	@Override
	public boolean isInitialized() {
		return baseURL != null && baseURL.length() > 0;
	}

	@Override
	public LayerSpec findLayerSpec(String layerId, boolean ignoreCase) {
		// TODO: implement this
		// LayerSpec spc=super.findLayerSpec(name, ignoreCase);
		// if (spc==null) {
		// spc=new WMSLayerSpec(this, name);
		// getRoot().add(spc);
		// }
		// return spc;
		return createLayerSpec(layerId);
	}

	public WMSLayerSpec createLayerSpec(StateGWT st) {
		return new WMSLayerSpec(this, st);
	}

	public WMSLayerSpec createLayerSpec(String layerId) {
		return new WMSLayerSpec(this, layerId);
	}

	public WMSLayer createLayer(String name) {
		return (WMSLayer) createLayer(findLayerSpec(name, true));
	}

	@Override
	public Layer createLayer(LayerSpec spec) {
		WMSLayer ret = new WMSLayer((WMSLayerSpec) spec);
		return ret;
	}

	@Override
	public boolean supports(String capability) {
		Object cp = capabilities.get(capability);
		if (cp == null || Boolean.FALSE.equals(cp))
			return false;
		return true;
	}
	
	public Object getCapability(String capability) {
		return capabilities.get(capability);
	}

	@Override
	public boolean supportsInfoType(MimeType type) {
		if (type.isParentOrEqual(TYPE_HTML_STRING))
			return true;
		if (type.isParentOrEqual(FeatureInfoCollection.MIME_OBJECT_FEATURE_INFO_COLLECTION))
			return true;
		return false;
	}

	@Override
	public String getFeaturesSourceType() {
		return SOURCE_TYPE_WMS;
	}
	

	public void descriptorLoaded(CFeatureDescriptor d) {
		if (mySpecs.get(d.getLocalID()) != null) {
			mySpecs.get(d.getLocalID()).setDescriptor(d);
		}
	}

	boolean hasDescriptor(String featureType) {
		return mySpecs.get(featureType) != null
			&& mySpecs.get(featureType).getDescriptor() != null;
	}

	public Identifier getFeaturesSourceId() {
		return identifier;
	}

	@Override
	public <T> void getFeatureInfo(Layer[] visibleLayers, FeatureInfoLayer[] queryLayers, CRS crs, double wx, double wy, double pxRadius, double scale, MimeType type, final SGAsyncCallback<T> cb) {
		WMSFeatureInfoRequest req = createFeatureInfoRequest(visibleLayers, queryLayers, crs, wx, wy, pxRadius, scale, type);
		getFeatureInfo(req, cb);
	}

	@SuppressWarnings("unchecked")
	public <T> void getFeatureInfo(WMSFeatureInfoRequest req, final SGAsyncCallback<T> cb) {
		final MimeType reqType = req.getInfoFormat();
		final RemoteReqEvent rrEvent = new RemoteReqEvent(EVENT_CODE_GET_FEATURE_INFO);
		
		if (MimeType.MIME_JAVA_OBJECT.isParentOrEqual(reqType)) {
			RemoteReqEventHM.getInstance().fireEvent(rrEvent);
			getObjectFeatureInfo(cb, req, rrEvent);
			
		} else if (TYPE_HTML_STRING.isEqualOrAlternative(reqType)) {
			//TODO: Show HTML result in iFrame, so that remote WMS can be used directly 
			getHtmlFeatureInfo((SGAsyncCallback<? super String>)cb, req, rrEvent);
			
		} else {
			cb.onFailure(new Exception("Illegal FeatureInfo Result Type " + reqType));
		}
	}

	protected <T> void getObjectFeatureInfo(final SGAsyncCallback<T> cb, WMSFeatureInfoRequest req, final RemoteReqEvent rrEvent) {
		service.getFeatureInfo(req, new AsyncCallback<WMSFeatureInfoResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				cb.onFailure(caught);
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}

			@Override
			@SuppressWarnings("unchecked")
			public void onSuccess(WMSFeatureInfoResponse result) {
				prepareResult(result, getQualifiedID());
				cb.onSuccess((T)result);
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}
		});
	}

	protected void getHtmlFeatureInfo(final SGAsyncCallback<? super String> cb, WMSFeatureInfoRequest req, final RemoteReqEvent rrEvent) {
		String reqUrl = req.createRequestURL(baseURL);
		System.out.println(reqUrl);
		RequestBuilder bld = new RequestBuilder(RequestBuilder.GET, reqUrl);
		
		bld.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 200) { // HTTP OK code
					cb.onSuccess(response.getText());
				} else {
					cb.onFailure(new Exception("Error while getting HTML feature info; status=" + response.getStatusCode() + " " + response.getStatusText()));
				}
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}

			@Override
			public void onError(Request request, Throwable exception) {
				RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
			}
		});
		try {
			RemoteReqEventHM.getInstance().fireEvent(rrEvent);
			bld.send();
		} catch (Exception e) {
			cb.onFailure(e);
			RemoteReqEventHM.getInstance().fireEvent(rrEvent.setFinished());
		}
	}
	
	

	protected void prepareResult(FeatureInfoCollection result) {
		prepareResult(result, getFeaturesSourceId());
	}

	public final void prepareResult(FeatureInfoCollection result, Identifier dsId) {
		for (int i = 0; i < result.getItemCount(); i++) {
			FeatureInfoItem itm = result.getItem(i);
			CFeatureIdentifier itmId = itm.getQualifiedID();
			Identifier srcId = itmId.getDataSourceID();
			if (srcId == null || srcId.getLocalID() == null) {
				itmId.getFeatureTypeID().bindTo(dsId);
			}
			CFeatureDescriptor itmDesc = itm.f.getDescriptor();
			if (itmDesc != null) {
				Identifier qid = itmDesc.getQualifiedID();
				if (!qid.isBound()) {
					qid.bindTo(dsId);
				}
				//should not overload the layer descriptor with a subset of a descriptor used for query
				//but set it if not loaded yet TODO: check if necessary
				if (!hasDescriptor(itm.f.getFeatureTypeName())) {
					descriptorLoaded(itmDesc);
				}
			}
		}
	}

	/**
	 * Generates WMSFeatureInfoRequest from commonly accessible data on GUI
	 * 
	 * @param visibleLayers
	 * @param queryLayers
	 * @param wx
	 * @param wy
	 * @param scale
	 * @param type
	 * @return
	 */
	public WMSFeatureInfoRequest createFeatureInfoRequest(Layer[] visibleLayers, FeatureInfoLayer[] queryLayers, CRS crs, double wx, double wy, double pxRadius, double scale, MimeType type) {
		ArrayList<WMSLayer> lst = new ArrayList<WMSLayer>();
		for (int i = 0; i < visibleLayers.length; i++) {
			Layer l = visibleLayers[i];
			if (l.getSource() != this) continue;
			if (l instanceof WMSLayer) lst.add((WMSLayer) l);
		}
		WMSLayer[] visL = new WMSLayer[lst.size()];
		ArrayUtil.toArray(lst, visL);

		lst.clear();
		for (int i = 0; i < queryLayers.length; i++) {
			FeatureInfoLayer l = queryLayers[i];
			if (l instanceof WMSLayer) {
				WMSLayer wl = (WMSLayer) l;
				if (wl.getSource() != this) continue;
				lst.add(wl);
			}
		}
		WMSLayer[] qryL = new WMSLayer[lst.size()];
		ArrayUtil.toArray(lst, qryL);

		WMSFeatureInfoRequest req = new WMSFeatureInfoRequest();
		req.setDefaults(requestDefaults);
		if (!req.containsParam(PARAM_FEATURE_COUNT)) {
			req.setFeatureCount(7);
		}
		req.setCRS(crs);
		req.setTransparent(false);
		req.setExceptions(EXCEPTIONS_BLANK);
		req.setFormat(MIME_IMAGE_JPG);
		req.setLayers(visL);

		req.setQueryPoint(wx, wy, CoordUtil.worldPerPix(scale, PIX_SIZE_MICRO_OGC), pxRadius, 128, 128);
		req.setQueryLayers(qryL);
		req.setInfoFormat(type);
		return req;
	}

	public String[] getLayerIDs() {
		Collection<String> col = mySpecs.keySet();
		return col.toArray(new String[col.size()]);
	}
	
	@Override
	public void setNamedSelection(final String selectionName, FeatureSelectionInfo selectionInfo, final SGAsyncCallback<Void> callback) {
		WMSSetNamedSelectionRequest req = new WMSSetNamedSelectionRequest(selectionName, selectionInfo);
		req.setDefaults(requestDefaults);
		service.setNamedSelection(req, new AsyncCallback<WMSSetNamedSelectionResponse>() {
				@Override
				public void onSuccess(WMSSetNamedSelectionResponse result) {
					logger.trace("Named selection "+selectionName+" set on server.");
					callback.onSuccess(null);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			}
		);
	}
	
}
