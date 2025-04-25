package com.sinergise.common.geometry.tiles;

import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.server.ServersCluster;
import com.sinergise.common.util.server.ServersClusterMap;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;

public class HttpTileURLProvider implements IsTileProvider {

	private static final long serialVersionUID = -3705377309269638632L;
	
	protected Identifier httpServersClusterIdentifier;
	protected String baseURL;
	private TiledDatasetProperties dsPropeties;

	private transient ServersCluster serversCluster;

	public static HttpTileURLProvider create (String path) {
		if (path.length()<10) throw new IllegalArgumentException("Illegal path");
		if (path.length() == (path.lastIndexOf("/")+1))
			path=path.substring(0, path.length()-1);
		int separator = path.lastIndexOf("/");
		String baseURL=path.substring(0,separator);
		String containerName = path.substring(separator+1);
		return new HttpTileURLProvider(baseURL, new TiledDatasetProperties(containerName));

	}
	public static HttpTileURLProvider create(ServersCluster httpServersCluster, String basePath, int maxScale) {
		return create(httpServersCluster, basePath, maxScale, null);
	}
	public static HttpTileURLProvider create(ServersCluster httpServersCluster, String basePath, int maxScale, MimeType mimeType) {
		if (!StringUtil.isNullOrEmpty(basePath) && basePath.startsWith("/")) basePath=basePath.substring(1);
		TiledDatasetProperties dsProperties = new TiledDatasetProperties(basePath, mimeType);
		dsProperties.setMaxScale(maxScale);
		
		return new HttpTileURLProvider(httpServersCluster.getIdentifier(), dsProperties);
	}
	
	
	@Deprecated
	protected HttpTileURLProvider() {		
	}
	public HttpTileURLProvider(Identifier httpServersClusterIdentifier, TiledDatasetProperties dsProperties) {
		this.httpServersClusterIdentifier = httpServersClusterIdentifier;
		this.baseURL=null;
		this.dsPropeties = dsProperties;
	}
	
	public HttpTileURLProvider(String baseURL, TiledDatasetProperties dsProperties) {
		this.baseURL=baseURL;
		this.httpServersClusterIdentifier=null;
		this.dsPropeties=dsProperties;
		
	}

	
	private ServersCluster getCluster() {
		if (serversCluster==null) {
			serversCluster = ServersClusterMap.getServersCluster(httpServersClusterIdentifier);
			if (serversCluster==null) {
				throw new RuntimeException("Unable to find cluster for identifier: "+ httpServersClusterIdentifier);
			}
		}
		return serversCluster;
	}

	
	
	@Override
	public String getObjectURL(String objectName) {
		String objectPath = null;
		if (!StringUtil.isNullOrEmpty(objectName)) {
			if (objectName.startsWith("/")) {
				objectPath = objectName;
			} else {
				objectPath = "/" + objectName;
			}
		} else {
			objectPath="";
		}

		if (httpServersClusterIdentifier==null) {
			return baseURL+objectPath;
		}
		
		return getCluster().getURL(objectName);
		
	}

	@Override
	public String getTileURL(TiledCRS crs, int scaleLevel, int column, int row) {
		return getObjectURL(dsPropeties.getTileLocation(crs, scaleLevel, column, row));
	}

	@Override
	public TiledDatasetProperties getDatasetProperties() {
		return dsPropeties;
	}

	@Override
	public String getDatasetPropertiesConfigurationURL() {
		return getObjectURL(dsPropeties.getPropertiesFilePath());
	}

}
