package com.sinergise.common.gis.feature;

import java.util.Collection;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.string.Escaper;



public class CFeatureIdentifier extends EntityIdentifier implements RepresentsFeature {
	private static final long serialVersionUID = 1L;

	public static String encode(String featureTypeName, String featureID) {
		return qualifierEscaper.escapeComponent(featureTypeName)+"."+featureID;
	}
	public static CFeatureIdentifier decode(FeaturesSource datasource, String featureIDString) {
		int idx=qualifierEscaper.specialCharIndex(featureIDString,'.');
		return new CFeatureIdentifier(datasource,
				qualifierEscaper.unescapeComponent(featureIDString.substring(0, idx)),
				featureIDString.substring(idx+1));
	}
	public static CFeatureIdentifier[] decode(FeaturesSource datasource, String[] featureIDStrings) {
		if (featureIDStrings==null) return null;
		CFeatureIdentifier[] ret= new CFeatureIdentifier[featureIDStrings.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i]=decode(datasource, featureIDStrings[i]);
		}
		return ret;
	}
	

	public static <T extends RepresentsFeature> String encode(T[] selectedFeatures) {
		String[] names=new String[selectedFeatures.length];
		for (int i = 0; i < names.length; i++) {
			names[i]=selectedFeatures[i].getQualifiedID().encode();
		}
		return WMSUtil.encodeArray(names);
	}
	
	public static <T extends RepresentsFeature> String encode(Collection<? extends RepresentsFeature> selectedFeatures) {
		String[] names=new String[selectedFeatures.size()];
		int i = 0;
		for (RepresentsFeature f : selectedFeatures) {
			names[i++] = f.getQualifiedID().encode();
		}
		return WMSUtil.encodeArray(names);
	}
	
	protected static final Escaper qualifierEscaper=new Escaper('\\',new char[]{'.'});

	private static final String validate(String localID) {
		if (localID == null) return null;
		if (localID.endsWith(".0")) localID = localID.replace(".0", ""); //workaround for giselle returning double values for primary keys
		if (localID.indexOf('.')>=0) throw new IllegalArgumentException("Invalid localID, contains '.': "+localID);
		return localID;
	}
	
	/**
	 * @deprecated serialization only
	 */
	@Deprecated
	public CFeatureIdentifier() {
		//not used
	}
	
	public CFeatureIdentifier(CFeatureDescriptor dataset) {
		this(dataset, null);
	}
	
	public CFeatureIdentifier(CFeatureDescriptor dataset, String featureID) {
		super(dataset.getQualifiedID(), validate(featureID));
	}
	
	public CFeatureIdentifier(FeatureDataLayer layer) {
		this(layer, null);
	}
	
	public CFeatureIdentifier(FeatureDataLayer layer, String featureID) {
		this(layer.getFeaturesSource(), layer.getFeatureTypeName(), validate(featureID));
	}
	
	public CFeatureIdentifier(FeaturesSource source, String featureTypeName, String featureID) {
		super(new Identifier(source==null?Identifier.ROOT:source.getQualifiedID(),featureTypeName),validate(featureID));
	}
	
	@Override
	public CFeatureIdentifier getQualifiedID() {
		return this;
	}
	
	public String getFeatureTypeName() {
		return parent.getLocalID();
	}

	public String getFeatureDataSourceName() {
		if (parent.isEmpty()) return null;
		return parent.getParent().getLocalID();
	}
	
	public String encode() {
		return encode(getParent().getLocalID(), getLocalID());
	}
	
	public Identifier getFeatureTypeID() {
		return getParent();
	} 
	
	public Identifier getDataSourceID() {
		return getFeatureTypeID().getParent();
	}
	
	public boolean bindToFeatureType(final Identifier featureTypeID) {
		if (!isBound()) {
			bindTo(featureTypeID);
			
		} else if (!getFeatureTypeID().isBound()) {
			bindToFeaturesSource(featureTypeID.getParent());
		}
		else return false;
		
		return true;
	}
	public boolean bindToFeaturesSource(Identifier featuresSourceID) {
		if (!isBound()) throw new IllegalStateException("CFeatureIdentifier cannot be bound to featuresSource when featureType is not specified");
		if (getFeatureTypeID().isBound()) return false;
		
		getFeatureTypeID().bindTo(featuresSourceID);
		return true;
	}
}
