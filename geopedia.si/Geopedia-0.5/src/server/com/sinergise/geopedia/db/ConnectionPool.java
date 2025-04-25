package com.sinergise.geopedia.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.util.collections.FastStack;

public class ConnectionPool
{
	FastStack<Connection> connections = new FastStack<Connection>();
	
	public final int maxKeep;
	public final String jdbcUrl;
	public final String username, password;
	
	private String refUrl;
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	
	public ConnectionPool(String jdbcUrl, String username, String password, int maxKeep)
	{
		if (jdbcUrl == null || username == null || password == null || maxKeep < 0)
			throw new IllegalArgumentException();
		
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.maxKeep = maxKeep;
	}
	
	public Connection get() throws SQLException
	{
		synchronized(connections) {
			while (connections.size() > 0) {
				com.mysql.jdbc.Connection conn = (com.mysql.jdbc.Connection) connections.pop();
				if (conn.getIdleFor() > 30L*60*1000) {
					try {
						conn.close();
					} catch (SQLException e) {
						// ignore
					}
				} else {
					return conn;
				}
			}
		}
		logger.debug("Connecting to "+jdbcUrl+" (username = "+username+")");
		Connection res = DriverManager.getConnection(jdbcUrl, username, password);
		res.setAutoCommit(true);
		if (refUrl==null) {
			try {
				refUrl = res.getMetaData().getURL();
			} catch (Exception e) {
			}
		}
		return res;
	}
	
	/**
	 * Returns true if the connection was stored in the pool, or false if it was closed.
	 * 
	 * @param conn
	 * @return
	 */
	public boolean release(Connection conn)
	{
		if (conn == null)
			return false;
		
				
		try {
			if (!conn.getAutoCommit()) {
				DBUtil.rollBack(conn);
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			DBUtil.close(conn);
			return false;
		}

		try {
			if (conn.isClosed())
				return false;
		} catch (SQLException e) {
			return false;
		}
		
		if (refUrl!=null) {
			String compUrl=null;
			try {
				compUrl=conn.getMetaData().getURL();
			} catch (Exception e) {
			}
			if (!refUrl.equals(compUrl)) throw new IllegalArgumentException("Cannot release connection with different URL than pool; conn:"+compUrl+" pool:"+refUrl);
		}
		
		synchronized(connections) {
			if (connections.size() < maxKeep) {
				connections.push(conn);
				return true;
			}
		}

		DBUtil.close(conn);
		return false;
	}
}
