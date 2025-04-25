package com.sinergise.geopedia.core.entities;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.common.util.ArrayUtils;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.symbology.AbstractSymbologyUtils;

/*
 * Example of table_properties:
 * 
 * source layer: 3213 (Združeni habitatni tipi - šifrant)
 * target layer: 2988 (Vrste)
 * join table
 * field 7229 belongs to join table and references source layer
 * field 7230 belongs to join table and references target layer
 * 
 * source layer: 3213 (Združeni habitatni tipi - šifrant)
 * target layer: 3214 (Združeni habitatni tipi)
 * no join table
 * field 7222 belongs to target layer and references source layer
 * 
 <?xml version="1.0" encoding="UTF-8"?>
 <states>
 <state isQueryable="false" >
 <manyLinks>
 <L2988 label="Vrste" sourceReference="7229" queryTable="3222" targetReference="7230"/>
 <L3214 label="ZHT"   sourceReference="7222" queryTable="3214"/>
 </manyLinks>
 </state>
 </states>
 */

public class Table extends AbstractEntityWithDescription {

	private static final long serialVersionUID = 1L;
	public static final String PROP_INCLUDE_IN_TOTAL_FULLTEXT = "includeInTotalFullText";
	public static final String PROP_ALLOW_EXPORT = "allowDataExport";
	public static final String PROP_MANY_LINKS = "manyLinks";
	public static final String PROP_MINSCALE = "minscale";
	public static final String PROP_MAXSCALE = "maxscale";
	public static final String PROP_RENDERASRASTER = "RenderRaster";
	public static final String PROP_IS_QUERYABLE = "isQueryable";

	public GeomType geomType = GeomType.POINTS;

	public long lastDataWrite;
	public long lastMetaChange;
	public int public_perms;
	
	private String repTextJS;

	public Envelope envelope = Envelope.getEmpty();
	public Field[] fields = new Field[0];

	public String styleJS;

	public StateGWT properties;

	/** Permissions this user has (or -1, if user isn't logged in) */
	@Deprecated
	public int user_perms;

	/** for Repo and Metadata only, don't touch!!! */
	transient public long _lastLoadTime;

	/** used for importing, ignore everywhere else */
	transient public String sysId;

	public Table() {
		repTextJS=null;
		descRawHtml = "";
	}

	@Override
	public Table clone(DataScope scope) {
		Table cloned = new Table();
		cloned.dataScope=scope;
		cloned.id = getId();
		cloned.setName(getName());
		cloned.geomType = getGeomType();

		if (scope == DataScope.ALL || scope == DataScope.MEDIUM) {
			cloned.descDisplayableHtml = descDisplayableHtml;
			cloned.lastDataWrite = lastDataWrite;
			cloned.lastMetaChange = lastMetaChange;
			cloned.public_perms = public_perms;
			cloned.user_perms = user_perms;
			cloned.setDeleted(isDeleted());
			cloned.envelope = new Envelope(envelope);

			if (fields != null) {
				cloned.fields = new Field[fields.length];
				for (int i = 0; i < fields.length; i++) {
					cloned.fields[i] = fields[i].clone(scope);
				}
			}
		}
		
		if (scope == DataScope.ALL) {
			cloned.descRawHtml = descRawHtml;
			cloned.styleJS = styleJS;
			cloned.repTextJS = repTextJS;

			if (properties != null && !properties.isEmpty()) {
				cloned.properties = new StateGWT();
				cloned.properties.setFrom(properties, true);
			} else {
				cloned.properties = null;
			}
			
		}
		return cloned;
	}
	
	public boolean isQueryable() {
		if (properties==null) return true;
		return properties.getBoolean(PROP_IS_QUERYABLE, true);
	}

	public Feature createEmptyFeature() {
		Feature feature = new Feature();
		feature.tableDataTs = lastDataWrite;
		feature.tableId = id;
		feature.geomType = geomType;
		feature.fields = new Field[fields.length];
		feature.properties = new Property<?>[fields.length];
		ArrayUtils.arraycopy(fields, 0, feature.fields, 0, fields.length);
		for (int i = 0; i < fields.length; i++) {
			String defValue = fields[i].defaultValueString;
			if (defValue != null && defValue.length() > 0) {
				feature.properties[i] = PropertyUtils.forField(fields[i]);
			}
		}

		return feature;
	}

	public void setMetaChange(long lastTs) {
		lastMetaChange = lastTs;

		Field[] fields = getFields();
		if (fields == null)
			return;

		for (int i = 0; i < fields.length; i++) {
			fields[i].lastTableMeta = lastTs;
		}
	}

	public Field getFieldById(int fieldId) {
		Field[] fields = getFields();
		if (fields == null)
			throw new IllegalStateException("No fields array.");
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].id == fieldId)
				return fields[i];
		}
		return null;
	}

	public boolean includeInTotalFullText() {
		return properties == null ? true : properties.getBoolean(
				PROP_INCLUDE_IN_TOTAL_FULLTEXT, true);
	}

	// TODO: store table CRS in table?
	public int minscale(TiledCRS tiledCRS) {
		final int defaultMinScale = tiledCRS.getMinLevelId();
		return properties == null ? defaultMinScale : properties.getInt(
				PROP_MINSCALE, defaultMinScale);
	}

	public int maxscale(TiledCRS tiledCRS) {
		final int defaultMaxScale = tiledCRS.getMaxLevelId();
		return properties == null ? defaultMaxScale : properties.getInt(
				PROP_MAXSCALE, defaultMaxScale);
	}

	public boolean filterByScale(int scale, TiledCRS tiledCRS) {
		return (scale < minscale(tiledCRS) || scale > maxscale(tiledCRS));
	}

	public long getLastChangeTS() {
		if (lastMetaChange > lastDataWrite)
			return lastMetaChange;
		return lastDataWrite;
	}

	public boolean hasGeometry() {
		return getGeometryType().isGeom();
	}

	public GeomType getGeometryType() {
		return geomType;
	}
	
	public GeomType getGeomType() {
		return geomType;
	}
	
	public void setGeomType(GeomType geomType) {
		this.geomType = geomType;
		AbstractSymbologyUtils asu = AbstractSymbologyUtils.getInstance();
		if (geomType.isLine()) {
			styleJS = asu.toJavaScript(asu.createDefaultLineSimbology());
		} else if (geomType.isPoint()) {
			styleJS = asu.toJavaScript(asu.createDefaultPointSymbology());
		} else if (geomType.isPolygon()) {
			styleJS = asu.toJavaScript(asu.createDefaultPolygonSimbology());
		} else {
			styleJS = null;
		}
	}
	
	public StateGWT getProperties() {
		return properties;
	}

	public Field[] getFields() {
		return fields;
	}

	
	/*
	 * Remove when styles are replaced
	 */
	public static Integer getSingleFieldReptextId(String textRepExpr) {
		if (StringUtil.isNullOrEmpty(textRepExpr))
			return null;
		if (textRepExpr.startsWith("f") && textRepExpr.length() > 1) {
			try {
				return Integer.valueOf(textRepExpr.substring(1));
			} catch (NumberFormatException ex) {

			}
		}
		return null;
	}

	public static String createSingleFieldReptext(String value) {
		return "f"+value;
	}

	
	public String getStyle() {
		return styleJS;
	}

	public void setStyle(String value) {
		this.styleJS=value;
	}

	public String getRepText() {
		if (StringUtil.isNullOrEmpty(repTextJS)) {
			if (!hasValidId()) return null; // new unsaved table
			if (fields!=null) {
				for (Field f: fields) {
					if (f.getType() == Field.FieldType.PLAINTEXT && f.getVisibility().canView()) {
						repTextJS = "f"+f.getId();
						break;
					}
				}
			} 
			if (StringUtil.isNullOrEmpty(repTextJS)) {
				repTextJS = "id"+getId();
			}
			return repTextJS;
		}
		return repTextJS;
	}

	public void setRepText(String reptext) {
		this.repTextJS = reptext;
	}

}
