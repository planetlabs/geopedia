package com.sinergise.common.gis.feature;

import java.util.Arrays;
import java.util.Iterator;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.HasGeometry;
import com.sinergise.common.geometry.property.GeomAuxiliaryProperties;
import com.sinergise.common.gis.feature.auxprops.FeatureAuxiliaryProps;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.common.util.naming.IdentifiableEntityImpl;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.Property.QualifyingProperty;
import com.sinergise.common.util.property.Property.WritableProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;


public class CFeature extends IdentifiableEntityImpl implements HasGeometry, Iterable<Property<?>>, RepresentsFeature, HasEnvelope {
	private static final long serialVersionUID = 1L;

	private Property<?>[] values;

	public FeatureAuxiliaryProps auxProps;
	private CFeatureDescriptor descriptor;

	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	public CFeature() {
		//do nothing
	}
	
	public CFeature(CFeatureDescriptor desc) {
		this(desc, new Property<?>[desc.size()], null);
	}

	public CFeature(CFeatureDescriptor desc, String featureID) {
		this(desc, new Property<?>[desc.size()], featureID);
	}
	

	public CFeature(CFeatureDescriptor desc, Property<?>[] vals) {
		this(desc, vals, null);
	}

	public CFeature(CFeatureDescriptor desc, Property<?>[] vals, String featureID) {
		this.descriptor = desc;
		this.values = vals;

		if (featureID == null) {
			updateID();
		} 
		if (id == null) {
			setID(featureID);
		}
	}

	@Override
	public Iterator<Property<?>> iterator() {
		return Arrays.asList(values).iterator();
	}

	public void setID(String localID) {
		int idIdx = getDescriptor().getIdIndex();
		if (idIdx >= 0) {
			setPropertyValue(idIdx, CFeatureUtils.convertValue(localID, getDescriptor().getIdDescriptor()));
		}
		
		setID(new CFeatureIdentifier(getDescriptor(), localID));
	}
	
	public void setID(CFeatureIdentifier featureID) {
		this.id = featureID;
	}

	public void updateID() {
		if (values == null) {
			return;
		}
		if (descriptor.hasId() && getIdProperty() instanceof QualifyingProperty) {
			setID(((QualifyingProperty)getIdProperty()).getValueAsName());
		}
	}

	public FeatureAuxiliaryProps getAuxProps() {
		if (auxProps == null) {
			auxProps = new FeatureAuxiliaryProps();
		}
		return auxProps;
	}

	public void resetAuxProps() {
		auxProps = new FeatureAuxiliaryProps();
	}
	
	public String getShortName() {
		String sn = null;
		if (auxProps != null) {
			sn = auxProps.getInfoString(AbstractAuxiliaryInfo.KEY_SHORTNAME, null);
			if (sn != null) return sn;
		}
		if (getDescriptor() != null) {
			String snExpr = getDescriptor().getInfoString(CFeatureDescriptor.KEY_FEATURE_SHORTNAME_EXPR, null);
			if (snExpr != null) {
				sn = FeatureAuxiliaryProps.evaluateExpressionString(snExpr, this, "(" + getLocalID() + ")");
				if (auxProps != null) auxProps.setInfoString(AbstractAuxiliaryInfo.KEY_SHORTNAME, sn);
				if (sn != null) return sn;
			}
		}
		// fallback
		return getLocalID();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (int i = 0; i < getDescriptor().size(); i++) {
			PropertyDescriptor<?> desc = getDescriptor().getValueDescriptor(i);
			if (!first) buf.append(",");
			else first = false;
			buf.append(desc.getSystemName());
			buf.append("[").append(desc.getType().toString().charAt(0)).append("]:");
			buf.append(values[i]);
		}
		return buf.toString();
	}

	public Property<?> getIdProperty() {
		if (!getDescriptor().hasId()) {
			return null;
		}
		return getProperty(getDescriptor().getIdIndex());
	}

	@Override
	public String getLocalID() {
		if (id == null) {
			QualifyingProperty idProperty = (QualifyingProperty)getIdProperty();
			return idProperty != null ? idProperty.getValueAsName() : null;
		}
		return id.getLocalID();
	}

	@Override
	public CFeatureIdentifier getQualifiedID() {
		if (id == null) {
			id = new CFeatureIdentifier(getDescriptor(), getLocalID());
		}
		return (CFeatureIdentifier) id;
	}

	public CFeatureIdentifier getIdentifier() {
		return getQualifiedID();
	}

	public String getTitle() {
		String tit = null;
		if (auxProps != null) {
			tit = auxProps.getTitle();
			if (tit != null) return tit;
		}

		if (getDescriptor() != null) {
			String titExpr = getDescriptor().getInfoString(CFeatureDescriptor.KEY_FEATURE_TITLE_EXPR, null);
			if (titExpr != null) {
				tit = FeatureAuxiliaryProps.evaluateExpressionString(titExpr, this, null);
				if (auxProps != null) auxProps.setTitle(tit);
				if (tit != null) return tit;
			}
		}
		//fallback
		return getShortName();
	}

	public String getDescription() {
		String ret = null;
		if (auxProps != null) {
			ret = auxProps.getDescription();
			if (ret != null) return ret;
		}
		if (getDescriptor() != null) {
			String retExpr = getDescriptor().getInfoString(CFeatureDescriptor.KEY_FEATURE_DESCRIPTION_EXPR, null);
			if (retExpr != null) {
				ret = FeatureAuxiliaryProps.evaluateExpressionString(retExpr, this, null);
				if (auxProps != null) auxProps.setDescription(ret);
				if (ret != null) return ret;
			}
		}
		//fallback
		return getTitle();
	}

	@Override
	public Geometry getGeometry() {
		int gIdx = getDescriptor().getGeomIndex();
		if (gIdx < 0) throw new IllegalStateException("No geometry field");
		return getPropertyValue(getDescriptor().getGeomDescriptor());
	}
	
	public void setGeometry(Geometry geom) {
		setPropertyValue(getDescriptor().getGeomIndex(), geom);
	}
	
	public boolean hasGeometry() {
		return getDescriptor().hasGeometry() && getGeometry() != null;
	}

	@Override
	public Envelope getEnvelope() {
		if (hasGeometry()) {
			return getGeometry().getEnvelope();
		}
		if (auxProps != null) {
			GeomAuxiliaryProperties gap = auxProps.getGeomInfo();
			if (gap != null) {
				return gap.getMBR();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> String getStringValue(int idx) {
		if (idx < 0 || idx >= values.length) {
			throw new IndexOutOfBoundsException("Invalid property index: " + idx + " (size: " + values.length + ")");
		}
		return ((PropertyDescriptor<T>)descriptor.getValueDescriptor(idx)).getStringValue((Property<T>)values[idx]);
	}

	public <T> String getStringValue(PropertyDescriptor<T> pDesc) {
		return getStringValue(pDesc.getSystemName());
	}
	
	public <T> String getStringValue(String propertyName) {
		return getStringValue(descriptor.fieldIndex(propertyName));
	}


	public static void clearGeom(CFeature f) {
		int gIdx = f.getDescriptor().getGeomIndex();
		if (gIdx >= 0) {
			f.values[gIdx] = null;
		}
	}

	public int size() {
		return values == null ? getDescriptor().size() : values.length;
	}

	public boolean isNull(int index) {
		return values == null ? true : values[index] == null ? true : values[index].isNull();
	}
	
	public boolean isNull(PropertyDescriptor<?> pDesc) {
		return isNull(descriptor.fieldIndex(pDesc.getSystemName()));
	}

	public Property<?> getProperty(int i) {
		return values == null ? null : values[i];
	}
	
	@SuppressWarnings("unchecked")
	public <T> Property<T> getProperty(PropertyDescriptor<T> pDesc) {
		return (Property<T>)getProperty(descriptor.fieldIndex(pDesc.getSystemName()));
	}
	
	public Object getPropertyValue(int i) {
		return values == null ? null : values[i] == null ? null : values[i].getValue();
	}

	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(PropertyDescriptor<T> pDesc) {
		return (T)getPropertyValue(descriptor.fieldIndex(pDesc.getSystemName()));
	}
	
	public boolean hasValues() {
		return values != null;
	}

	@SuppressWarnings("unchecked")
	public <T> void setPropertyValue(int index, Object val) {
		if (values == null) {
			values = new Property<?>[size()];
		}
		PropertyDescriptor<T> pd = (PropertyDescriptor<T>)getDescriptor().getValueDescriptor(index);
		if (values[index] == null) {
			values[index] = pd.getType().createProperty((T)val);
			
		} else if (!(values[index] instanceof WritableProperty<?>)) { 
			throw new IllegalArgumentException("Property not mutable: " + pd);
			
		} else {
			((WritableProperty<T>)values[index]).setValue((T)val);
		}
	}

	public CFeatureDescriptor getDescriptor() {
		return descriptor;
	}

	public String getFeatureTypeName() {
		if (id == null) {
			return descriptor.getFeatureTypeName();
		}
		return getQualifiedID().getFeatureTypeName();
	}
	
	public CFeature copy() {
		CFeature clone = new CFeature(descriptor);
		clone.setID(getIdentifier());
		clone.auxProps = auxProps;
		
		for (int i=0; i<values.length; i++) {
			clone.setPropertyValue(i, getPropertyValue(i));
		}
		
		return clone;
	}

}
