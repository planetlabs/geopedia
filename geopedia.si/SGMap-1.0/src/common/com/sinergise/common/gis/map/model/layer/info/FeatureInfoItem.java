/*
 *
 */
package com.sinergise.common.gis.map.model.layer.info;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.io.Serializable;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.feature.auxprops.FeatureAuxiliaryProps;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.model.layer.FeaturesLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.gis.map.model.layer.LegendImageSource;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.common.util.property.Property;


public class FeatureInfoItem implements Serializable, Comparable<FeatureInfoItem>, RepresentsFeature, HasEnvelope {
	private static final long serialVersionUID = -4157000979582102510L;

	public CFeature f;

	public String layerName;

	/**
	 * Smaller value means better hit; square of the distance should be used for
	 * ordering by distance
	 */
	public double hitValue;

	public transient Layer layer;

	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	public FeatureInfoItem() {
	}

	public FeatureInfoItem(CFeature f, String layerName) {
		this(f, layerName, Double.POSITIVE_INFINITY);
	}

	public FeatureInfoItem(CFeature f, Layer layer) {
		this(f, layer, Double.POSITIVE_INFINITY);
	}

	public FeatureInfoItem(CFeature f, String layerName, double hitValue) {
		this.f = f;
		this.layerName = layerName;
		this.hitValue = hitValue;
	}

	public FeatureInfoItem(CFeature f, Layer layer, double hitValue) {
		this(f, layer.getLocalID(), hitValue);
		this.layer = layer;
	}

	@Override
	public int compareTo(FeatureInfoItem other) {
		if (other.hitValue == hitValue) return 0;
		if (hitValue < other.hitValue) return -1;
		return 1;
	}

	@Override
	public String toString() {
		return hitValue + " " + layerName + " " + f;
	}

	@Override
	public Envelope getEnvelope() {
		if (f == null) {
			return null;
		}
		return f.getEnvelope();
	}

	public String getTitle() {
		String title = f.getTitle();
		if (!isNullOrEmpty(title)) return title;
		
		String descTitle = f.getDescriptor() == null ? null : f.getDescriptor().getTitle();
		if (isNullOrEmpty(descTitle)) {
			descTitle = layer != null ? layer.getTitle() : layerName;
		}

		if (f.getLocalID() != null) return descTitle + " : " + f.getLocalID().toString();
		return descTitle;
	}

	public double getFeatureMinScale() {
		double min = f.getAuxProps().getInfoDouble(FeatureAuxiliaryProps.KEY_MIN_SCALE, -1);
		if (min > 0) return min;
		if (layer != null) return layer.getBounds().minScale();
		return min;
	}

	public double getFeatureMaxScale() {
		double max = f.getAuxProps().getInfoDouble(FeatureAuxiliaryProps.KEY_MAX_SCALE, Double.POSITIVE_INFINITY);
		if (max > 0 && !Double.isInfinite(max)) return max;
		if (layer != null) return layer.getBounds().maxScale();
		return max;
	}

	/**
	 * Preferred scale for display of feature (if set), preffered scale of layer
	 * (if set) or minimum scale (see {@link #getFeatureMinScale})
	 * 
	 * @return
	 */
	public double getFeaturePrefScale() {
		double fSc = f.getAuxProps().getInfoDouble(FeatureAuxiliaryProps.KEY_PREF_SCALE, -1);
		if (fSc > 0) return fSc;

		if (layer != null) {
			fSc = layer.getDefaultScale();
			if (fSc > 0) return fSc;
		}

		return getFeatureMinScale();
	}

	public String getFeatureLegendImageURL() {
		String ftrLeg = f.getAuxProps().getInfoString(FeatureAuxiliaryProps.KEY_LEGEND_IMAGE, null);
		if (ftrLeg == null && layer != null && layer.getSource().supports(LayersSource.CAPABILITY_LEGEND_IMAGE)) {
			LegendImageSource src = ((LegendImageSource) layer.getSource());
			return src.getLegendImageURL(layer, new DimI(24, 24), true);
		}
		return null;
	}

	public void updateTransient(MapViewContext context) {
		if (f == null) return;
		if (layer == null) layer = context.layers.findByName(layerName);
		
		if (layer instanceof FeaturesLayer) CFeatureUtils.updateTransientWithFeaturesSource(f, ((FeaturesLayer)layer).getFeaturesSource());
		CFeatureUtils.updateTransientWithLayer(f, layer);
	}

	public void setTitle(String title) {
		if (f == null) return;
		f.getAuxProps().setTitle(title);
	}

	public Object getValue(String fieldName) {
		if (f == null || f.getDescriptor() == null || !f.hasValues()) return null;
		int id = f.getDescriptor().fieldIndex(fieldName);
		Property<?> val = null;
		if(id>=0) val = f.getProperty(id);		
		if (val == null) return null;
		return val.getValue();
	}
	
	
	public static final void clearGeom(FeatureInfoItem[] results) {
		if (results == null) return;
		for (FeatureInfoItem itm : results) {
			clearGeom(itm);
		}
	}

	protected static void clearGeom(FeatureInfoItem itm) {
		if (itm==null || itm.f == null || itm.f.getDescriptor().fetchGeometry()) {
			return;
		}
		CFeature.clearGeom(itm.f);
	}
	
	@Override
	public String getLocalID() {
		return f.getLocalID();
	}
	
	@Override
	public CFeatureIdentifier getQualifiedID() {
		return f.getQualifiedID();
	}

	public String getStringValue(String propertyName) {
		return f.getStringValue(propertyName);
	}
	
	public Layer getLayer() {
		return layer;
	}
}
