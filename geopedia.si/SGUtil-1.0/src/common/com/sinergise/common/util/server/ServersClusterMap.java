package com.sinergise.common.util.server;

import java.util.HashMap;

import com.sinergise.common.util.naming.Identifier;

public class ServersClusterMap {
	private static ServersClusterMap INSTANCE = null;
	
	private HashMap<Identifier, ServersCluster> clusterMap = new HashMap<Identifier, ServersCluster> ();
	
	public static ServersClusterMap initialize() {
		if (INSTANCE == null) {
			INSTANCE = new ServersClusterMap();
		}
		return INSTANCE;
	}
	
	private ServersClusterMap() {
		
	}
	
	public static void registerCluster(ServersCluster cluster) {
		getInstance().internalRegisterCluster(cluster);
	}
	
	
	
	public static ServersCluster getServersCluster(Identifier clusterIdentifier) {
		return getInstance().clusterMap.get(clusterIdentifier);
	}
	
	
	private void internalRegisterCluster(ServersCluster cluster) {
		clusterMap.put(cluster.getIdentifier(), cluster);
	}
	private static ServersClusterMap getInstance() {
		if (INSTANCE==null) {
			throw new RuntimeException("ServersClusterMap not initialised, did you forget to run ServersClusterMap.initialize .. ?");
		}
		return INSTANCE;
	}
}
