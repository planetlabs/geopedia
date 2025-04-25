package com.sinergise.generics.builder.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sinergise.generics.builder.AbstractDatabaseTableInspector;
import com.sinergise.generics.builder.InspectorException;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.impl.XmlUtils;
import com.sinergise.generics.java.DBConnectionProvider;
import com.sinergise.java.util.sql.SQLUtilJava;

public class MySQLTableInspector extends AbstractDatabaseTableInspector {
	public static final Logger logger = LoggerFactory.getLogger(MySQLTableInspector.class);
	private DBConnectionProvider dbcp = null;
	
	public MySQLTableInspector(DBConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}
	
	@Override
	public Element inspect(Object toInspect) throws InspectorException {
		if (toInspect==null)
			return null;
		if (!(toInspect instanceof String))
			throw new InspectorException("Argument should be a string (table name or a view)");
		String tableName = (String) toInspect;
		try {
		    Connection conn = dbcp.getConnection();
		try {
			DatabaseMetaData metadata = conn.getMetaData();
	        
	        Document xml = XmlUtils.newDocumentBuilder().newDocument();
	        Element entity = xml.createElement("Entity");
	        entity.setAttribute(MetaAttributes.TYPE, tableName);
	        entity.setAttribute(MetaAttributes.NAME, tableName);
	        int pos=ENTITY_ATTRIBUTE_STARTPOSITION; // no table probably contains 100K entries so we start here  (0.99999 reserved for manual positioning)
	        /* 
	        ResultSetMetaData rsmd = resultSet.getMetaData(); int numColumns = rsmd.getColumnCount(); 
	        for (int i=1; i<numColumns+1; i++) {
	        	String columnName = rsmd.getColumnName(i); // Get the name of the column's table name 
	        	System.out.println("Column: "+columnName); 
	        }*/
	        
	        ResultSet resultSet = metadata.getColumns(null, null,tableName , null);
	        try {
	        	while (resultSet.next()) {
		        	Element ea = xml.createElement("EntityAttribute");
		        	ea.setAttribute(MetaAttributes.NAME, resultSet.getString("COLUMN_NAME"));
		 
		        	//System.out.println(resultSet.getInt("DATA_TYPE")+" "+resultSet.getString("TYPE_NAME"));
		        	int type=Types.STRING;
		        	switch (resultSet.getInt("DATA_TYPE")) {
			        	case java.sql.Types.VARCHAR: 
			        		type = Types.STRING;
			        		break;
			        	case java.sql.Types.INTEGER:
			        	case java.sql.Types.BIGINT:
			        	case java.sql.Types.SMALLINT:
			        	case java.sql.Types.NUMERIC:
			        	case java.sql.Types.DECIMAL:
			        		Integer decimalCount = null; //FLOAT if DECIMAL_DIGITS is null
				        	if(resultSet.getObject("DECIMAL_DIGITS") != null) {
				        		decimalCount = new Integer(resultSet.getInt("DECIMAL_DIGITS"));
				        	}
				        	
			        		if (decimalCount == null || decimalCount > 0)
			        			type = Types.FLOAT;
			        		else
			        			type = Types.INT;
			        		break;
			        	case java.sql.Types.BOOLEAN:
			        		type = Types.BOOLEAN;
			        		break;
			        	case 93: // mysql DATETIME
			        	case java.sql.Types.DATE:
			        		type = Types.DATE;
			        		break;
		        	}
		        	
		        	if (loadRemarks) {
			        	Object remarks = resultSet.getObject(12);
			        	if (remarks!=null) {
			        		ea.setAttribute(MetaAttributes.LABEL,remarks.toString());
			        	}
		        	}
		        	int columnSize = resultSet.getInt("COLUMN_SIZE");
		        	if (columnSize>0) {
		        		ea.setAttribute(MetaAttributes.VALUE_LENGTH, Integer.toString(columnSize));
		        	}
		        	
		        	ea.setAttribute(MetaAttributes.POSITION, Integer.toString(pos));
		        	ea.setAttribute(MetaAttributes.TYPE, Integer.toString(type));
		        	if (ignoreAll)
		        		ea.setAttribute(MetaAttributes.IGNORE, "true");
		        	pos++;
		        	entity.appendChild(ea);	        	
		        }
		        if (pos == ENTITY_ATTRIBUTE_STARTPOSITION) {
		        	logger.warn("No columns found for: table={}", tableName);
		        }
				return entity;
	        } finally {
	        	SQLUtilJava.closeSilent(resultSet);
	        }
	        
		} finally {
			close(conn);
		}
		} catch (Exception ex) {
			throw new InspectorException(ex);
		}
	}
	
	
	protected void close(Connection conn) {
		if(conn == null)
			return;
		try {
			dbcp.rollbackConnection(conn, null);
		} catch(SQLException ignore){}
		try {
			dbcp.closeConnection(conn, null);
		} catch(SQLException ignore){}
	}

}
