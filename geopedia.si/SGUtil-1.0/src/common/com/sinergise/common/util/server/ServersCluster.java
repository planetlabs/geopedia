package com.sinergise.common.util.server;

import java.io.Serializable;
import java.util.ArrayList;

import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.string.StringUtil;


public class ServersCluster implements Serializable {

	private static final long serialVersionUID = 1L;

	private Identifier identifier;
	
	private ClusterServer servers[];
	private transient ArrayList<ClusterServer> onlineServers;
	
	
	/** serialization only **/
	@Deprecated
	protected ServersCluster() {
	}
	

	public static ServersCluster createPlainHTTPServersCluster(String clusterIdentifier, String [] serverName, String[] httpServerURLs) {
		if (httpServerURLs==null || httpServerURLs.length==0) {
			throw new IllegalArgumentException("Provide a list of http servers!");
		}
		Identifier clusterIdent =  new Identifier(Identifier.ROOT, clusterIdentifier);
		ClusterPlainHTTPServer [] servers = new ClusterPlainHTTPServer[httpServerURLs.length];
		for (int i=0;i<httpServerURLs.length;i++) {
			Identifier srvIdentifier = new Identifier(Identifier.ROOT, serverName[i]);
			servers[i] = new ClusterPlainHTTPServer(srvIdentifier, httpServerURLs[i]);
		}
		return new ServersCluster(clusterIdent, servers);
	
	}
	
	public static ServersCluster createPlainHTTPServersCluster(String clusterIdentifier, String[] httpServerURLs) {
		return createPlainHTTPServersCluster(clusterIdentifier, httpServerURLs, httpServerURLs);
	}
	
	public ServersCluster(Identifier identifier, ClusterServer ... servers) {
		if (servers.length==0)
			throw new IllegalArgumentException("At least one URL is required!");
		this.identifier = identifier;
		this.servers = servers;
		for (ClusterServer server:servers) {
			server.getIdentifier().bindTo(identifier);
		}
	}
	
	private void updateTransient() {
		synchronized(servers) {
			if (onlineServers==null) {
				onlineServers = new ArrayList<ClusterServer>();
				for (ClusterServer srv:servers) {
					if (srv.isOnline())
						onlineServers.add(srv);
				}
			}
		}
	}
	
	private ClusterServer getOnlineServer(long pathHash) {
			
			if (onlineServers == null) {
				updateTransient();
			}
			synchronized(onlineServers) {
				int numAvailableServers = onlineServers.size();
				if (numAvailableServers==0)  // no servers...
					return null;
				int serverIdx = (int)(Math.abs(pathHash)%numAvailableServers);
				return onlineServers.get(serverIdx);
			}
	}
	

	public String getNextServerURL() {
		return getURL(null);
	}
		
	public String getURL(String path) {				
		if (StringUtil.isNullOrEmpty(path)) {			
			ClusterServer server = getOnlineServer(0);
			if (server!=null) {
				return server.getURL();
			}
			return null;
		} 
		ClusterServer server = getOnlineServer(path.hashCode());
		if (server==null) {
			return null;
		}
		return server.getURL()+path;		
	}
	
	
	public Identifier getIdentifier() {
		return identifier;
	}
	
	
	

}
