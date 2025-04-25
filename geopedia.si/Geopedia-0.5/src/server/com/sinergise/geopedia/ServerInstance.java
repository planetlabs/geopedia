package com.sinergise.geopedia;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.cluster.swift.SwiftAccount;
import com.sinergise.common.cluster.swift.SwiftTileURLProvider;
import com.sinergise.common.geometry.tiles.IsTileProvider;
import com.sinergise.common.geometry.tiles.TiledDatasetProperties;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.app.Main;
import com.sinergise.geopedia.app.SysTables;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.config.ServerConfiguration;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.geopedia.core.entities.baselayers.TiledBaseLayer;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.db.DB;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.db.TranslationServiceImpl;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.geometry.height.Heights;
import com.sinergise.geopedia.util.LinkStorage;
import com.sinergise.java.geometry.tiles.TiledDatasetPropertiesLoaderJava;

public class ServerInstance {

	private static final Logger logger = LoggerFactory.getLogger(ServerInstance.class);
	/*--- static --- */
	
	public static final int INSTANCE_ID_GEOPEDIASI = 0;
	public static final int INSTANCE_ID_GEOPEDIACOUK = 1;
	public static final int INSTANCE_ID_GEOPEDIACOMNG = 2;
	public static final int INSTANCE_ID_GEOPEDIACZ = 3;
	public static final int INSTANCE_ID_GEOPEDIATZ = 4;
	public static final int INSTANCE_ID_GEOPEDIASEN = 5;
	
	private static ServerInstance[] allInstances;
	
	public static ServerInstance[] allInstances() {
		return allInstances;
	}
	private static int maxInstanceId = 0;
	public static void initialize(ArrayList<ServerInstance> instances) {
		
		for (ServerInstance i:instances) {
			if (i.getId()>maxInstanceId) {
				maxInstanceId=i.getId();
			}
		}
		allInstances = new ServerInstance[maxInstanceId+1];
		for (ServerInstance i:instances) {
			allInstances[i.getId()]=i;
		}
		
	}

	public static int getMaxInstanceId() {
		return maxInstanceId;
	}
	public static ServerInstance getInstance(String domainName) throws GeopediaException {
		if (StringUtil.isNullOrEmpty(domainName)) { // TODO: add check if dev mode
			return getInstance(INSTANCE_ID_GEOPEDIASI);
		}
			
		for (ServerInstance i:allInstances) {
			if (i!=null && domainName.equalsIgnoreCase(i.getDomainName())) {
				return i;
			}
		}
		throw new GeopediaException(GeopediaException.Type.INVALID_INSTANCE, "No instance found for domain "+domainName, null);
	}
	
	public static ServerInstance getInstance(int instanceId) throws GeopediaException {
		if (instanceId<0 || instanceId>maxInstanceId) {
			throw new GeopediaException(GeopediaException.Type.INVALID_INSTANCE);
		}
		ServerInstance instance = allInstances[instanceId];
		if (instance==null)
			throw new GeopediaException(GeopediaException.Type.INVALID_INSTANCE);
		return instance;
	}

	/*--- instance --- */
	
	public InstanceConfiguration config;
	DBPoolHolder dbPoolHolder;
	MetaData meta;
	TranslationServiceImpl translationService;
	DB db;
	SysTables sysTables;
	CRSSettings crsSettings;
	private LinkStorage webLinkStorage = LinkStorage.EMPTY_STORAGE;
	private Heights heights = new Heights();
	private ScriptableObject sharedScriptableObject;
	
	
	public ServerInstance (CRSSettings crsSettings, InstanceConfiguration config, ServletContext context, ScriptableObject shared) throws SQLException {
		this.config = config;
		this.sharedScriptableObject = shared;
		this.crsSettings = crsSettings;
		dbPoolHolder = new DBPoolHolder(config);
		translationService = new TranslationServiceImpl(dbPoolHolder);
		meta = new MetaData(dbPoolHolder, translationService);
		db = new DB(dbPoolHolder, meta);
		sysTables = new SysTables(dbPoolHolder);
		
		/**weblinks **/
		try {
		String weblinksLoc = getServerConfiguration().getStringProperty(ServerConfiguration.PROP_WEBLINKS);
    	if (weblinksLoc != null && weblinksLoc.trim().length() > 0) {
    		weblinksLoc=weblinksLoc.trim();
    		if (!weblinksLoc.startsWith("/") && context != null) {
    			weblinksLoc = context.getRealPath("/"+weblinksLoc);
    		}
    		webLinkStorage = new LinkStorage(new File(weblinksLoc));
    	}
    	} catch (Throwable th) {
    		logger.error("Failed to load WEBLINKS", th);    		
    	}
    	/** heights **/
    	try {
    		heights.readData(getServerConfiguration());
    	} catch (Throwable th) {
    		logger.warn("Failed to initialize Heights provider!", th);    		
    	}
    	
    	logger.trace("Loading dataset configurations...");
    	reloadBaseLayerConfigurations(config.commonCfg);
    	
    	config.commonCfg.hasDMV = config.serverCfg.getStringProperty(ServerConfiguration.PROP_DMVFILE) != null;
	}
	
	
	

    public void reloadBaseLayerConfigurations(Configuration config) {
    	
    	if (config.datasetsConf == null || config.datasetsConf.length==0) {
    		return;
    	}
    	for (BaseLayer bl:config.datasetsConf) {
    		if (!(bl instanceof TiledBaseLayer)) {
    			continue;
    		}
    		TiledBaseLayer tiledBl = (TiledBaseLayer)bl;
    		IsTileProvider tileProvider = tiledBl.getTileProvider();
    		TiledDatasetProperties tdp = tileProvider.getDatasetProperties();
    		try {
    			if (tileProvider instanceof SwiftTileURLProvider) {
	    			SwiftTileURLProvider swiftTileProvider = (SwiftTileURLProvider) tileProvider;
	    			SwiftAccount account = swiftTileProvider.getAccount();
	    			if (!account.hasAccountToken()) {
	    				if (!Main.getSwiftClient().login(account)) {
	    					logger.error("Failed to login to account: "+account);
	    				}
	    			}
	    		}
				TiledDatasetPropertiesLoaderJava.loadTiledDatasetProperties(tdp, tileProvider.getDatasetPropertiesConfigurationURL());
			} catch (Exception e) {
				logger.error("Failed to load dataset properties for dataset: id:"+tiledBl.id+" name:"+tiledBl.name,e );
			}
    	}
    	
    }
	
	
	public int getId() {
		return config.instanceId;
	}
	public String getDomainName() {
		return config.instanceDomainName;
	}

	
	
	public InstanceConfiguration getConfiguration() {
		return config;
	}
	public ServerConfiguration getServerConfiguration() {
		return config.serverCfg;
	}


	public DBPoolHolder getDBPoolHolder() {
		return dbPoolHolder;
	}

	public DB getDB() {
		return db;
	}

	public MetaData getMetaData() {
		return meta;
	}

	public SysTables getSysTables() {
		return sysTables;
	}
	
	public TranslationServiceImpl getTranslationService() {
		return translationService;
	}

	public Configuration getCommonConfiguration() {
		return config.commonCfg;
	}
	
	public CRSSettings getCRSSettings() {
		return crsSettings;
	}

	public Heights getHeights() {
		return heights;
	}
	
	public LinkStorage getLinkStorages() {
		return webLinkStorage;
	}

	public ScriptableObject getJSSharedScope() {
		return sharedScriptableObject;
	}
}
