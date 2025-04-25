package com.sinergise.geopedia.config;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gson.Gson;

public class ServerConfiguration implements Serializable {
	
	public static final String PROP_COPYRIGHT_WATERMARK = "copyright.watermark";
	public static final String PROP_DMVFILE = "dmvFile";
	public static final String PROP_WEBLINKS = "webLinks.location";
	
	public static class DatabaseConfig implements Serializable {		
		public String jdbcHost;
		public String username;
		public String password;
		public String schemaPrefix;
		public int port = 3306;
		public int maxConnectionsPerServer = 5;
	}
	
	
	public DatabaseConfig updateDBConfig;
	public DatabaseConfig queryDBConfig;
	
	
	public HashMap<String,String> serverProperties = new HashMap<String,String> ();
	
	
	public String getStringProperty(String propertyName) {
		return serverProperties.get(propertyName);
	}
	
	public static void main(String[] args) {
		
		Gson gson = new Gson();
		ServerConfiguration sc = new ServerConfiguration();		
		sc.updateDBConfig = new DatabaseConfig();
		sc.updateDBConfig.jdbcHost="10.5.250.20";
		sc.updateDBConfig.username="pediaupdate";
		sc.updateDBConfig.password="pediaupdate";
		sc.updateDBConfig.schemaPrefix="pedicamain";
		
		sc.queryDBConfig = new DatabaseConfig();
		sc.queryDBConfig.jdbcHost="localhost";
		sc.queryDBConfig.username="pediawww";
		sc.queryDBConfig.password="pediawww";
		sc.queryDBConfig.schemaPrefix="pedicamain";
		
		
		String jdbc = gson.toJson(sc);
		
		System.out.println(gson.toJson(sc));
		
		ServerConfiguration sconfig = gson.fromJson(jdbc, ServerConfiguration.class);
		System.out.println(sconfig.updateDBConfig.jdbcHost);
	}

	public void setProperty(String key, String value) {
		serverProperties.put(key, value);
		
	}
}
