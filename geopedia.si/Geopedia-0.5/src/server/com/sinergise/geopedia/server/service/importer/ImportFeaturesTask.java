package com.sinergise.geopedia.server.service.importer;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.core.common.PediaEventConsts;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.service.params.ImportSettings;
import com.sinergise.geopedia.core.service.params.ImportSettings.FieldSettings;
import com.sinergise.geopedia.core.service.params.ImportSettings.FileTypes;
import com.sinergise.geopedia.core.service.params.ImportStatus;
import com.sinergise.geopedia.core.service.params.TaskStatus;
import com.sinergise.geopedia.core.service.params.TaskStatus.Status;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstColor;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstFillType;
import com.sinergise.geopedia.core.style.consts.ConstFontId;
import com.sinergise.geopedia.core.style.consts.ConstLineType;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.consts.ConstSymbolId;
import com.sinergise.geopedia.core.style.defs.FillStyleDef;
import com.sinergise.geopedia.core.style.defs.LineStyleDef;
import com.sinergise.geopedia.core.style.defs.StyleDef;
import com.sinergise.geopedia.core.style.defs.SymbolStyleDef;
import com.sinergise.geopedia.core.style.defs.TextStyleDef;
import com.sinergise.geopedia.core.style.model.FillType;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.db.DB;
import com.sinergise.geopedia.db.DBExecutor;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.server.SessionTask;
import com.sinergise.geopedia.server.service.FeatureServiceImpl;
import com.sinergise.geopedia.server.service.MetaServiceImpl;

public class ImportFeaturesTask implements SessionTask, Runnable {
	
    public static final StyleDef DEFAULT_STYLE = new StyleDef(new LineStyleDef(
            new ConstColor(0xFF000000), new ConstDouble(1), new ConstLineType(
                    LineType.SOLID)), new FillStyleDef(new ConstColor(
            0x80FFFFFF), new ConstColor(0), new ConstFillType(FillType.SOLID)),
            new SymbolStyleDef(new ConstSymbolId(SymbolId.STAR4),
                    new ConstLong(25), new ConstColor(0xFF0000FF),
                    new ConstString("")),
            new TextStyleDef(new ConstColor(0xFF000000), new ConstLong(15),
                    new ConstFontId(FontId.DEFAULT), new ConstBool(false),
                    new ConstBool(false)));
    
	protected ShapefileReader reader;
	protected Session session;
	protected ImportSettings iSettings =null;
	private ImportStatus tStatus = new ImportStatus();
	private File workDirectory;
	private long totalFeatures;
	private long importedFeatures;
	private int batchSize = 1; // single feature only
	private HashMap<Integer, CodelistCache> codelists = new HashMap<Integer,CodelistCache>();
	
	public ImportFeaturesTask (ShapefileReader reader,File workDirectory, Session session) throws Exception {
		this.reader = reader;
		this.session = session;
		this.workDirectory = workDirectory;
		
		iSettings = new ImportSettings();
		CFeatureDescriptor cfDesc = reader.getFeatureDescriptor();
		iSettings.fileType = FileTypes.SHP;
		iSettings.crsId = CRS.SI_D48.getDefaultIdentifier(); // TODO: read from some configuration?
		iSettings.geometryType = reader.getGeometryType();
		iSettings.fields = buildTableFields(cfDesc);
		iSettings.cFeatureDesc = cfDesc;
		tStatus.setStatus(Status.NOP);
		totalFeatures = reader.getFeatureCount();
		importedFeatures=0;
		iSettings.tableName="import_"+DateFormatter.FORMATTER_ISO_DATETIME.formatDate(new Date());
		

	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public ImportSettings getImportSettings() { 
		return iSettings;
	}
	public void setImportSettings(ImportSettings settings) {
		this.iSettings = settings;
	}

	
	private static Field[] buildTableFields(CFeatureDescriptor descriptor) {
		ArrayList<Field> fieldList = new ArrayList<Field>();
		int nFields = descriptor.size();
		for (int i=0;i<nFields;i++) {
			PropertyDescriptor pDesc = descriptor.getValueDescriptor(i);
			if (pDesc == descriptor.getGeomDescriptor()) {
				continue; // skip geomtry
			}
			PropertyType pType = pDesc.getType();
			Field field = new Field();
			field.setName(pDesc.getTitle());
			if (PropertyType.VALUE_TYPE_TEXT.equals(pType.getValueType())) {
				int length = pType.getInfoInteger(PropertyType.KEY_LENGTH, Integer.MAX_VALUE);
				if (length<=255)
					field.type = FieldType.PLAINTEXT;
				else 
					field.type = FieldType.LONGPLAINTEXT;
			} else if (PropertyType.VALUE_TYPE_DATE.equals(pType.getValueType())) {
				field.type = FieldType.DATE;
			} else if (PropertyType.VALUE_TYPE_LONG.equals(pType.getValueType())) {
				field.type = FieldType.INTEGER;
			} else if (PropertyType.VALUE_TYPE_BOOLEAN.equals(pType.getValueType())) {
				field.type = FieldType.BOOLEAN;
			} else if (PropertyType.VALUE_TYPE_REAL.equals(pType.getValueType())) {
				field.type = FieldType.DECIMAL;
			}
			fieldList.add(field);
		}
		return fieldList.toArray(new Field[fieldList.size()]);
	}

	protected Table createTable(GeomType geomType, ImportSettings iSettings, 
			MetaServiceImpl msi, DB db, Connection conn) throws GeopediaException, SQLException {
		Table t = new Table();
		int nFields = iSettings.enabledFields.size();
		
		
		ArrayList<Field> fieldList = new ArrayList<Field>();
		
		t.setGeomType(GeomType.NONE);
		t.setName(iSettings.tableName);
		t.descRawHtml="<p>"+iSettings.tableName+"</p>";
	
		
		for (int i=0;i<nFields;i++) {
			FieldSettings fSettings = iSettings.enabledFields.get(i);
			if (fSettings.fieldNumber == iSettings.cFeatureDesc.getGeomIndex()) {
				t.setGeomType(geomType);		
				continue;
			}
			Field field = iSettings.fields[fSettings.fieldNumber];
			
			if (fSettings.isCodelist) {
				CodelistCache cc = new CodelistCache(t.getName()+" - "+field.getName(), field, db);
				Table refTable = cc.createTable(msi, session, conn);
				field.refdTable = refTable;
				field.refdTableId = refTable.id;
				field.type = FieldType.FOREIGN_ID;
				codelists.put(i, cc);
			}
			fieldList.add(field);
		}
		t.fields = fieldList.toArray(new Field[fieldList.size()]);
		
		//t.styleSpecString = DEFAULT_STYLE.toString();
		t = db.saveTable(t, session, conn);
		for (CodelistCache cc:codelists.values())
			cc.lockTables(conn, session);
		return t;
	}
	
	protected static GeomType getPediaGeometry(int geometryType) {
		 
		switch (geometryType) {
		case GeometryTypes.GEOM_TYPE_POINT:
			return GeomType.POINTS_M;
		case GeometryTypes.GEOM_TYPE_POLYGON:
			return GeomType.POLYGONS_M;		
		case GeometryTypes.GEOM_TYPE_LINESTRING:
			return GeomType.LINES_M;
		}
		return GeomType.NONE;
	}
	
	/**
	 * Creates feature and inserts codelist to cache. !Fix! codelists should also be batch-inserted
	 * @param tbl
	 * @param feat
	 * @param iSettings
	 * @param conn
	 * @param db
	 * @param session
	 * @return
	 * @throws UpdateException
	 * @throws SQLException
	 * @throws GeopediaException
	 */
	
	
	
	HashMap<Integer,Integer> importFieldIdxToGpdIdxMap = null;
	protected Feature createFeature(Table tbl, CFeature feat, ImportSettings iSettings, Connection conn, DB db, Session session)
	throws UpdateException, SQLException, GeopediaException {
		
		Feature f = tbl.createEmptyFeature();
		f.tableId = tbl.id;
		f.fields = tbl.fields;
		f.geomType = tbl.geomType;
		int nFields = iSettings.enabledFields.size();
				
		
		if (importFieldIdxToGpdIdxMap==null) {
			importFieldIdxToGpdIdxMap = new HashMap<Integer,Integer>();
	 		for (int i = 0; i < nFields; i++) {
	 			FieldSettings fSettings = iSettings.enabledFields.get(i);
	 			if (fSettings.fieldNumber == feat.getDescriptor().getGeomIndex()) continue;
	 			boolean mappingFound = false;
	 			for (int j=0;j<tbl.fields.length;j++) {	 				
	 				if (tbl.fields[j].getName().equalsIgnoreCase(iSettings.fields[fSettings.fieldNumber].getName())) {
	 					importFieldIdxToGpdIdxMap.put(fSettings.fieldNumber,j);
	 					mappingFound=true;
	 					break;
	 				}
	 			}
	 			if (!mappingFound) throw new IllegalStateException("Unable to find mapping for field: "+iSettings.fields[fSettings.fieldNumber].getName());
	 		}
		}
		
		ArrayList<Property<?>> vhList = new ArrayList<Property<?>>();
 		for (int i = 0; i < nFields; i++) {
 			
 			FieldSettings fSettings = iSettings.enabledFields.get(i);
			Property<?> prop = feat.getProperty(fSettings.fieldNumber);
			
			
			
			
			if (fSettings.fieldNumber == feat.getDescriptor().getGeomIndex()) {
				f.featureGeometry = feat.getGeometry();	
			} else {
				int propIdx = importFieldIdxToGpdIdxMap.get(fSettings.fieldNumber); 
				if (fSettings.isCodelist) {
						CodelistCache cc = codelists.get(i);
						if (cc==null) throw new IllegalStateException("Unable to find codelist for field '"+i+"'");
						Integer intValue = cc.addValue(prop, conn, session);
						ForeignReferenceProperty frp = new ForeignReferenceProperty(intValue==null?null:(long)intValue.intValue());
						f.properties[propIdx]=frp;
				} else {				
					if (f.fields[propIdx].getType()==FieldType.DECIMAL &&
							prop instanceof LongProperty) {
						if (prop!=null && !prop.isNull())
							f.properties[propIdx] = new DoubleProperty(((LongProperty)prop).getValue().doubleValue());
					} else {
						f.properties[propIdx]=prop;
					}
				}
			}
		}
			
//		f.properties = vhList.toArray(new Property<?>[vhList.size()]);
		if (tbl.fields.length!=f.properties.length) {
			System.out.println("illegal "+tbl.fields.length+" "+f.properties.length);
		}
				
		return f;
	}
	
	public void initialize() {
		tStatus.setStatus(Status.WORKING);		
	}
	private boolean terminate = false;
	private boolean running = false;
	private MetaServiceImpl msi;
	
	
	private void batchInsertFeatures(ArrayList<Feature> featuresList, Table tbl, DB db, Connection conn, InstanceConfiguration instanceConfig) throws GeopediaException, SQLException {
		if (featuresList==null || featuresList.size()==0)
			return;
		db.batchInsertFeatures(featuresList, tbl, session.getUser(), conn, instanceConfig);
		importedFeatures+=featuresList.size();
		tStatus.setProgress((double)importedFeatures/(double)totalFeatures);
		featuresList.clear();	
	}
	
	@Override
	public void run() {
		Connection conn =null; 
		try {
			ServerInstance instance = ServerInstance.getInstance(session.getInstanceId());
			InstanceConfiguration instanceConfig = instance.getConfiguration();
			running = true;
		try {
			final DB db = instance.getDB();
			DBPoolHolder dbPoolHolder = instance.getDBPoolHolder();
			
			CRSSettings crsSettings = instance.getCRSSettings();
			Transform<?, ?> transform =
					crsSettings.getTransform(iSettings.crsId, crsSettings.getMainCrsId(), true);
			if (transform!=null) {
				reader.setTransform(transform);
			}

			msi = new MetaServiceImpl(session);
			conn = dbPoolHolder.getUpdate();
			conn.setAutoCommit(false);

			FeatureServiceImpl fsi = new FeatureServiceImpl(session);
			Table tbl;
			if (iSettings.existingTableId!=Integer.MIN_VALUE) {
				tbl = msi.getTableById(iSettings.existingTableId, 0, DataScope.ALL);
			} else {
				Statement st=null;
				try {
					st = conn.createStatement();
//					st.execute("LOCK TABLES "+TTables.TBL_NAME+ " WRITE ,"
//							+TFields.TBL_NAME+" WRITE, "
//							+TPermissions.TBL_NAME+ " WRITE, "
//							+TUserTables.TBL_NAME+" WRITE");
				tbl = createTable(getPediaGeometry(reader.getGeometryType()),
					iSettings, msi, db, conn);
				} finally {
					try {
						st.execute("UNLOCK TABLES");
					} catch (SQLException ex) {ex.printStackTrace();}
					DBUtil.close(st);
				}
			}
			
			final int tableId = tbl.id;
			
			
			final long 	newDataTs = db.getVersionStamp();
			CFeatureDescriptor descriptor = reader.getFeatureDescriptor();
			
			int [] tablesToLock = new int[1+codelists.size()];
			tablesToLock[0] = tbl.getId();
			int i=1;
			for (CodelistCache clCache:codelists.values()) {
				tablesToLock[i]=clCache.getTable().getId();
				i++;
			}
			db.lockFeatureTables(tablesToLock, conn, instanceConfig);
			ArrayList<Feature> featuresList = new ArrayList<Feature>();
			while (reader.hasMoreFeatures()) {
				CFeature feat = reader.nextFeature();
				Feature feature = createFeature (tbl, feat, iSettings, conn, db, session);
				if (batchSize > 1) {
					featuresList.add(feature);					
					if (featuresList.size()>batchSize) {
						batchInsertFeatures(featuresList, tbl, db, conn, instanceConfig);
					}
				} else {
					db.saveFeature(feature, tbl, session, conn);
					importedFeatures++;
					tStatus.setProgress((double)importedFeatures/(double)totalFeatures);

				}
				if (terminate) {
					reader.done();
					break;
				}				
			}
			if (!terminate) {
				batchInsertFeatures(featuresList, tbl, db, conn, instanceConfig); // insert remaining

				for (CodelistCache cc:codelists.values()) {
					cc.finalize(conn, dbPoolHolder);
				}
				conn.commit();				
				// can't update tables if not locked (silly mysql)
				dbPoolHolder.executeUpdate(new DBExecutor<Void>() {

					@Override
					public Void execute(Connection conn) throws SQLException {	
						db.touchTableMeta(conn, tableId, newDataTs);
						db.touchTableData(conn, tableId, newDataTs);								
						return null;
					}
				});
			}
			tStatus.setStatus(Status.FINAL);	
			tStatus.tableId = tbl.id;
		} catch (Exception ex) {
			if (conn!=null) {
				try {
				conn.rollback();
				} catch (SQLException e) {
					
				}
			}
			ex.printStackTrace();
			tStatus.setError(TaskStatus.GENERAL_ERROR);
			tStatus.setStatus(Status.ERROR);
			terminate=true; // so junk get's cleaned up..
		} finally {
			if (conn!=null) {
				try {
					Statement s = conn.createStatement();
					s.execute("unlock tables");
					s.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				try {
					conn.close();
				} catch (SQLException ex) {}
				instance.getDBPoolHolder().releaseUpdate(conn);
			}

		}
		running = false;
		if (terminate) {
			
			// delete stuff because of stupid myisam
		}
		} catch (GeopediaException ex) {
			tStatus.setError(TaskStatus.GENERAL_ERROR);
			tStatus.setStatus(Status.ERROR);
		}
		
	}

	@Override
	public boolean isFinished() {
		if (tStatus.getStatus().isBefore(Status.FINAL)) 
			return false;
		return true;
	}

	@Override
	public void cleanup() {
		if (!running) {
			try {
				if (workDirectory != null && workDirectory.exists() && workDirectory.isDirectory()) {
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

	public ImportStatus getTaskStatus() {
		return tStatus;
	}

}
