package com.sinergise.geopedia.db.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.db.MySQLFieldType;
import com.sinergise.geopedia.db.MySQLFieldType.DatabaseIndex;
import com.sinergise.geopedia.db.MySQLFieldType.DatabaseType;
import com.sinergise.geopedia.db.TableAndFieldNames.FeaturesTable;
import com.sinergise.java.util.sql.LoggableStatement;

public class DatabaseStructures {


	
	private static class TableField {
		public String name;
		public DatabaseType type;
		public boolean nullable = true;
		public DatabaseIndex index = DatabaseIndex.NONE;
		public boolean autoIncrement = false;
		
		public TableField(Field field) {
			this(FeaturesTable.userField(field), MySQLFieldType.getDatabaseType(field));
		}
		public TableField(String name, DatabaseType type) {
			this.name=name;
			this.type=type;
		}
		
		public TableField setIndex(DatabaseIndex index) {
			this.index=index;
			return this;
		}
		public TableField setAutoIncrement(boolean on) {
			this.autoIncrement=on;
			return this;
		}
		
		public String toString() {
			return name;
		}

		public TableField setNullable(boolean nullable) {
			this.nullable=nullable;
			return this;
		}
	}
	
	public static ArrayList<TableField> createBaseHistoryTableFields (Table table) {
		int tableId = table.getId();
		ArrayList<TableField> fields = new ArrayList<TableField>();
		// hid 
		fields.add(new TableField(FeaturesTable.hid(tableId), DatabaseType.IDENTIFIER)
				   .setIndex(DatabaseIndex.PRIMARY)
				   .setAutoIncrement(true)
				   .setNullable(false));
		// id
		fields.add(new TableField(FeaturesTable.id(tableId), DatabaseType.IDENTIFIER)
		   .setNullable(false));


		// user reference
		fields.add(new TableField(FeaturesTable.user(tableId), DatabaseType.IDENTIFIER)
					.setNullable(false));
		// deleted
		fields.add(new TableField(FeaturesTable.deleted(tableId), DatabaseType.BOOLEAN)
					.setNullable(false));
		// timestamp - silly field, not related to data version in table
		fields.add(new TableField(FeaturesTable.timestamp(tableId), DatabaseType.TIMESTAMP));

		if (table.hasGeometry()) {
			// geometry
			fields.add(new TableField(FeaturesTable.geometry(tableId), DatabaseType.GEOMETRY)
					.setIndex(DatabaseIndex.GEOMETRY)
					.setNullable(false));
		}
		
		return fields;
	}
	public static ArrayList<TableField> createBaseTableFields(Table table) {
		int tableId = table.getId();
		ArrayList<TableField> fields = new ArrayList<TableField>();
		// id 
		fields.add(new TableField(FeaturesTable.id(tableId), DatabaseType.IDENTIFIER)
				   .setIndex(DatabaseIndex.PRIMARY)
				   .setAutoIncrement(true)
				   .setNullable(false));
		// user reference
		fields.add(new TableField(FeaturesTable.user(tableId), DatabaseType.IDENTIFIER)
					.setNullable(false));
		// deleted
		fields.add(new TableField(FeaturesTable.deleted(tableId), DatabaseType.BOOLEAN)
					.setNullable(false));
		// fulltext
		fields.add(new TableField(FeaturesTable.fullText(tableId), DatabaseType.TEXT).setIndex(DatabaseIndex.FULLTEXT));
		// timestamp - silly field, not related to data version in table
		fields.add(new TableField(FeaturesTable.timestamp(tableId), DatabaseType.TIMESTAMP));

		if (table.hasGeometry()) {
			// geometry
			fields.add(new TableField(FeaturesTable.geometry(tableId), DatabaseType.GEOMETRY)
					.setIndex(DatabaseIndex.GEOMETRY)
					.setNullable(false));
			// mbr
			fields.add(new TableField(FeaturesTable.minX(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
			fields.add(new TableField(FeaturesTable.minY(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
			fields.add(new TableField(FeaturesTable.maxX(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
			fields.add(new TableField(FeaturesTable.maxY(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
			
			if (table.getGeometryType().isLine() || table.getGeometryType().isPolygon()) {
				// length
				fields.add(new TableField(FeaturesTable.length(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
			}
			
			if (table.getGeometryType().isPolygon()) {
				// area
				fields.add(new TableField(FeaturesTable.area(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
				// centroid
				fields.add(new TableField(FeaturesTable.centroidX(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
				fields.add(new TableField(FeaturesTable.centroidY(tableId), DatabaseType.DOUBLE)
					.setNullable(false));
			}
		}
		
		return fields;
	}
	
	public static void alterTable(Table table, ArrayList<Field> newFields, Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException {
		alterTable(FeaturesTable.table(table.getId(), instanceConfiguration), newFields, conn);
		alterTable(FeaturesTable.historyTable(table.getId(), instanceConfiguration), newFields, conn);
	}
	
	private static void alterTable(String tableName, ArrayList<Field> newFields, Connection conn) throws SQLException {
		if (newFields==null || newFields.size()==0)
			return;
		ArrayList<TableField> fields = new ArrayList<TableField>();
		
		for (Field f:newFields) {
			fields.add(new TableField(f));
		}

		HashMap<DatabaseIndex,ArrayList<TableField>> idxMap = new HashMap<DatabaseIndex,ArrayList<TableField>>();
		String sql = "ALTER TABLE "+tableName+" ";
		boolean first = true;
			for (TableField f:fields) {
				if (!first) 
					sql+=", ";
				first=false;
				
				sql+=" ADD COLUMN "+tableFieldToSQL(f);
				addTableFieldToIdxMap(f, idxMap);
			}
		if (idxMap.size()>0) {
			for (DatabaseIndex idx:idxMap.keySet()) {
				
				String idxSQL=createIndexSQL(idx, idxMap.get(idx));
				if (!StringUtil.isNullOrEmpty(idxSQL)) {
					sql+=", ADD "+idxSQL;
				}
			}
		} 
		
		LoggableStatement st = new LoggableStatement(conn, sql);
		try {
			st.execute();
		} finally {
			DBUtil.close(st);
		}
	}
	
	public static void createTable(Table table, Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException {
		createTable(table, FeaturesTable.table(table.getId(), instanceConfiguration), conn);
		createHistoryTable(table, conn, instanceConfiguration);
	}
	
	private static void createHistoryTable(Table table,  Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException {
		String tableName = FeaturesTable.historyTable(table.getId(), instanceConfiguration);
		ArrayList<TableField> fields = createBaseHistoryTableFields(table);
		
		for (Field f:table.getFields()) {
			fields.add(new TableField(f));
		}

		
		HashMap<DatabaseIndex,ArrayList<TableField>> idxMap = new HashMap<DatabaseIndex,ArrayList<TableField>>();
		String sql = "CREATE TABLE "+tableName+" (";
		boolean first = true;
			for (TableField f:fields) {
				if (!first) 
					sql+=", ";
				first=false;
				
				sql+=tableFieldToSQL(f);
				addTableFieldToIdxMap(f, idxMap);
			}
		if (idxMap.size()>0) {
			for (DatabaseIndex idx:idxMap.keySet()) {
				
				String idxSQL=createIndexSQL(idx, idxMap.get(idx));
				if (!StringUtil.isNullOrEmpty(idxSQL)) {
					sql+=","+idxSQL;
				}
			}
		} 
		
		sql+=") ENGINE = MyISAM DEFAULT CHARSET = utf8";
		
		LoggableStatement st = new LoggableStatement(conn, sql);
		try {
			st.execute();
		} finally {
			DBUtil.close(st);
		}
	}
	
	private static void createTable(Table table, String tableName, Connection conn) throws SQLException {
		ArrayList<TableField> fields = createBaseTableFields(table);
		
		for (Field f:table.getFields()) {
			fields.add(new TableField(f));
		}

		
		HashMap<DatabaseIndex,ArrayList<TableField>> idxMap = new HashMap<DatabaseIndex,ArrayList<TableField>>();
		String sql = "CREATE TABLE "+tableName+" (";
		boolean first = true;
			for (TableField f:fields) {
				if (!first) 
					sql+=", ";
				first=false;
				
				sql+=tableFieldToSQL(f);
				addTableFieldToIdxMap(f, idxMap);
			}
		if (idxMap.size()>0) {
			for (DatabaseIndex idx:idxMap.keySet()) {
				
				String idxSQL=createIndexSQL(idx, idxMap.get(idx));
				if (!StringUtil.isNullOrEmpty(idxSQL)) {
					sql+=","+idxSQL;
				}
			}
		} 
		
		sql+=") ENGINE = MyISAM DEFAULT CHARSET = utf8";
		
		LoggableStatement st = new LoggableStatement(conn, sql);
		try {
			st.execute();
		} finally {
			DBUtil.close(st);
		}
	}
	
	private static void addTableFieldToIdxMap(TableField field, HashMap<DatabaseIndex,ArrayList<TableField>> idxMap) {
		if (field.index != DatabaseIndex.NONE) {
			ArrayList<TableField> list = idxMap.get(field.index);
			if (list==null) {
				list = new ArrayList<TableField>();
				idxMap.put(field.index,list);
			}
			list.add(field);
		}
	}
	
	private static String tableFieldToSQL(TableField field) {
		String sql="";
		sql+=field.name+" "+MySQLFieldType.toSQLType(field.type);
		if (!field.nullable)  {
			sql+=" NOT NULL";
		}
		if (field.autoIncrement) {
			sql+=" AUTO_INCREMENT";
		}
		if (field.type.isText()) {
			sql+=" CHARACTER SET utf8 COLLATE utf8_slovenian_ci";
		}
		return sql;
	}
	
	private static String createIndexSQL(DatabaseIndex idx, ArrayList<TableField> fields) {
		String sql = "";
		if (fields==null || fields.size()==0) return sql;
		switch (idx) {
			case PRIMARY:
				sql+="PRIMARY KEY";
			break;
			case GEOMETRY:
				sql+="SPATIAL KEY";
			break;
			case FULLTEXT:
				sql+="FULLTEXT KEY";
			break;
		}
		sql+="("+StringUtil.collectionToString(fields,",")+")";
		return sql;
	}
	
	
	
	public static void main(String[] args) {
		/*
		Session mySession = Session.DEFAULT_SESSION;
		try {
			mySession.setUser(DB.getUser("pkolaric"));
		MetaServiceImpl msi = new MetaServiceImpl(mySession);
		Table tbl = msi.getTableById(659, 0, DataScope.ALL);
		createTable(tbl, null);
		Table tbl1 = msi.getTableById(2770, 0, DataScope.ALL);
//		alterTable(tbl, new ArrayList<Field>().addAll(tbl1.fields), null);
		}catch (Exception e1) {
			e1.printStackTrace();
		}
*/
	}
}
