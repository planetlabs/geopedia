package com.sinergise.common.gis.feature;

import static com.sinergise.common.gis.feature.auxprops.FeatureAuxiliaryProps.evaluateExpressionString;
import static com.sinergise.common.util.collections.CollectionUtil.first;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_BOOLEAN;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_LONG;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_REAL;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_TEXT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sinergise.common.geometry.property.GeometryPropertyType;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.lang.TypeUtil;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.common.util.string.StringUtil;

/**
 * @author tcerovski
 *
 */
public class CFeatureUtils {
	public static final String DEFAULT_HISTORY_KEY_PARAM_FEATURE = "feature";
	
	public static class PropertyDisplayData<T> {
		private PropertyDescriptor<T> desc;
		private Property<T> valueUsed; 
		private String valueAsString;
		public String title;
		public String link;
		public String tooltip;
		
		public PropertyDisplayData(PropertyDescriptor<T> desc, Property<T> valueUsed) {
			this.desc = desc;
			this.valueUsed = valueUsed;
			title = desc.getTitle();
		}
		
		public PropertyDescriptor<T> getDesc() {
			return desc;
		}

		public String getValue() {
			return valueAsString;
		}

		public void setValue(String value) {
			this.valueAsString = value;
		}
		
		public T getValueUsed() {
			return valueUsed == null ? null : valueUsed.getValue();
		}
		
		public Property<T> getPropertyUsed() {
			return valueUsed;
		}
	}
	
	public static abstract class PropertyDisplayBuilder<T> {
		protected String historyKey = DEFAULT_HISTORY_KEY_PARAM_FEATURE;
		private int maxProperties = Integer.MAX_VALUE;
		
		private Iterable<PropertyDescriptor<?>> allProperties;
		private List<PropertyDescriptor<?>> builtProperties = Collections.emptyList();

		public PropertyDisplayBuilder(Iterable<PropertyDescriptor<?>> allProperties) {
			this.allProperties = allProperties;
		}
		
		public void setHistoryKey(String historyKey) {
			this.historyKey = historyKey;
			builtProperties.clear();
		}
		
		public void setMaxProperties(int maxProperties) {
			this.maxProperties = maxProperties;
			builtProperties.clear();
		}
		
		public List<PropertyDescriptor<?>> getProperties() {
			if (builtProperties.isEmpty()) {
				buildProps();
			}
			return Collections.unmodifiableList(builtProperties);
		}
		
		private void buildProps() {
			builtProperties = getAllPropertiesByImportance();
			if (maxProperties > 0) {
				CollectionUtil.reduceList(builtProperties, maxProperties);
			}
			Collections.sort(builtProperties, new PropertyDescriptor.OrderComparator());
		}

		public List<PropertyDescriptor<?>> getAllPropertiesByImportance() {
			ArrayList<PropertyDescriptor<?>> allProps = new ArrayList<PropertyDescriptor<?>>();
			for (PropertyDescriptor<?> pd : allProperties) {
				if (!pd.isHidden()) {
					allProps.add(pd);
				}
			}
			
			Collections.sort(allProps, new PropertyDescriptor.ImportanceComparator());
			return allProps;
		}
		
		
		public PropertyDisplayData<?>[] getValues(T feature) {
			if (builtProperties.isEmpty()) {
				buildProps();
			}
			PropertyDisplayData<?>[] ret = new PropertyDisplayData[builtProperties.size()];
			for (int i = 0; i < builtProperties.size(); i++) {
				ret[i] = getPropertyValue(feature, builtProperties.get(i));
			}
			return ret;
		}

		public abstract <P> PropertyDisplayData<P> getPropertyValue(T feature, PropertyDescriptor<P> propertyDescriptor);

		public PropertyDescriptor<?> getPropertyForSystemName(String att) {
			for (PropertyDescriptor<?> pd : allProperties) {
				if (pd.getSystemName().equals(att)) {
					return pd;
				}
			}
			return null;
		}
	}
	
	public static class FeatureInfoDisplayBuilder extends PropertyDisplayBuilder<CFeature> {
		private CFeatureDescriptor fDesc;
		
		public FeatureInfoDisplayBuilder(CFeatureDescriptor descriptor) {
			super(descriptor);
			this.fDesc = descriptor;
		}
		
		public void limitForSummary() {
			setMaxProperties(Math.min(fDesc.getMaxSummaryAttrs(), fDesc.size()));
		}
		@Override
		public <P> PropertyDisplayData<P> getPropertyValue(CFeature feature, PropertyDescriptor<P> pd) {
			return getPropertyDisplayData(feature, pd, historyKey, false);
		}
	}

	private CFeatureUtils() {
		//hide constructor
	}
	
	public static String[] getNonHiddenPropertyNames(CFeatureDescriptor featureDesc) {
		List<String> props = new ArrayList<String>();
		for (PropertyDescriptor<?> pd : featureDesc) {
			if(!pd.isHidden()) {
				props.add(pd.getSystemName());
			}
		}
		//check if ID field is present and add it if not
		if(featureDesc.getIdIndex() >= 0) {
			String idField = featureDesc.getIdDescriptor().getSystemName();
			if(!props.contains(idField)) {
				props.add(featureDesc.getIdIndex(), idField);
			}
		}
		
		return props.toArray(new String[props.size()]);
	}
	
	public static String[] getPropertyNamesForQuery(CFeatureDescriptor featureDesc) {
		boolean fetchGeom = featureDesc.fetchGeometry();
		
		Set<String> props = new LinkedHashSet<String>();
		for (int i = 0; i < featureDesc.size(); i++) {
			if (!ignoreProperty(fetchGeom, featureDesc, i)) {
				String propName = featureDesc.getValueDescriptor(i).getSystemName();
				for (String propNamePart : propName.split(",")) { //handle composite property names (PointXY Layers)
					props.add(propNamePart.trim());
				}
			}
		}
		
		return props.toArray(new String[props.size()]);
	}
	
	public static String[] removeIgnoredProperties(CFeatureDescriptor featureDesc, String[] properties) {
		if (properties == null) {
			return properties;
		}
		
		Set<String> filtered = new LinkedHashSet<String>();
		
		//ensure to have ID
		if (featureDesc.getIdIndex() > -1) {
			filtered.add(featureDesc.getIdDescriptor().getSystemName());
		}
		for (String propName : properties) {
			int idx = featureDesc.fieldIndex(propName);
			if (idx > -1 && !ignoreProperty(true, featureDesc, idx)) {
				filtered.add(propName);
			}
		}
		
		return filtered.toArray(new String[filtered.size()]);
	}
	
	private static boolean ignoreProperty(boolean fetchGeom, CFeatureDescriptor featureDesc, int propIdx) {
		if (propIdx < 0) {
			return true;
		}
		//ID field should not be ignored
		if (propIdx == featureDesc.getIdIndex()) {
			return false;
		}
		
		PropertyDescriptor<?> pd = featureDesc.getValueDescriptor(propIdx);
		if (pd == null) {
			return true;
		}
		
		if (pd.isValueType(GeometryPropertyType.GENERIC_GEOMETRY)) {
			return !fetchGeom;
		}
		return pd.ignore();
	}
	
	public static String[] getAllPropertyNames(CFeatureDescriptor featureDesc) {
		String[] ret = new String[featureDesc.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = featureDesc.getValueDescriptor(i).getSystemName();
		}
		return ret;
	}
	
	public static List<CFeatureIdentifier> toFeatureIds(Collection<? extends RepresentsFeature> features) {
		if (features == null) {
			return Collections.emptyList();
		}
		List<CFeatureIdentifier> ids = new ArrayList<CFeatureIdentifier>(features.size());
		for (RepresentsFeature f : features) {
			ids.add(f.getQualifiedID());
		}
		return ids;
	}
	
	public static CFeatureIdentifier[] toFeatureIdsArray(Collection<? extends RepresentsFeature> features) {
		List<CFeatureIdentifier> list = toFeatureIds(features);
		return list.toArray(new CFeatureIdentifier[list.size()]); 
	}
	
	public static CFeatureIdentifier[] toFeatureIdsArray(RepresentsFeature[] features) {
		CFeatureIdentifier[] ids = new CFeatureIdentifier[features.length];
		
		for (int i=0; i<features.length; i++) {
			ids[i] = features[i].getQualifiedID();
		}
		return ids; 
	}
	
	public static CFeatureCollection toFeatureList(FeatureInfoCollection info) {
		CFeatureCollection list = new CFeatureCollection(info.getItemCount());
		for (int i=0; i<info.getItemCount(); i++) {
			list.add(info.getItem(i).f);
		}
		return list;
	}

	public static void copy(CFeature src, CFeature target) {
		int idx = 0;
		for (Property<?> p : src) {
			target.setPropertyValue(idx++, p.getValue());
		}
		if (src.auxProps == null) target.auxProps = null;
		else target.getAuxProps().setFrom(src.auxProps);
	}

	public static Envelope getMBR(Iterable<? extends HasEnvelope> features) {
		if (features == null) {
			return Envelope.getEmpty();
		}
		return HasEnvelope.Util.getMBR(features);
	}
	
	public static boolean updateTransientWithFeaturesSource(CFeature f, FeaturesSource featuresSource) {
		if (f == null) return false;
		
		f.updateID();
		
		if (featuresSource == null) return false;
		
		CFeatureIdentifier cfi = f.getIdentifier();
		if (cfi == null) return false;
		
		return cfi.bindToFeaturesSource(featuresSource.getQualifiedID());
	}
	
	public static boolean updateTransientWithLayer(CFeature f, Layer lyr) {
		if (f == null) return false;
		
		f.updateID();
		
		if (lyr == null) return false;
		
		CFeatureIdentifier cfi = f.getIdentifier();
		if (cfi == null) return false;
		
		return cfi.bindToFeatureType(lyr.getSpec().getQualifiedID());
	}
	
	public static Query[] createQueriesForFeatures(Collection<? extends RepresentsFeature> featureIDs) {
		final Query[] ret = new Query[featureIDs.size()];
		int idx = 0;
		for (RepresentsFeature ref : featureIDs) {
			try {
				CFeatureIdentifier fId = ref.getQualifiedID();
				ret[idx++] = new Query(fId.getFeatureTypeName(), new IdentifierOperation(fId.getLocalID()));
			} catch(InvalidFilterDescriptorException e) {
				throw new RuntimeException(e);
			}
		}
		return ret;
	}

	public static boolean shouldShowAsLink(CFeatureDescriptor fDesc, PropertyDescriptor<?> pDesc) {
		if (pDesc.showAsLink()) {
			return true;
		}
		if (fDesc == null) {
			return false;
		}
		PropertyDescriptor<?> idDesc = fDesc.getIdDescriptor();
		if (idDesc == null) {
			return false;
		}
		return Util.safeEquals(pDesc.getSystemName(), idDesc.getSystemName());
	}

	public static String getEvaluatedPropertyValue(CFeature f, PropertyDescriptor<?> pd) {
		String value = f.getStringValue(pd);
		if (pd.getValueExpr() != null) {
			value = evaluateExpressionString(pd.getValueExpr(), f, value);
		}
		return value;
	}

	public static String getLinkUrlOrNull(CFeature feature, PropertyDescriptor<?> pd, boolean autoLinkToSelf, String historyKey) {
		if (shouldShowAsLink(feature.getDescriptor(), pd)) {
			final String linkUrlExpr = pd.getLinkUrlExpr();
			if (autoLinkToSelf && shouldLinkToSelf(linkUrlExpr)) {
				return "#"+historyKey+"="+feature.getFeatureTypeName()+":"+feature.getLocalID();
			}
			return evaluateExpressionString(linkUrlExpr, feature, "");
		}
		return null;
	}
	
	public static String getLinkUrlOrNull(PropertyDescriptor<?> pd, String selfLinkUrl) {
		if (shouldShowAsLink(null, pd)) {
			final String linkUrlExpr = pd.getLinkUrlExpr();
			if (shouldLinkToSelf(linkUrlExpr)) {
				return selfLinkUrl;
			}
			return evaluateExpressionString(linkUrlExpr, null, "");
		}
		return null;
	}
	
	private static boolean shouldLinkToSelf(final String linkUrlExpr) {
		return StringUtil.isNullOrEmpty(linkUrlExpr);
	}

	public static Collection<CFeatureIdentifier> getIdentifiers(Iterable<? extends RepresentsFeature> selectedFeatures) {
		return CollectionUtil.map(selectedFeatures, new HashSet<CFeatureIdentifier>(), new Function<RepresentsFeature, CFeatureIdentifier>(){
			@Override
			public CFeatureIdentifier execute(RepresentsFeature param) {
				return param.getQualifiedID();
			}
		});
	}

	public static <T> PropertyDisplayData<T> getPropertyDisplayData(CFeature f, PropertyDescriptor<T> pd, String historyKey, boolean autoLinkToSelf) {
		PropertyDisplayData<T> ret = new PropertyDisplayData<T>(pd, f.getProperty(pd));
		ret.setValue(getEvaluatedPropertyValue(f, pd)); 
		ret.link = StringUtil.trimNullEmpty(getLinkUrlOrNull(f, pd, autoLinkToSelf, historyKey));
		if (pd.getTooltipExpr() != null) {
			ret.tooltip = evaluateExpressionString(pd.getTooltipExpr(), f, "");
		}
		return ret;
	}
	
	public static <T> PropertyDisplayData<T> getPropertyDisplayData(PropertyDescriptor<T> pd, Property<T> prop, String selfLinkUrl) {
		PropertyDisplayData<T> ret = new PropertyDisplayData<T>(pd, prop);
		ret.setValue(pd.getStringValue(prop)); 
		ret.link = StringUtil.trimNullEmpty(CFeatureUtils.getLinkUrlOrNull(pd, selfLinkUrl));
		if (pd.getTooltipExpr() != null) {
			ret.tooltip = evaluateExpressionString(pd.getTooltipExpr(), null, "");
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T convertValue(Object value, PropertyDescriptor<T> desc) {
        PropertyType<T> type = desc.getType();
        if (value == null) {
			return null;
        }
		if (type.isType(VALUE_TYPE_BOOLEAN)) {
			return (T)TypeUtil.toBoolean(value);
		}
		if (type.isType(VALUE_TYPE_LONG)) {
			return (T)TypeUtil.toLong(value);
		}
		if (type.isType(VALUE_TYPE_REAL)) {
			return (T)TypeUtil.toDouble(value);
		}
		if (type.isType(VALUE_TYPE_TEXT)) {
			return (T)TypeUtil.toString(value);
		}
		return (T)value;
	}
	
	public static Collection<CFeature> extractCFeatures(HasFeatureRepresentations fRep) {
		List<CFeature> cFeatures = new ArrayList<CFeature>();
		for(RepresentsFeature repF : fRep.getFeatures()) {
			if(repF instanceof CFeature) {
				cFeatures.add((CFeature)repF);
			}
		}
		return cFeatures;
	}

	public static boolean areAllCFeatures(HasFeatureRepresentations fRep) {
		for(RepresentsFeature rf : fRep.getFeatures()) {
			if(!(rf instanceof CFeature)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean hasOnlyThisCFeature(HasFeatureRepresentations fRep, CFeature cFeature) {
		return getCFeatureIfHasExactlyOne(fRep) == null ? false : getCFeatureIfHasExactlyOne(fRep).equals(cFeature);
	}
	
	public static CFeature getCFeatureIfHasExactlyOne(HasFeatureRepresentations fRep) {
		return hasExactlyOneCFeature(fRep) ? (CFeature) first(fRep.getFeatures()) : null;
	}
	
	public static boolean hasExactlyOneCFeature(HasFeatureRepresentations fRep) {
		return (fRep.getFeatures().size() == 1 && first(fRep.getFeatures()) instanceof CFeature);
	}

}
