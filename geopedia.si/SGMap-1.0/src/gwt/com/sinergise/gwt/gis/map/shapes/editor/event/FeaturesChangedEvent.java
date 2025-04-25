package com.sinergise.gwt.gis.map.shapes.editor.event;

import static java.util.Collections.unmodifiableSet;

import java.util.Set;

import com.google.gwt.event.shared.EventHandler;
import com.sinergise.common.gis.editor.FeatureChanges;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent.FeaturesUpdatedEventHandler;
import com.sinergise.gwt.util.event.bus.SGEventBus.SGEvent;

public class FeaturesChangedEvent extends SGEvent<FeaturesUpdatedEventHandler> {
	
	public interface FeaturesUpdatedEventHandler extends EventHandler {
		void onFeaturesUpdated(FeaturesChangedEvent event);
	}

	public static final Type<FeaturesUpdatedEventHandler> TYPE = new Type<FeaturesUpdatedEventHandler>();
	
	private final FeatureChanges changes;
	
	public FeaturesChangedEvent(FeatureChanges changes) 
	{
		this.changes = changes;
	}
	
	@Override
	public Type<FeaturesUpdatedEventHandler> getAssociatedType() {
		return TYPE;
	}
	
	@Override
	protected void directDispatch(FeaturesUpdatedEventHandler handler) {
		handler.onFeaturesUpdated(this);
	}
	
	@Override
	protected boolean canHandle(EventHandler handler) {
		return handler instanceof FeaturesUpdatedEventHandler;
	}
	
	public boolean hasFeatureId(RepresentsFeature feature) {
		return isUpdated(feature)
			|| isInserted(feature)
			|| isDeleted(feature);
	}
	
	public boolean isUpdated(RepresentsFeature feature) {
		return changes.getUpdates().contains(feature.getQualifiedID());
	}
	
	public boolean isInserted(RepresentsFeature feature) {
		return changes.getInserts().contains(feature.getQualifiedID());
	}
	
	public boolean isDeleted(RepresentsFeature feature) {
		return changes.getDeletes().contains(feature.getQualifiedID());
	}
	
	public boolean hasUpdatedFeatures() {
		return !changes.getUpdates().isEmpty();
	}
	
	public boolean hasInsertedFeatures() {
		return !changes.getInserts().isEmpty();
	}
	
	public boolean hasDeletedFeatures() {
		return !changes.getDeletes().isEmpty();
	}
	
	public Set<CFeatureIdentifier> getUpdatedFeatureIds() {
		return unmodifiableSet(changes.getUpdates());
	}
	
	public Set<CFeatureIdentifier> getInsertedFeatureIds() {
		return unmodifiableSet(changes.getInserts());
	}
	
	public Set<CFeatureIdentifier> getDeletedFeatureIds() {
		return unmodifiableSet(changes.getDeletes());
	}
	
}
