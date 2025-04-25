package com.sinergise.geopedia.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.config.ServerConfiguration.DatabaseConfig;
import com.sinergise.java.util.sql.LoggableStatement;
import com.sinergise.java.util.sql.StatementLogger;
import com.sinergise.java.util.sql.StatementLoggerProvider;

public class DBPoolHolder
{
	private static Logger logger = LoggerFactory.getLogger(DBPoolHolder.class);

	private ConnectionPool local;
	private ConnectionPool update;


	
	public DBPoolHolder(InstanceConfiguration instanceConfig) {
		setupLocal(instanceConfig.serverCfg.queryDBConfig);
		setupUpdate(instanceConfig.serverCfg.updateDBConfig);
	}
	
	/**
	 * @deprecated use executeLocal instead
	 */
	public Connection getLocal() throws SQLException
	{
		return local.get();
	}
	
	/**
	 * @deprecated use executeUpdate instead
	 */

	public Connection getUpdate() throws SQLException
	{
		return update.get();
	}
	
	public <T> T executeUpdate(DBExecutor<T> executor) throws SQLException
	{
		Connection conn=getUpdate();
		try {
			return executor.execute(conn);
		} finally {
			releaseUpdate(conn);
		}
	}
	public <T> T executeLocal(DBExecutor<T> executor) throws SQLException
	{
		Connection conn=getLocal();
		try {
			return executor.execute(conn);
		} finally {
			releaseLocal(conn);
		}
	}
	
	public void releaseLocal(Connection conn)
	{
		if (conn == null)
			return;
		
		local.release(conn);
	}

	public  void releaseUpdate(Connection conn)
	{
		if (conn == null)
			return;
		
		update.release(conn);
	}
	
	static {
		doInit();
	}
	
	private static void doInit()
	{
		try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
        	throw new IllegalStateException(ex.getMessage());
        }
		
		
		StatementLoggerProvider.setLogger(new StatementLogger() {

			@Override
			public void logError(LoggableStatement stat, String message) {
				logger.error("SQLError: {}, statement: {}",message,stat);
				
			}

			@Override
			public void logExecution(LoggableStatement stat) {
				logger.trace("SQLExecution: {}",stat);
			}

			@Override
			public void logExecutionError(LoggableStatement stat) {
				logger.error("SQLExecution error: {}",stat);
			}
			
		});
	}
	
	public  void close(Statement s)
	{
		if (s == null)
			return;
		
		try {
			s.close();
		} catch (SQLException e) {
			// ignore
		}
	}
	
	public  void close(ResultSet rs)
	{
		if (rs == null)
			return;
		
		try {
			rs.close();
		} catch (SQLException e) {
			// ignore
		}
	}
	
	private  void setupLocal(DatabaseConfig queryDBConfig)
	{
		String host = queryDBConfig.jdbcHost;
		String user =queryDBConfig.username;
		String pass = queryDBConfig.password;
		String baseDb = TableAndFieldNames.getMetadataDBSchema(queryDBConfig.schemaPrefix);
		
		int port = queryDBConfig.port;
		int maxConnsPerServer = queryDBConfig.maxConnectionsPerServer;
		
		if (host == null || user == null || pass == null || baseDb == null)
			throw new IllegalStateException("Missing db data");
		
		String baseUrl = urlFor(host, port, baseDb);
		local = new ConnectionPool(baseUrl, user, pass, maxConnsPerServer);
	}
	
	private  void setupUpdate(DatabaseConfig updateDBConfig)
	{
		String host = updateDBConfig.jdbcHost;
		String user = updateDBConfig.username;
		String pass = updateDBConfig.password;
		String baseDb = TableAndFieldNames.getMetadataDBSchema(updateDBConfig.schemaPrefix);
		int port = updateDBConfig.port;
		int maxConnsPerServer = updateDBConfig.maxConnectionsPerServer;
		
		if (host == null || user == null || pass == null || baseDb == null)
			throw new IllegalStateException("Missing updatedb data");
		
		String baseUrl = urlFor(host, port, baseDb);
		update = new ConnectionPool(baseUrl, user, pass, maxConnsPerServer);
	}
	
	public static String urlFor(String host, int port, String dbName)
	{
		return "jdbc:mysql://"+host+":"+port+"/"+dbName+"?autoReconnect=true&cachePrepStmts=true&characterEncoding=UTF-8&useServerPrepStmts=false&rewriteBatchedStatements=true&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull";
	}
}
