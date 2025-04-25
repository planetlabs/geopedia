package com.sinergise.generics.java;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.sinergise.generics.server.GenericsServerSession;

public class DataSourceConnectionProvider extends DBConnectionProvider{
	
	public static final String DEFAULT_CONNECTION ="defaultConnection";
	
	protected DataSource sqlDS;
	protected static final Map<String, DataSourceConnectionProvider> providers = new HashMap<String, DataSourceConnectionProvider>();
	
	
	public static DataSourceConnectionProvider getInstance(String dbConnectionName) {
		DataSourceConnectionProvider ocp = providers.get(dbConnectionName);
		if (ocp!=null)
			return ocp;
		throw new RuntimeException("No database connection named '"+dbConnectionName+"' found!");
		
	}
	
	public static synchronized DataSourceConnectionProvider initializeDBConnection(String dbConnectionName, DataSourceConnectionProvider cProvider) {
		DataSourceConnectionProvider provider = providers.get(dbConnectionName);
		if (provider==null) {
			provider = cProvider;
			providers.put(dbConnectionName, provider);
		}
		return provider;
	}
	
	public static synchronized DataSourceConnectionProvider initializeDBConnection(String dbConnectionName, DataSource sqlDS) { 
		DataSourceConnectionProvider provider = providers.get(dbConnectionName);
		if (provider==null) {
			provider = new DataSourceConnectionProvider(sqlDS);
			providers.put(dbConnectionName, provider);
		}
		return provider;
	}

	public static void destroyAll() {
		for (DataSourceConnectionProvider e : providers.values()) {
			e.destroy();
		}
		providers.clear();
	}
	
	protected DataSourceConnectionProvider(DataSource sqlDS) {
		this.sqlDS = sqlDS;
	}
	
	@Override
	public Connection getConnection(boolean autocommit) throws SQLException {
		return getConnection(autocommit, null);
	}
	
	@Override
	public Connection getConnection(boolean autocommit, GenericsServerSession gSession) throws SQLException {
		Connection conn = sqlDS.getConnection();
		conn.setAutoCommit(autocommit);
		return conn;
	}
	@Override
	public void closeConnection(Connection conn, GenericsServerSession gSession) throws SQLException {
		conn.close();
	}
	
	@Override
	public void commitConnection(Connection conn, GenericsServerSession gSession) throws SQLException {
		conn.commit();
		
	}
	@Override
	public void rollbackConnection(Connection conn, GenericsServerSession gSession) throws SQLException {
		conn.rollback();
	}

	@Override
	public DatabaseType getDatabaseType() {
		return DatabaseType.MSSQL;
	}

	private void destroy() {
		sqlDS = null;
	}

}
