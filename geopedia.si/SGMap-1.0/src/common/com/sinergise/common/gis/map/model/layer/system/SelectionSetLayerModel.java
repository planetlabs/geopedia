package com.sinergise.common.gis.map.model.layer.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.feature.RepresentsFeatureCollection;
import com.sinergise.common.gis.map.model.layer.LayerSpec;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.naming.Identifier;


public class SelectionSetLayerModel extends LayerSpec.Leaf {
	
	private static final long serialVersionUID = 1L;
	
//	private HashSet<RepresentsFeature> selectedFeatures = new HashSet<RepresentsFeature>();
	private List<HasFeatureRepresentations> collections = new ArrayList<HasFeatureRepresentations>();
	private RepresentsFeatureCollection defaultCollection = new RepresentsFeatureCollection();
	{
		collections.add(defaultCollection);
	}
	
	public SelectionSetLayerModel(String specId) {
		super(SystemLayersSource.INSTANCE, specId);
	}
	public boolean addSelected(RepresentsFeature featureRef) {
		checkReference(featureRef);
		return defaultCollection.add(featureRef);
	}
	
	public Set<Identifier> getDataSourceIDs() {
		HashSet<Identifier> ret = new HashSet<Identifier>();
		for (HasFeatureRepresentations coll : collections) {
			CollectionUtil.map(coll.getFeatures(), ret, new Function<RepresentsFeature, Identifier>() {
				@Override
				public Identifier execute(RepresentsFeature param) throws RuntimeException {
					return param.getQualifiedID().getDataSourceID();
				}
			});
		}		
		return ret;
	}
	
	public <T extends Collection<? super RepresentsFeature>> T getSelectedForFeaturesSource(String featuresSource, T retList) {
		for (HasFeatureRepresentations coll : collections) {
	    	for (RepresentsFeature ref : coll.getFeatures()) {
				if (featuresSource==null || featuresSource.equals(ref.getQualifiedID().getFeatureDataSourceName())) {
					retList.add(ref);
				}
			}
		}
		return retList;
	}
	
	public boolean removeSelected(RepresentsFeature featureRef) {
		return defaultCollection.remove(featureRef);
	}
	
	public boolean clearSelection() {
		if (isEmpty()) {
			return false;
		}
		collections.clear();
		defaultCollection.clear();
		collections.add(defaultCollection);
		return true;
	}
	
	@Override
	public Envelope getBoundingBox() {
		return null;
	}

	private static void checkReference(RepresentsFeature cfr) {
		CFeatureIdentifier id = cfr.getQualifiedID();
		if (id == null || id.getDataSourceID() == null) {
			throw new IllegalStateException("Feature reference without data source ID");
		}
	}
	public Collection<CFeatureIdentifier> getSelectedIds() {
		HashSet<CFeatureIdentifier> ret = new HashSet<CFeatureIdentifier>();
		for (HasFeatureRepresentations coll : collections) {
			ret.addAll(CFeatureUtils.getIdentifiers(coll.getFeatures()));
		}
		return ret;
	}
	public boolean isEmpty() {
		for (HasFeatureRepresentations coll : collections) {
			if (!coll.getFeatures().isEmpty()) {
				return false;
			}
			
		}
		return true;
	}
	public boolean addCollection(HasFeatureRepresentations selection) {
		boolean ret = true;
		if (removeCollection(selection)) {
			ret = false;
		}
		collections.add(selection);
		return ret;
	}
	public boolean removeCollection(HasFeatureRepresentations selection) {
		for (Iterator<HasFeatureRepresentations> it = collections.iterator(); it.hasNext();) {
			if (it.next() == selection) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	public boolean setCollection(HasFeatureRepresentations refs) {
		boolean ret = true;
		if (collections.size() == 2 && defaultCollection.isEmpty() && removeCollection(refs)) {
			ret = false;
		}
		clearSelection();
		addCollection(refs);
		return ret;
	}
}
