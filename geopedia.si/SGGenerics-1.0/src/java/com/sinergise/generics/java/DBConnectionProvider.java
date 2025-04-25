package com.sinergise.generics.java;

import java.sql.Connection;
import java.sql.SQLException;

import com.sinergise.generics.server.GenericsServerSession;

public abstract class DBConnectionProvider {
	public Connection getConnection() throws SQLException {
		return getConnection(false);
	}
	
	public abstract Connection getConnection(boolean autocommit) throws SQLException;
	public abstract Connection getConnection(boolean autocommit, GenericsServerSession gSession) throws SQLException;
	public abstract void closeConnection( Connection conn, GenericsServerSession gSession) throws SQLException;
	public abstract void commitConnection (Connection conn, GenericsServerSession gSession) throws SQLException;
	public abstract void rollbackConnection (Connection conn, GenericsServerSession gSession) throws SQLException;
	
	public Connection startTransaction(GenericsServerSession gSession) throws SQLException {
		return getConnection(false, gSession);
	}
	
	public void endTransaction(Connection conn, GenericsServerSession gSession) throws SQLException {
		closeConnection(conn, gSession);
	}
	
	public abstract DatabaseType getDatabaseType();
	
	protected String defaultSchemaName = null;
	
	public void setDefaultSchema (String defaultSchema) {
		this.defaultSchemaName = defaultSchema;
	}
	
	public String getDefaultSchema() {
		return defaultSchemaName;
	}
}
