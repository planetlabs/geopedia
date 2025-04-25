package com.sinergise.common.gis.editor;

import static com.sinergise.common.gis.feature.CFeatureUtils.getIdentifiers;
import static java.util.Collections.singleton;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.RepresentsFeature;

public class FeatureChanges implements Serializable, Iterable<CFeatureIdentifier> {

	private static final long serialVersionUID = -4670684934262931094L;
	
	private HashSet<CFeatureIdentifier> updated = new HashSet<CFeatureIdentifier>();
	private HashSet<CFeatureIdentifier> inserted = new HashSet<CFeatureIdentifier>();
	private HashSet<CFeatureIdentifier> deleted = new HashSet<CFeatureIdentifier>();

	@Override
	public Iterator<CFeatureIdentifier> iterator() {
		return getAll().iterator();
	}
	
	public void registerUpdate(RepresentsFeature feature) {
		registerUpdates(singleton(feature));
	}
	
	public void registerUpdates(Collection<? extends RepresentsFeature> features) {
		registerWithCollections(features, updated);
	}
	
	public void registerInsert(RepresentsFeature feature) {
		registerInserts(singleton(feature));
	}
	
	public void registerInserts(Collection<? extends RepresentsFeature> features) {
		registerWithCollections(features, inserted);
	}
	
	public void registerDelete(RepresentsFeature feataure) {
		registerDeletes(singleton(feataure));
	}
	
	public void registerDeletes(Collection<? extends RepresentsFeature> features) {
		registerWithCollections(features, deleted);
	}
	
	private static void registerWithCollections(Collection<? extends RepresentsFeature> features, Collection<CFeatureIdentifier> coll) {
		coll.addAll(getIdentifiers(features));
	}
	
	public Set<CFeatureIdentifier> getUpdates() {
		return updated;
	}
	
	public Set<CFeatureIdentifier> getInserts() {
		return inserted;
	}
	
	public Set<CFeatureIdentifier> getDeletes() {
		return deleted;
	}
	
	public Set<CFeatureIdentifier> getAll() {
		Set<CFeatureIdentifier> all = new HashSet<CFeatureIdentifier>(size());
		all.addAll(updated);
		all.addAll(inserted);
		all.addAll(deleted);
		return all;
	}
	
	public int size() {
		return updated.size() + inserted.size() + deleted.size();
	}
	
}
