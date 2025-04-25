package com.sinergise.geopedia.server.service.importer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.db.DB;
import com.sinergise.geopedia.db.DBExecutor;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.server.service.MetaServiceImpl;

public class CodelistCache {
	private Table tbl = null;
	
	HashMap<Property<?>, Integer> hashMap = new HashMap<Property<?>,Integer>();

	private DB db;
	public Table getTable() {
		return tbl;
	}
	public CodelistCache (String tableName, Field origField, DB db) {
		this.db = db;
		tbl = new Table();
		tbl.geomType = GeomType.NONE;
		tbl.setName(tableName);
		Field f = new Field();
		f.setName(origField.getName());
		f.type = origField.type;
		tbl.fields = new Field[]{f};				
	}
	
	public Table createTable(MetaServiceImpl msi,Session session, Connection conn) throws GeopediaException, SQLException {
		tbl = db.saveTable(tbl, session, conn); 		
		return tbl;
	}
	
	public void lockTables(Connection conn, Session session) throws GeopediaException, SQLException {
		db.lockFeatureTables(new int[]{tbl.getId()}, conn, session.getServerInstance().getConfiguration());
	}
	
	
	public Integer addValue(Property<?> value, Connection conn, Session session) throws GeopediaException, SQLException {
		Integer id = hashMap.get(value);
		if (id!=null)
			return id;
		
		Feature feat = new Feature();
		feat.tableId = tbl.id;
		feat.fields = tbl.fields;
		feat.geomType = tbl.geomType;
		feat.properties = new Property<?>[]{value};
		feat = db.saveFeature(feat, tbl,  session, conn);
		//Integer newFeatureId = FeatureUtil.insertFeature(feat,	null, tbl, conn, session);
		hashMap.put(value,feat.getId());
		return feat.getId();
	}
	
	public void finalize(Connection conn, DBPoolHolder dbPoolHolder) throws SQLException {
		// mysql complains if you execute update within the same connection if some tables have not been locked.. 
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			@Override
			public Void execute(Connection conn) throws SQLException {
				db.touchTableData(conn, tbl.id, tbl.lastDataWrite);						
				return null;
			}
		});
	}
}
