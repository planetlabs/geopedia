/*
 *
 */
package com.sinergise.common.gis.feature.descriptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.property.GeometryPropertyType;
import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.naming.Identifiable;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;


/**
 * Class containing meta information about a layer/table, such as field names.
 * 
 * @author mkadunc
 */
public class CFeatureDescriptor extends AbstractAuxiliaryInfo implements Identifiable, Iterable<PropertyDescriptor<?>> {

	private static final long serialVersionUID = 1L;

	public static final String KEY_FEATURE_TITLE_EXPR = "FeatureTitleExpression";
	public static final String KEY_FEATURE_DESCRIPTION_EXPR = "FeatureDescriptionExpression";
	public static final String KEY_FEATURE_URL_EXPR = "FeatureLinkURLExpression";
	public static final String KEY_FEATURE_SHORTNAME_EXPR = "FeatureShortNameExpression";
	public static final String KEY_FEATURE_MAX_SUMMARY_ATTRS = "MaxSummaryAttrs";

	public static final String KEY_FEATURE_FETCH_GEOMETRY = "FetchGeometry";

	private int idFieldIndex = -1;
	private int geomFieldIndex = -1;
	private PropertyDescriptor<?>[] valueDescriptors;
	private Identifier featureTypeId;
	private PropertyDescriptor<Object> defaultPropertyDesc;

	/**
	 * @deprecated serialization only
	 */
	@Deprecated
	public CFeatureDescriptor() {
		// not used
	}

	/**
	 * @param featureTypeId - usually the same as layer name
	 */
	public CFeatureDescriptor(Identifier featureTypeId) {
		this.featureTypeId = featureTypeId;
		setTitle(featureTypeId.getLocalID());
		setDescription(featureTypeId.toString());
		defaultPropertyDesc = new PropertyDescriptor<Object>("", new PropertyType<Object>(PropertyType.VALUE_TYPE_TEXT));
	}

	@Override
	public Iterator<PropertyDescriptor<?>> iterator() {
		return Arrays.asList(valueDescriptors).iterator();
	}

	public int size() {
		return valueDescriptors == null ? 0 : valueDescriptors.length;
	}

	@Override
	public String getLocalID() {
		return featureTypeId.getLocalID();
	}

	public String getFeatureTypeName() {
		return getLocalID();
	}

	@Override
	public Identifier getQualifiedID() {
		return featureTypeId;
	}

	private transient Map<String, Integer> fieldIndexCache = new HashMap<String, Integer>();

	public int fieldIndex(String fieldName) {
		if (fieldIndexCache == null)
			fieldIndexCache = new HashMap<String, Integer>();

		Integer cached = fieldIndexCache.get(fieldName);

		if (cached == null || (cached.intValue() > -1 //also check if changed
			&& !valueDescriptors[cached.intValue()].getSystemName().equals(fieldName))) {
			cached = Integer.valueOf(-1);
			for (int i = 0; i < valueDescriptors.length; i++) {
				if (fieldName.equals(valueDescriptors[i].getSystemName())) {
					cached = Integer.valueOf(i);
					break;
				}
			}

			fieldIndexCache.put(fieldName, cached);
		}
		return cached.intValue();
	}

	public boolean hasGeometry() {
		return geomFieldIndex >= 0;
	}

	public int getMaxSummaryAttrs() {
		return data.getInt(KEY_FEATURE_MAX_SUMMARY_ATTRS, 5);
	}

	public boolean fetchGeometry() {
		return data.getBoolean(KEY_FEATURE_FETCH_GEOMETRY, true);
	}

	public CFeatureDescriptor createSubset(String[] subsetFieldNames) {
		if (subsetFieldNames == null) {
			return this;
		}

		CFeatureDescriptor subset = new CFeatureDescriptor(featureTypeId);

		int size = subsetFieldNames.length;
		int index = 0;
		subset.data = data;
		subset.defaultPropertyDesc = defaultPropertyDesc;

		PropertyDescriptor<?>[] pDescs = new PropertyDescriptor<?>[size];
		for (int i = 0; i < size; i++) {

			if ((index = fieldIndex(subsetFieldNames[i])) != -1) {

				if (index == geomFieldIndex) {
					subset.geomFieldIndex = i;

				} else if (index == idFieldIndex) {
					subset.idFieldIndex = i;
				}

				pDescs[i] = valueDescriptors[index];

			} else {
				throw new IllegalArgumentException("FeatureSet does not containt attribute: " + subsetFieldNames[i]);
			}
		}
		subset.setValueDescriptors(pDescs);
		return subset;
	}

	public CFeatureDescriptor createExtended(PropertyDescriptor<?>[] additionalProperties) {
		if (additionalProperties == null || additionalProperties.length == 0) {
			return this;
		}
		CFeatureDescriptor xtended = new CFeatureDescriptor(featureTypeId);

		int oldLen = valueDescriptors.length;
		int size = oldLen + additionalProperties.length;
		xtended.geomFieldIndex = geomFieldIndex;
		xtended.idFieldIndex = idFieldIndex;
		xtended.data = data;
		xtended.defaultPropertyDesc = defaultPropertyDesc;

		PropertyDescriptor<?>[] pDescs = new PropertyDescriptor<?>[size];
		for (int i = 0; i < oldLen; i++) {
			pDescs[i] = valueDescriptors[i];
		}
		for (int i = 0; i < additionalProperties.length; i++) {
			pDescs[oldLen + i] = additionalProperties[i];
		}
		xtended.setValueDescriptors(pDescs);
		return xtended;
	}


	public PropertyDescriptor<?> getValueDescriptor(String systemName) {
		for (PropertyDescriptor<?> pd : valueDescriptors) {
			if (pd.getSystemName().equals(systemName))
				return pd;
		}
		return null;
	}

	public int getValueIndex(String systemName) {
		return fieldIndex(systemName);
	}

	public PropertyDescriptor<?> getValueDescriptor(int propIndex) {
		return valueDescriptors[propIndex];
	}

	public PropertyDescriptor<?> getIdDescriptor() {
		return idFieldIndex < 0 ? null : getValueDescriptor(idFieldIndex);
	}

	@SuppressWarnings("unchecked")
	public PropertyDescriptor<? extends Geometry> getGeomDescriptor() {
		return geomFieldIndex < 0 ? null : (PropertyDescriptor<Geometry>)getValueDescriptor(geomFieldIndex);
	}

	public void setValueDescriptors(PropertyDescriptor<?>... props) {
		this.valueDescriptors = props;
		if (props == null) {
			return;
		}
		int gIdx = -1;
		for (int i = 0; i < props.length; i++) {
			props[i].setInfoInt(PropertyDescriptor.KEY_INDEX_IN_FEATURE, i);
			if (GeometryPropertyType.isGeometryProperty(props[i])) {
				gIdx = i;
			}
		}
		if (geomFieldIndex < 0 && gIdx >= 0) {
			setGeomIndex(gIdx);
		}
	}

	public List<PropertyDescriptor<?>> asList() {
		return Arrays.asList(valueDescriptors);
	}

	public PropertyDescriptor<Object> getPropertyDefaults() {
		return defaultPropertyDesc;
	}

	public void setPropertyDefaults(PropertyDescriptor<Object> defaultPropertyDesc) {
		this.defaultPropertyDesc = defaultPropertyDesc;
	}

	public int getGeomIndex() {
		return geomFieldIndex;
	}

	public void setGeomIndex(int geomFieldIndex) {
		this.geomFieldIndex = geomFieldIndex;
	}

	public int getIdIndex() {
		return idFieldIndex;
	}

	public void setIdIndex(int idFieldIndex) {
		this.idFieldIndex = idFieldIndex;
	}

	public boolean hasId() {
		return idFieldIndex >= 0;
	}
	
	public <R> PropertyDescriptor<R> createPD(String systemName, PropertyType<R> type){
		return CFeatureDescriptor.createPD(this, systemName, type);
	}
	
	public static <R> PropertyDescriptor<R> createPD(CFeatureDescriptor fdParent, String systemName, PropertyType<R> type){
		return new PropertyDescriptor<R>(fdParent == null ? null : fdParent.getPropertyDefaults(), systemName, type);
	}

}
