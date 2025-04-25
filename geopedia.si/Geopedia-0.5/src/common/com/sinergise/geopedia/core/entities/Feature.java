package com.sinergise.geopedia.core.entities;


import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;


/**
 * A class for transfer of feature data between server and client. Notes:
 * 
 * - fields should be shallow copies
 * - not all fields for a feature must be transferred at once
 * 
 * - inserts ignore featureId, must contain all not-null fields (except featureId :)
 * - deletes ignore everything but tableId, featureId
 * - updates will only update provided values, others will be left as-is
 * 
 * - fields and values must have the same number of elements
 * - null values are represented as a null ValueHolder (and not null String, for instance)
 *
 * - a feature without at least one field/value is invalid
 * 
 * - all fields must come from the table with id tableId (obviously :)
 * 
 * - updates and deletes must have a matching version number, lest an exception is thrown
 * 
 */
public class Feature extends HasId
{
	private static final long serialVersionUID = 1L;
	
	public static final int SYMBOL_ID_LINE = 1000;
	public static final int SYMBOL_ID_POLYGON = 999;
	public int tableId;
	public long tableDataTs;
	
	public int styleSymbolId = Integer.MIN_VALUE;
	public int styleColor = Integer.MIN_VALUE;
	
	/** Geometry of feature (if geom table) */
	public Geometry featureGeometry;
	public GeomType geomType;
	
	/** timestamp of this feature (not related to table data version) */
	public long timestamp;
	
	/** last user that modified this table */
	public int lastUserId;
	
	/** fields of values */
	public Field[] fields;
	
	
	public Property<?>[] properties;
	
	/** meta stuff of geometry:
	 * <pre>
	 * [0] = minX (all geoms)
	 * [1] = minY (all geoms)
	 * [2] = maxX (all geoms)
	 * [3] = maxY (all geoms)
	 * [4] = length (lines and polygons)
	 * [5] = area (polygons)
	 * [6] = centroidX (polygons)
	 * [7] = centroidY (polygons)
     * [8] = centroidZ (polygons, center-of-MBR-Z for others)
	 * </pre>
	 *  
	 **/
//	public double[] envelopeLengthAreaCentroid;

	public Envelope envelope;
	public Point centroid;
	public Double length = null;
	public Double area = null;
	
	/** representative text */
	public String repText;

	
	/** is feature deleted? */
	public boolean deleted;
	
	/** don't use outside FeatureServiceImpl */
	public volatile String fulltext;
	
	public Feature()
	{
		// ...
	}
	
	
	public Property<?> getUserFieldValue(int idx) {
		if (properties == null || properties.length<=idx)
			return null;
		return properties[idx];
	}
	
	public String getTextDesc()
	{
		if (repText != null)
			return repText;
		
		if (fields!=null && fields.length>0) {
			for (int i=0; i<fields.length; i++) {
				if(fields[i].type == FieldType.PLAINTEXT && properties[i] != null) {
					return ((TextProperty)properties[i]).getValue();
				}
			}
		}
		
		return String.valueOf(id);
	}
	
	public Field[] getFields(){
		return fields;
	}
    

    public Property getValue(int fieldId) {
        return properties[getFieldIndex(fieldId)];
    }
    
    public int getFieldIndex(int fieldId) {
        if (fields==null) throw new IllegalArgumentException("Fields not set");
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].id==fieldId) return i;
        }
        throw new IllegalArgumentException("Field "+fieldId+" not found");
    }
    
    public String toString() {
        StringBuffer sb=new StringBuffer();
        sb.append(id);
        sb.append(": ");
        if (properties!=null) {
            for (int i = 0; i < properties.length; i++) {
                if (i>0) sb.append(", ");
                sb.append(properties[i]);
            }
        }
        return sb.toString();
    }
    
    public boolean hasValidId() {
    	return (id>0);
    }

	public boolean verifyMandatoryUserFields() {
		if (fields==null)
			return true; // khm?
		if (properties==null || fields.length!=properties.length)
			return false;
		for (int i=0;i<fields.length;i++) {
			Field f = fields[i];
			Property<?> vh = properties[i];
			if (f.isMandatory() && (PropertyUtils.isNull(vh))) {
				return false;
			}
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public int getTableId() {
		return tableId;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDeleted() {
		return deleted;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Feature other = (Feature) obj;
		if (id != other.id)
			return false;
		if (tableId != other.tableId)
			return false;
		return true;
	}
	
	public boolean hasGeometry() {
		return getGeometryType().isGeom();
	}

	public GeomType getGeometryType() {
		return geomType;
	}


	public void setGeometry(Geometry geom) {
		featureGeometry=geom;
		envelope = new Envelope(geom.getEnvelope());
		if (geomType.isPoint()) {			
			centroid = new Point(envelope.getCenter());
		} else if (geomType.isLine()) {
			length = geom.getLength();
		} else if (geomType.isPolygon()){
			centroid = new Point(envelope.getCenter());
			area = geom.getArea();
		}
	}
	
	
	
}
