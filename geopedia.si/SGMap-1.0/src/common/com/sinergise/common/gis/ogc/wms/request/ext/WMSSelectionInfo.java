package com.sinergise.common.gis.ogc.wms.request.ext;

import static com.sinergise.common.gis.ogc.wms.request.WMSRequest.PARAM_UPDATESEQUENCE;
import static com.sinergise.common.gis.ogc.wms.request.ext.WMSHighlightRequest.PARAM_HIGHLIGHTED_FEATURES;
import static com.sinergise.common.gis.ogc.wms.request.ext.WMSHighlightRequest.PARAM_HIGHLIGHTED_SELECTION;

import java.io.Serializable;
import java.util.Collection;

import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.ext.FeatureSelectionInfo;
import com.sinergise.common.gis.map.model.ext.NamedFeatureSelectionSource;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo.NamedSelectionInfo.NamedSelectionSyncInfo;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.string.StringUtil;

public abstract class WMSSelectionInfo implements Serializable {
	public static class EmptySelectionInfo extends WMSSelectionInfo {
		private EmptySelectionInfo() {
			super("");
		}
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public void updateRequestHighlight(OGCRequest request) {
			//ignore
		}
	}
	
	public static class EnumeratedSelectionInfo extends WMSSelectionInfo {
		private String encodedFeatureIds;
		private EnumeratedSelectionInfo(String encodedFeatureIds, String hash) {
			super(hash);
			this.encodedFeatureIds = encodedFeatureIds;
		}
		private EnumeratedSelectionInfo(Collection<? extends RepresentsFeature> features, String hash) {
			this(CFeatureIdentifier.encode(features), hash);
		}
		@Override
		public void updateRequestHighlight(OGCRequest request) {
			super.updateRequestHighlight(request);
			request.set(PARAM_HIGHLIGHTED_FEATURES, encodedFeatureIds);
		}
		public CFeatureIdentifier[] getIds(FeaturesSource fSrc) {
			return CFeatureIdentifier.decode(fSrc, WMSUtil.decodeArray(encodedFeatureIds));
		}
	}
	
	public static class NamedSelectionInfo extends WMSSelectionInfo {
		
		public static class NamedSelectionSyncInfo {
			private NamedFeatureSelectionSource source;
			private FeatureSelectionInfo info;
			
			public NamedSelectionSyncInfo(NamedFeatureSelectionSource source, FeatureSelectionInfo info) {
				this.source = source;
				this.info = info;
			}
			
			private void doSync(String selectionName, SGAsyncCallback<Void> callback) {
				source.setNamedSelection(selectionName, info, callback);
			}
		}
		
		private String namedSelection;
		private NamedSelectionSyncInfo syncInfo;
		private boolean synced = false;
		
		private NamedSelectionInfo(String namedSelection, String hash) {
			this(namedSelection, hash, null);
		}
		
		private NamedSelectionInfo(String namedSelection, String hash, NamedSelectionSyncInfo syncInfo) {
			super(hash);
			this.namedSelection = namedSelection;
			this.syncInfo = syncInfo;
		}
		
		@Override
		public void updateRequestHighlight(OGCRequest request) {
			if (syncInfo != null && !synced) {
				throw new RuntimeException("Named selection not synced with server.");
			}
			super.updateRequestHighlight(request);
			request.set(PARAM_HIGHLIGHTED_SELECTION, namedSelection);
		}
		
		@Override
		public void updateRequestHighlight(final OGCRequest request, final SGAsyncCallback<OGCRequest> callback) {
			if (syncInfo != null) {
				syncInfo.doSync(namedSelection, new SGAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						synced = true;
						updateRequestHighlight(request);
						callback.onSuccess(request);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			} else {
				super.updateRequestHighlight(request, callback);
			}
		}
		
		public String getSelectionName() {
			return namedSelection;
		}
		
		@Override
		public boolean isNamed() {
			return true;
		}
	}
	public static WMSSelectionInfo NO_SELECTION = new EmptySelectionInfo();
	
	
	private String hash;
	protected WMSSelectionInfo(String hash) {
		this.hash = hash;
	}
	
	public void updateRequestHighlight(OGCRequest request) {
		request.set(PARAM_HIGHLIGHTED_FEATURES, null);
		request.set(PARAM_HIGHLIGHTED_SELECTION, null);
		request.set(PARAM_UPDATESEQUENCE, getHash());
	}
	
	public void updateRequestHighlight(OGCRequest request, SGAsyncCallback<OGCRequest> callback) {
		updateRequestHighlight(request);
		callback.onSuccess(request);
	}
	
	public String getHash() {
		return hash;
	}

	public boolean isEmpty() {
		return false;
	}
	
	public boolean isNamed() {
		return false;
	}
	
	public static WMSSelectionInfo createFromHighlight(OGCRequest request) {
		String list = request.get(PARAM_HIGHLIGHTED_FEATURES);
		if (!StringUtil.isNullOrEmpty(list)) {
			return new EnumeratedSelectionInfo(list, request.get(PARAM_UPDATESEQUENCE));
		}
		String namedSet = request.get(PARAM_HIGHLIGHTED_SELECTION);
		if (!StringUtil.isNullOrEmpty(namedSet)) {
			return new NamedSelectionInfo(namedSet, request.get(PARAM_UPDATESEQUENCE));
		}
		return NO_SELECTION;
	}
	
	public static WMSSelectionInfo createNamed(String selectionName, String hash) {
		return new NamedSelectionInfo(selectionName, hash);
	}
	
	public static WMSSelectionInfo createNamed(String selectionName, String hash, NamedSelectionSyncInfo syncInfo) {
		return new NamedSelectionInfo(selectionName, hash, syncInfo);
	}

	public static WMSSelectionInfo createEnumerated(Collection<? extends RepresentsFeature> features, String dataSourceHash) {
		return new EnumeratedSelectionInfo(features, dataSourceHash);
	}
}