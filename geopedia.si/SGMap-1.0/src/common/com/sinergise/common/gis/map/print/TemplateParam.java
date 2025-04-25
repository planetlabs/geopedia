package com.sinergise.common.gis.map.print;

import java.io.Serializable;
import java.util.Collection;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.util.Util;

public final class TemplateParam  implements Serializable {
	public static final TemplateParam create(String name) {
		return new TemplateParam(name);
	}
	public static final TemplateParam TITLE = create("TITLE");
	public static final TemplateParam INDEX_TITLE = create("INDEX_TITLE");
	public static final TemplateParam PRINT_ATTRIBUTES = create("ATTS");
	public static final TemplateParam PRINT_GRAPHICS = create("MAP");
	
	private String paramName;
	private TemplateParam(String name) {
		super();
		if (name.startsWith(PrintParams.PREFIX_CUSTOM_PARAM)) {
			this.paramName = name;
		} else {
			this.paramName = PrintParams.PREFIX_CUSTOM_PARAM + name;
		}
	}
	
	public String getValue(PrintParams params) {
		return params.customParams.get(this);
	}

	public String getParamName() {
		return paramName;
	}
	
	public void setValue(PrintParams params, String value) {
		params.customParams.put(this, value);
	}
	
	@Override
	public String toString() {
		return paramName;
	}

	@Override
	public int hashCode() {
		return Util.safeHashCode(paramName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TemplateParam)) {
			return false;
		}
		TemplateParam other = (TemplateParam)obj;
		return Util.safeEquals(paramName, other.paramName);
	}

	public static String encode(Collection<CFeature> selectedFeatures) {
		return CFeatureIdentifier.encode(selectedFeatures);
	}
	
}
