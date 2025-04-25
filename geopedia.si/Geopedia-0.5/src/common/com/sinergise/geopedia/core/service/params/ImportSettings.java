package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;
import java.util.ArrayList;

import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.geopedia.core.entities.Field;

public class ImportSettings implements Serializable{
	public static enum FileTypes {UNKNOWN, SHP, GPX}
	
	private static final long serialVersionUID = 6017731296751768150L;
	
	public FileTypes fileType = FileTypes.UNKNOWN;
	public int geometryType = GeometryTypes.GEOM_TYPE_NONE;
	
	public CFeatureDescriptor cFeatureDesc;
	public Field[] fields;
	
	public String tableName;
	public ArrayList<FieldSettings> enabledFields;
	public CrsIdentifier crsId; 
	
	public int existingTableId = Integer.MIN_VALUE;
	
	public static class FieldSettings implements Serializable {
		private static final long serialVersionUID = 6037973721637284721L;
		@SuppressWarnings("unused")
		@Deprecated
		private FieldSettings() {
			
		}
		public FieldSettings (int fn) {
			fieldNumber = fn;
		}
		public int fieldNumber;		
		public boolean isCodelist = false;
	}
	
	
}
