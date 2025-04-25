/*
 *
 */
package com.sinergise.common.gis.feature.auxprops;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.property.GeomAuxiliaryProperties;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;

/**
 * Auxiliary values for each feature. Auxiliary values are those that do not have a separate field in the table, but can be (and are) calculated on the server side for each feature in the result collection.
 * 
 * @author mkadunc
 */
public class FeatureAuxiliaryProps extends AbstractAuxiliaryInfo {
	
	private static final long serialVersionUID = 1L;
	
    public static final String KEY_REFERENCED_BY = "ReferencedBy";
    
    public static final String KEY_LAST_UPDATE = "LastUpdate";
    public static final String KEY_VALID_TO = "ValidTo";
    public static final String KEY_VALID_FROM = "ValidFrom";
    
    public static final String KEY_GEOMINFO = "GeomInfo";
    
    public static final String KEY_MIN_SCALE = "MinScale";
    public static final String KEY_MAX_SCALE = "MaxScale";
    public static final String KEY_PREF_SCALE = "PreferredScale";
    
    public static final String KEY_LEGEND_IMAGE = "LegendImage";

	public static final String KEY_DETAILS_URL = "FeatureDetailsURL";
    
	public FeatureAuxiliaryProps() {
		super();
	}
	
    public FeatureAuxiliaryProps(FeatureAuxiliaryProps other) {
    	super(other);
	}

	public FeatureTransactionProperties getLastUpdate() {
    	checkWrappers();
        FeatureTransactionProperties ret=(FeatureTransactionProperties)wrappersCache.get(KEY_LAST_UPDATE);
        if (ret==null) {
            ret=new FeatureTransactionProperties(data.getState(KEY_LAST_UPDATE, true));
            wrappersCache.put(KEY_LAST_UPDATE, ret);
        }
        return ret;
    }
    
    public GeomAuxiliaryProperties getGeomInfo() {
    	return GeomAuxiliaryProperties.getFrom(this, KEY_GEOMINFO);
    }
    
    public StateGWT getState(String key, boolean createIfNull) {
    	return data.getState(key, createIfNull);
    }
    
    public void putState(String key, StateGWT state) {
    	data.putState(key, state);
    }
    
	public static void updateAuxVals(CFeature f) {
		if (f.getDescriptor().hasGeometry()) {
			try {
				Geometry geo = f.getGeometry();
				if (geo!=null) {
					f.getAuxProps().getGeomInfo().setMBR(geo.getEnvelope());
				} else {
					f.getAuxProps().getGeomInfo().setMBR(Envelope.getEmpty()); //set empty envelope to prevent fetching null geometries again
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	
       	setAuxExpr(f.getDescriptor(), f, CFeatureDescriptor.KEY_FEATURE_SHORTNAME_EXPR, 	 AbstractAuxiliaryInfo.KEY_SHORTNAME);
       	setAuxExpr(f.getDescriptor(), f, CFeatureDescriptor.KEY_FEATURE_URL_EXPR, 	     FeatureAuxiliaryProps.KEY_DETAILS_URL);
        setAuxExpr(f.getDescriptor(), f, CFeatureDescriptor.KEY_FEATURE_DESCRIPTION_EXPR, AbstractAuxiliaryInfo.KEY_DESCRIPTION);
        setAuxExpr(f.getDescriptor(), f, CFeatureDescriptor.KEY_FEATURE_TITLE_EXPR, 		 AbstractAuxiliaryInfo.KEY_TITLE);
    }
	
	public static void setAuxExpr(CFeatureDescriptor cDesc, CFeature ret, String exprKeyInDesc, String evaluatedKeyInFeatureAux) {
    	String expr = cDesc.getInfoString(exprKeyInDesc, null);
    	if (expr == null) {
    		return;
    	}
    	String val = evaluateExpressionString(expr, ret, null);
    	if (val!=null) {
    		ret.getAuxProps().setInfoString(evaluatedKeyInFeatureAux, val);
    	}
	}
	
	public static String evaluateExpressionString(final String exp, final CFeature f, final String defaultValue) {
		if (f == null || StringUtil.isNullOrEmpty(exp)) {
			return exp;
		}
		String ret = exp;
		for (int i = 0; i < f.getDescriptor().size(); i++) {
			String replaceStr = toEscapedVar(f.getDescriptor().getValueDescriptor(i).getSystemName());
			if (ret.contains(replaceStr)) {
				String value = null;
				if (!f.isNull(i)) {
					value = f.getProperty(i).toString();
				}
				if (value == null && defaultValue != null) {
					value = defaultValue;
				} else if (value == null) {
					continue;
				}
				ret = ret.replace(replaceStr, value);
			}
		}
		return ret;
	}
	
	public static String replaceExpressionVar(final String exp, final String varname, final String value) {
		String escVar = toEscapedVar(varname);
		if (!exp.contains(escVar)) {
			return exp;
		}
		return exp.replace(escVar, value);
	}
	
	public void setFrom(FeatureAuxiliaryProps auxProps) {
		data.setFrom(auxProps.data, true);
	}
}
