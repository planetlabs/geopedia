package com.sinergise.geopedia.server.service.export;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.geometry.property.GeometryPropertyType;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.common.gis.io.TransformingFeatureWriter;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.property.ByteArrayProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.common.PediaEventConsts;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.service.params.ExportSettings;
import com.sinergise.geopedia.core.service.params.ExportStatus;
import com.sinergise.geopedia.server.service.FeatureServiceImpl;


public class FeaturesExportTask implements Runnable  {

	private static final Logger logger = LoggerFactory.getLogger(FeaturesExportTask.class);
	private static final int USER_FIELDS_START = 2;
	private static final int FLD_IDX_GEOMETRY = 1;
	private static final int FLD_IDX_ID = 0;

	private static final int MAX_PREREAD_SIZE=3000;
	private ExportStatus status = new ExportStatus();
	private FeatureWriter writer;
	private ExportSettings settings;
	private Session session;
	private Table table;
	CFeatureDescriptor cfd; 
	HashMap<Integer,Integer> fieldOrderMap;
		
	private File workDirectory;
	private ServerInstance instance;
	
	public FeaturesExportTask (FeatureWriter writer, File workDirectory, Session session, ServerInstance instance){
		this.writer=writer;
		this.instance = instance;
		this.session = session;
		this.workDirectory = workDirectory;
	}
	
	public ExportStatus getStatus() {
		return status;		
	}
	public File getWorkDirectory() {
		return workDirectory;
	}
	
	public void initialize(ExportSettings settings) {
		try {
			this.settings=settings;
			
			table = instance.getMetaData().getTableById(settings.tableID);
			
			
			fieldOrderMap = new HashMap<Integer,Integer>();
			ArrayList<Field> fieldArray= new ArrayList<Field>();
			int i=USER_FIELDS_START;
			for (Field f:table.fields) {
				if (f.isDeleted())
					continue;
				if (settings.fieldIDs.contains(f.id)) {
					fieldArray.add(f);
					fieldOrderMap.put(f.id,i);
					i++;				
				}
			}
			
			cfd = getTableDescriptor(table, fieldArray, settings.exportCentroidAsField);
			CRSSettings crsSettings = instance.getCRSSettings();
			
			boolean swapLatLon = (settings.exportFormat == ExportSettings.FMT_SHP) && (crsSettings.getCoordinateSystem(settings.crsTransformID) instanceof LatLonCRS);
			
			Transform<?, ?> transform = crsSettings.getTransform(crsSettings.getMainCrsId(), settings.crsTransformID, swapLatLon);
			if (transform!=null) {
				writer = new TransformingFeatureWriter(writer, transform, false); //don't clone geometries as exporting only
			}
			status.setWorking();

		} catch (SQLException ex) {
			ex.printStackTrace();
			status.setError(ex.getLocalizedMessage());
		}

	}
	
	public void cleanup() {
		if (!exportRunning) {
			try {
				if (workDirectory.exists() && workDirectory.isDirectory()) {
					for (File f : workDirectory.listFiles()) {
						f.delete();
					}
					workDirectory.delete();
				}
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		} else {
			terminate=true;
		}
	}
	
	private boolean exportRunning = false;
	private boolean terminate = false;
	
	
	@Override
	public void run() {
		exportRunning = true;
		FeatureServiceImpl fsi = new FeatureServiceImpl(session);
		int maxRows = FeatureServiceImpl.MAX_SEARCH_QUERY;
		int numRead = 0;
		int offset = 0;
		
		try {
			
			Query query = new Query();
			query.startIdx=offset;
			query.stopIdx=maxRows;
			if (!settings.resolveLookups) {
				query.options.add(Query.Options.NO_FOREIGNREF_RESOLVE);	
			}
			query.options.add(Query.Options.FLDMETA_ENVLENCEN);
			query.options.add(Query.Options.FLDMETA_BASE);
			query.options.add(Query.Options.FLDUSER_ALL);
			query.options.add(Query.Options.TOTALCOUNT);
			query.options.add(Query.Options.FLDMETA_GEOMETRY);
			query.tableId = settings.tableID;
			query.dataTimestamp = 0;
			if (settings.filterDescriptor==null) {
				query.filter =  FilterFactory.createDeletedDescriptor(settings.tableID, false);
			} else {
				query.filter = settings.filterDescriptor;
			}
			
			
			FeaturesQueryResults fqr = fsi.executeQuery(query, instance);
			status.totalFeatures = fqr.totalCount;
			query.options.remove(Query.Options.TOTALCOUNT);
			
			do {
				numRead = fqr.getCollection().size();
				offset += numRead;
				logger.debug("count="+fqr.getCollection().size());
				for (Feature f : fqr.getCollection()) {
					Geometry g =f.featureGeometry;
					if (g instanceof Point) { // change point to multipoint
						g = new MultiPoint(new Point[] { (Point) g });
					}
					CFeature cf = new CFeature(cfd, getProperties(f, g, cfd, fieldOrderMap));
					writer.append(cf);
					status.exportedCnt++;
					if (terminate) {
						writer.close();
						exportRunning=false;
						cleanup();
						return;
					}
				}				
				query.startIdx = offset;
				query.stopIdx = offset+maxRows;
				fqr = fsi.executeQuery(query, instance);
			} while (maxRows == numRead);
			logger.debug("actually exported "+status.exportedCnt);
			writer.close();
			status.setExported();
		} catch (Exception ex) {
			ex.printStackTrace();
			status.setError(ex.getLocalizedMessage());
		}
		exportRunning=false;
	}
	
	@SuppressWarnings("rawtypes")
	public static PropertyType getPropertyType(Field f) {
		switch (f.type) {
			case INTEGER:
				return new PropertyType(PropertyType.VALUE_TYPE_LONG);
			case DECIMAL:
				return new PropertyType(PropertyType.VALUE_TYPE_REAL);
			case DATE:
			case DATETIME:
				return new PropertyType(PropertyType.VALUE_TYPE_DATE);
			case BOOLEAN:
				return new PropertyType(PropertyType.VALUE_TYPE_BOOLEAN);
			case PLAINTEXT:
			case LONGPLAINTEXT:
			case WIKITEXT:				
			case STYLE:
				return new PropertyType(PropertyType.VALUE_TYPE_TEXT);
			case BLOB:
				return new PropertyType(PropertyType.VALUE_TYPE_BYTEARRAY);
		}
		return new PropertyType(PropertyType.VALUE_TYPE_TEXT);		
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static CFeatureDescriptor getTableDescriptor(Table table, ArrayList<Field> fieldArray, boolean exportCentroid) {
		CFeatureDescriptor tableDescriptor = new CFeatureDescriptor(new Identifier(Identifier.ROOT, table.getName()));
		int size = fieldArray.size();
		int addCentroid=0;
		if (table.geomType.isPoint() && exportCentroid) {
			addCentroid=2;
		}
		PropertyDescriptor[] props = new PropertyDescriptor[USER_FIELDS_START+size+addCentroid];
		for (int i=0;i<size;i++) {
			Field f = fieldArray.get(i);
			props[USER_FIELDS_START + i] = tableDescriptor.createPD(f.getName(),getPropertyType(f));
		}
		props[FLD_IDX_ID] = tableDescriptor.createPD("ID", new PropertyType(PropertyType.VALUE_TYPE_LONG));
		props[FLD_IDX_GEOMETRY] = tableDescriptor.createPD("Geometry", GeometryPropertyType.GENERIC_GEOMETRY);
		if (addCentroid!=0) {
			props[size+USER_FIELDS_START] = tableDescriptor.createPD("CentroidX", new PropertyType(PropertyType.VALUE_TYPE_REAL));
			props[size+USER_FIELDS_START+1] = tableDescriptor.createPD("CentroidY", new PropertyType(PropertyType.VALUE_TYPE_REAL));			
		}
		tableDescriptor.setValueDescriptors(props);
		tableDescriptor.setGeomIndex(FLD_IDX_GEOMETRY);
		return tableDescriptor;
	}
	
	
	public static Property<?> getProperty(Property<?> prop, Field f,PropertyDescriptor pDescriptor) {
		if (prop==null) {
			logger.warn("null property found! This should not happend. Field id="+f.getId());
			return PropertyUtils.forField(f);
		}
		switch (f.type) {
		case INTEGER:
		case DECIMAL: 
		case DATE:
		case DATETIME:
		case BOOLEAN:
		case PLAINTEXT:
		case LONGPLAINTEXT:
		case STYLE:
		case WIKITEXT:
			return prop;		
		case FOREIGN_ID:
			ForeignReferenceProperty frp = (ForeignReferenceProperty)prop;
			String value = null;
			if (frp.getReptext()!=null) {
				value=frp.getReptext();
			} else if (frp.getValue()!=null){
				value = frp.getValue().toString();
			}				
			return new TextProperty(value);		
			//return new LongProperty(((ValueHolderInt)vh).value);
		case BLOB: // TODO load pictures
			return new ByteArrayProperty(null);
		
		}
		
			
		throw new IllegalArgumentException("Unsupported FieldType: "+f.type);
	}
	
	private Property<?>[] getProperties(Feature feature, Geometry g, CFeatureDescriptor cfd, HashMap<Integer,Integer> fieldOrderMap) {
		int addCentroid = 0;
		if (table.geomType.isPoint() && settings.exportCentroidAsField) {
			addCentroid=2;
		}
		int size  = USER_FIELDS_START+ fieldOrderMap.size()+addCentroid;
		Property<?>[] properties = new Property<?>[size];
		for (int i=0;i<feature.fields.length;i++){
			Field f = feature.fields[i];
			Integer idx = fieldOrderMap.get(f.id);
			if (idx!=null) {
				properties[idx] = getProperty(feature.properties[i],f, cfd.getValueDescriptor(idx));
			}
		}
		// add geometry;
		properties[FLD_IDX_ID]=new LongProperty(feature.id);
		properties[FLD_IDX_GEOMETRY]=new GeometryProperty(g);
		
		if (table.geomType.isPoint() && settings.exportCentroidAsField) {
			HasCoordinate centroid = feature.envelope.getCenter();
			properties[USER_FIELDS_START+fieldOrderMap.size()] 
					= new DoubleProperty(centroid.x());
			properties[USER_FIELDS_START+fieldOrderMap.size()+1] 
					= new DoubleProperty(centroid.y());			
		} 
		return properties;
		
	}

	public void markAsDownloaded() {
		status.setDownloaded();
	}


	
}
