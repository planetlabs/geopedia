package com.sinergise.generics.datasource.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sinergise.generics.java.DBConnectionProvider;
import com.sinergise.generics.java.DatabaseType;
import com.sinergise.generics.server.GenericsServerSession;

public class MySQLConnectionProvider extends DBConnectionProvider{
	public static final String DEFAULT_CONNECTION ="defaultMySQLConnection";
	
	private DataSource oraDS;
	private static final Map<String, MySQLConnectionProvider> providers = new HashMap<String, MySQLConnectionProvider>();
	
	public static MySQLConnectionProvider getInstance(String dbConnectionName) {
		MySQLConnectionProvider ocp = providers.get(dbConnectionName);
		if (ocp!=null)
			return ocp;
		throw new RuntimeException("No m database connection named '"+dbConnectionName+"' found!");
		
	}
	
	public static synchronized MySQLConnectionProvider initializeDBConnection(String dbConnectionName, MySQLConnectionProvider cProvider) {
		MySQLConnectionProvider provider = providers.get(dbConnectionName);
		if (provider==null) {
			provider = cProvider;
			providers.put(dbConnectionName, provider);
		}
		return provider;
	}
	
	public static synchronized MySQLConnectionProvider initializeDBConnection(String dbConnectionName, String url, String username, String password) {
		MySQLConnectionProvider provider = providers.get(dbConnectionName);
		if (provider==null) {
			provider = new MySQLConnectionProvider(url, username, password);
			providers.put(dbConnectionName, provider);
		}
		return provider;
	}
	
	public static synchronized MySQLConnectionProvider initializeDBConnection(String dbConnectionName, MysqlDataSource oraDS) { 
		MySQLConnectionProvider provider = providers.get(dbConnectionName);
		if (provider==null) {
			provider = new MySQLConnectionProvider(oraDS);
			providers.put(dbConnectionName, provider);
		}
		return provider;
	}	
	
	protected MySQLConnectionProvider(String url, String username, String password) {
		MysqlDataSource mds = new MysqlDataSource();
		mds.setURL(url);
		mds.setUser(username);
		mds.setPassword(password);
		this.oraDS = mds;
	}
	
	protected MySQLConnectionProvider(MysqlDataSource oraDS) {
		this.oraDS = oraDS;
	}
	
	@Override
	public Connection getConnection(boolean autocommit) throws SQLException {
		return getConnection(autocommit, null);
	}
	

	
	
	@Override
	public Connection getConnection(boolean autocommit,
			GenericsServerSession gSession) throws SQLException {
		Connection conn = oraDS.getConnection();
		conn.setAutoCommit(autocommit);
		return conn;
	}
	@Override
	public void closeConnection(Connection conn, GenericsServerSession gSession)
			throws SQLException {
		conn.close();
	}
	
	@Override
	public void commitConnection(Connection conn, GenericsServerSession gSession)
			throws SQLException {
		conn.commit();
		
	}
	@Override
	public void rollbackConnection(Connection conn,
			GenericsServerSession gSession) throws SQLException {
		conn.rollback();
		
	}
	@Override
	public DatabaseType getDatabaseType() {
		return DatabaseType.MYSQL;
	}
	
}
