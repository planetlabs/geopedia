package com.sinergise.common.gis.map.model.layer.system;

import java.util.Collection;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.Timer;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.feature.RepresentsFeatureCollection;
import com.sinergise.common.gis.map.model.ext.FeatureSelectionInfo;
import com.sinergise.common.gis.map.model.ext.NamedFeatureSelectionSource;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.ogc.base.OGCLayersSource;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.naming.Identifier;


public class SelectionSetLayer extends Layer {
	
	public static final String PROP_SELECTED_FEATURES="selectedFeatures";

	private boolean syncWithServer = false;

	boolean wasSyncedWithServer = false;
	
	long lastSyncedHash = 0;
	
	long lastModified = 0;
	
	Timer selectionChangeTimer = new Timer(){
		@Override
		public void run() {
			selectionChanged();
		}			
	};
	
	public SelectionSetLayer(String id, String title) {
		super(new SelectionSetLayerModel(id));
		setTitle(title);
	}
	
	public SelectionSetLayer(String id, SelectionSetLayerModel source) {
		super(id, source);
	}
	
	@Override
	public boolean isOpaque() {
		return false;
	}

	@Override
	public boolean hasAnythingToRender() {
		if (!super.hasAnythingToRender()) return false;
		return !isEmpty();
	}
	
	public <T extends Collection<? super RepresentsFeature>> T getSelectedForFeaturesSource(String dataSource, T retList) {
		return getSpec().getSelectedForFeaturesSource(dataSource, retList);
	}
	
	public Set<Identifier> getDataSourceIDs() {
		return getSpec().getDataSourceIDs();
	}
	
	public boolean addSelected(RepresentsFeature featureRef) {
		return processChange(getSpec().addSelected(featureRef));
	}

	public boolean removeSelected(RepresentsFeature featureRef) {
		return processChange(getSpec().removeSelected(featureRef));
	}
	
	public boolean clearSelection() {
		return processChange(getSpec().clearSelection());
	}
	
	protected boolean processChange(boolean changed) {
		if (changed) {
			scheduleSelectionChange();
		}
		return changed;
	}
	
	@Override
	public long getLastModified() {
		return lastModified;
	}
	
	@Override
	public SelectionSetLayerModel getSpec() {
		return (SelectionSetLayerModel)sourceLayerSpec;
	}		
	
	protected void scheduleSelectionChange(){
		selectionChangeTimer.cancel();		
		selectionChangeTimer.schedule(200);
	}
	
	protected void selectionChanged() {
		wasSyncedWithServer = false;
		
		if (syncWithServer) {
			if (sendSelectionChangeToServer()) {
				return;
			}
		}		
		setDirty();
		notifyChange(PROP_SELECTED_FEATURES);
	}
	
	

	private boolean sendSelectionChangeToServer() {
		boolean syncedStarted = false;
		final long[] curHash = new long[1];
		for (Identifier srcId : getDataSourceIDs()) {
			syncedStarted = true;
			
			LayersSource src = findLayersSource(srcId.getLocalID());
			if (!(src instanceof NamedFeatureSelectionSource)) {
				continue;
			}
			
			final Collection<RepresentsFeature> refs = getSelectedForFeaturesSource(src.getLocalID());
			if (refs.isEmpty()) {
				continue;
			}
			
			
			((NamedFeatureSelectionSource)src).setNamedSelection(
				getLocalID(),  
				new FeatureSelectionInfo(CFeatureUtils.toFeatureIdsArray(refs)),
				new SGAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						wasSyncedWithServer = true;
						curHash[0] ^= CollectionUtil.hashCode(refs);
						lastSyncedHash = curHash[0];
						setDirty();
						notifyChange(PROP_SELECTED_FEATURES);
					}
	
					@Override
					public void onFailure(Throwable e) {
						LoggerFactory.getLogger(SelectionSetLayer.class).error(
							"Failed to set named highlight selection on server", e);
					}
				}
			);
		}
		
		return syncedStarted;
	}

	private RepresentsFeatureCollection getSelectedForFeaturesSource(String localID) {
		return getSelectedForFeaturesSource(localID, new RepresentsFeatureCollection());
	}

	private static LayersSource findLayersSource(String dataSourceLocalId) {
		return ApplicationContext.getInstance().findLayersSource(dataSourceLocalId);
	}
	
	@Override
	public void setDirty() {
		lastModified = System.currentTimeMillis();
	}

	@Override
	public int getRenderDelayOnChange() {
	    return 200;
	}
	
	public Collection<CFeatureIdentifier> getSelectedIds() {
		return getSpec().getSelectedIds();
	}
	
	public void setSyncWithServer(boolean b) {
		syncWithServer = b;
	}
	
	public boolean wasSyncedWithServer() {
		return wasSyncedWithServer;
	}

	public boolean isEmpty() {
		return getSpec().isEmpty();
	}

	public WMSSelectionInfo constructSelectionInfo(String dataSource) {
		if (isEmpty()) {
			return WMSSelectionInfo.NO_SELECTION;
		}
		RepresentsFeatureCollection features = getSelectedForFeaturesSource(dataSource);
		long dsHash = getDataSourceHash(dataSource);
		if (wasSyncedWithServer()) {
			return WMSSelectionInfo.createNamed(getLocalID(), String.valueOf(lastSyncedHash^lastModified^dsHash));
		}
		return WMSSelectionInfo.createEnumerated(features, String.valueOf(dsHash));
	}

	private static long getDataSourceHash(String dataSource) {
		LayersSource src = findLayersSource(dataSource);
		if (src instanceof OGCLayersSource) {
			return ((OGCLayersSource)src).getLastChanged();
		}
		return 0;
	}

	public boolean addCollection(HasFeatureRepresentations selection) {
		return processChange(getSpec().addCollection(selection));
	}

	public boolean removeCollection(HasFeatureRepresentations selection) {
		return processChange(getSpec().removeCollection(selection));
	}
	
	@SuppressWarnings("unused")
	public boolean updateCollection(HasFeatureRepresentations selection) {
		return processChange(true);
	}

	public boolean setCollection(HasFeatureRepresentations refs) {
		return processChange(getSpec().setCollection(refs));
	}
	
	public void refresh() {
		processChange(true);
	}
}
