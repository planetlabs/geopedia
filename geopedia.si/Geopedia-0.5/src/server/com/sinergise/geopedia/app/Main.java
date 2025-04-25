package com.sinergise.geopedia.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.mortbay.xml.XmlConfiguration;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import com.sinergise.common.cluster.PasswordCredentials;
import com.sinergise.common.cluster.swift.SwiftClusterSinergise;
import com.sinergise.common.cluster.swift.SwiftCredentialsProvider;
import com.sinergise.common.util.server.ServersCluster;
import com.sinergise.common.util.server.ServersClusterMap;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.configuration.cluster.swift.SVNData;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.SettingKeys;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.style.symbology.rhino.FillSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.JavaSymbologyUtils;
import com.sinergise.geopedia.style.symbology.rhino.LineSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.PaintingPassImpl;
import com.sinergise.geopedia.style.symbology.rhino.PointSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.SymbolizerFontImpl;
import com.sinergise.geopedia.style.symbology.rhino.SymbologyImpl;
import com.sinergise.geopedia.style.symbology.rhino.TextSymbolizerImpl;
import com.sinergise.java.cluster.swift.SwiftClient;
import com.sinergise.java.util.io.ByteArrayInputStream;
import com.sinergise.java.util.settings.ObjectStorage;
import com.sinergise.java.util.sql.LoggableStatement;

public class Main
{
	private static final String SYSPROP_GP_CONFIG_NAME = "geopedia.config";
	private static final String SYSPROP_GP_SERVER_ID = "geopedia.serverId";
	private static final String SYSPROP_INTERNAL_JS = "geopedia.internalJS";
	static String serverId;
	
	private static Map<String, String> settings;
	public static String serverName;
	private static boolean running = false;
	private static SwiftClient swiftClient;
	
	public static SwiftClient getSwiftClient() {
		return swiftClient;
	}
	
	public static boolean isRunning() {
		return running;
	}
	public static String getServerID()
	{
		return serverId;
	}
	
	private static String getServerId() throws SocketException
	{
		Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
		while (nics.hasMoreElements()) {
			NetworkInterface nic = nics.nextElement();
			
			Enumeration<InetAddress> adds = nic.getInetAddresses();
			while (adds.hasMoreElements()) {
				InetAddress addr = adds.nextElement();
				if (addr.isLoopbackAddress() || !(addr instanceof Inet4Address))
					continue;
				
				return "Pedia@"+addr;
			}
		}
		
		throw new IllegalStateException("No NICs found");
	}
	
	static {
		try {
			serverId = getServerId();
			JavaSymbologyUtils.initialize();
		} catch (SocketException e) {
			throw new IllegalStateException(e);
		}
	}
	
    public static void serverStaticInit() throws Exception {
//        SymLoader.init();
//        SysTables.init();
//        MetaServiceImpl.initStatic();
    }
    
    
    private static void initializeSwift() {
		//TODO: refactor
		ServersClusterMap.initialize();
		ServersClusterMap.registerCluster(SwiftClusterSinergise.SWIFT_CLUSTER);
		ServersClusterMap.registerCluster(ServersCluster.createPlainHTTPServersCluster("pediaFileserver", new String[]{
				"http://dof501.geopedia.si/","http://dof502.geopedia.si/"}));
		
		SwiftCredentialsProvider.Map credentialsMap = (SwiftCredentialsProvider.Map) SwiftCredentialsProvider.initialize(new SwiftCredentialsProvider.Map());
		credentialsMap.addCredentials(SVNData.ACCOUNT_SVN_GEOPEDIA, new PasswordCredentials(settings.get(SettingKeys.swift_svn_geopedia_username), settings.get(SettingKeys.swift_svn_geopedia_password)));
		credentialsMap.addCredentials(SVNData.ACCOUNT_SVN_RASTERS, new PasswordCredentials(settings.get(SettingKeys.swift_svn_rasters_username), settings.get(SettingKeys.swift_svn_rasters_password)));
		
		
		HttpParams params = new BasicHttpParams();
		 PoolingClientConnectionManager pccm = new PoolingClientConnectionManager();
		 pccm.setMaxTotal(15);
		 pccm.setDefaultMaxPerRoute(15);
		 HttpClient httpClient = new DefaultHttpClient(pccm, params);
		 swiftClient = new SwiftClient(httpClient);
		
	}
    
	public static void main(String[] args) throws Exception {
	    serverStaticInit();    
	    System.out.println("Starting "+serverId);
	    initialize(null);
	    XmlConfiguration configuration = new XmlConfiguration(new FileInputStream(args[0]));
	    configuration.configure();
	}

	private static ScriptableObject initializeRhinoScript(ServletContext servletContext) throws Exception {
		Context globalContext = Context.enter();
		ScriptableObject scopeShared = globalContext.initStandardObjects(null, true);
		String jsBase = System.getProperty(SYSPROP_INTERNAL_JS,"internalJS");
		if (servletContext!=null && jsBase!=null && !jsBase.startsWith(File.separator)) {
			jsBase = servletContext.getRealPath(File.separator+jsBase);
		}
		
		ScriptableObject.defineClass(scopeShared, LineSymbolizerImpl.class);
		ScriptableObject.defineClass(scopeShared, PointSymbolizerImpl.class);
		ScriptableObject.defineClass(scopeShared, FillSymbolizerImpl.class);
		ScriptableObject.defineClass(scopeShared, SymbologyImpl.class);
		ScriptableObject.defineClass(scopeShared, PaintingPassImpl.class);
		ScriptableObject.defineClass(scopeShared, TextSymbolizerImpl.class);
		ScriptableObject.defineClass(scopeShared, SymbolizerFontImpl.class);
		globalContext.evaluateReader(scopeShared, new FileReader(new File(jsBase+"/uglifyjs.1.2.5.min.js")), "uglifyjs.1.2.5.min.js", 1, null);
		globalContext.evaluateReader(scopeShared, new FileReader(new File(jsBase+"/utility.js")), "utility.js", 1, null);
		globalContext.evaluateReader(scopeShared, new FileReader(new File(jsBase+"/utilityJava.js")), "utilityJava.js",1,null);
		scopeShared.sealObject();
		return scopeShared;
	}
	
	private static Map<String, String> loadConfiguration(ServletContext servletContext) {
		if (servletContext != null) {
			return loadConfigurationFromContext(servletContext);
		}
		return loadConfigurationFromFile();
	}
	
	private static Map<String, String> loadConfigurationFromContext(ServletContext servletContext) {
		Map<String, String> settings = new HashMap<String, String>();
		
		settings.put(SettingKeys.swift_svn_rasters_username, (String) servletContext.getInitParameter(SettingKeys.swift_svn_rasters_username));
		settings.put(SettingKeys.swift_svn_rasters_password, (String) servletContext.getInitParameter(SettingKeys.swift_svn_rasters_password));
		settings.put(SettingKeys.swift_svn_geopedia_username, (String) servletContext.getInitParameter(SettingKeys.swift_svn_geopedia_username));
		settings.put(SettingKeys.swift_svn_geopedia_password, (String) servletContext.getInitParameter(SettingKeys.swift_svn_geopedia_password));
		
		settings.put(SettingKeys.db_host, (String) servletContext.getInitParameter(SettingKeys.db_host));
		settings.put(SettingKeys.db_user, (String) servletContext.getInitParameter(SettingKeys.db_user));
		settings.put(SettingKeys.db_pass, (String) servletContext.getInitParameter(SettingKeys.db_pass));
		settings.put(SettingKeys.db_basedb, (String) servletContext.getInitParameter(SettingKeys.db_basedb));
		
		settings.put(SettingKeys.updatedb_host, (String) servletContext.getInitParameter(SettingKeys.updatedb_host));
		settings.put(SettingKeys.updatedb_user, (String) servletContext.getInitParameter(SettingKeys.updatedb_user));
		settings.put(SettingKeys.updatedb_pass, (String) servletContext.getInitParameter(SettingKeys.updatedb_pass));
		settings.put(SettingKeys.updatedb_basedb, (String) servletContext.getInitParameter(SettingKeys.updatedb_basedb));
		
		return settings;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Map<String, String> loadConfigurationFromFile() {
		try {
			String settingsFileName = System.getProperty(SettingKeys.gisopedia_settings_fileName);
			Properties props = new Properties();
			InputStream is = new FileInputStream(settingsFileName);
			props.load(is);
			is.close();
			return new HashMap(props);
		}
		catch (IOException e) {
			throw new RuntimeException("Error loading configuration", e);
		}
	}
	
	public static void initialize(ServletContext servletContext) {
		initialize(servletContext, null);
	}
	
	public static void initialize(ServletContext servletContext, String configuration) {
		settings = loadConfiguration(servletContext);
		
		try {
			serverName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		initializeSwift();
		running = true;
		
		int dbPort  = 3306;
		String dbHost = settings.get(SettingKeys.db_host);
		String dbName = "common";
		String dbUsername = settings.get(SettingKeys.db_user);
		String dbPassword = settings.get(SettingKeys.db_pass);
			
		String serverIdentifier = System.getProperty(SYSPROP_GP_SERVER_ID,serverId);
		String configName= System.getProperty(SYSPROP_GP_CONFIG_NAME, configuration);
		
		Connection conn = null;
		LoggableStatement ls = null ;
		ResultSet rs = null;
		try {
			ScriptableObject shared = initializeRhinoScript(servletContext);
			conn =  DriverManager.getConnection(DBPoolHolder.urlFor(dbHost, dbPort, dbName), dbUsername, dbPassword);
			ls = new LoggableStatement(conn, "SELECT config FROM configurations WHERE serverId=? AND configName=? UNION " +
					"SELECT config FROM  configurations WHERE  configName=?");
			ls.setString(1, serverIdentifier);
			ls.setString(2, configName);			
			ls.setString(3, configName);
			rs = ls.executeQuery();
			if (rs.next()) {
				String config  = rs.getString(1);
				
				
				ByteArrayInputStream bos = new ByteArrayInputStream(config.getBytes("UTF-8"));
				InstanceConfiguration.All  configs = ObjectStorage.load(bos,
						new InstanceConfiguration.All(), new ResolvedType<InstanceConfiguration.All>(InstanceConfiguration.All.class), true);
				ArrayList<ServerInstance> instanceList = new ArrayList<ServerInstance>();
				for (InstanceConfiguration iConfig:configs.configurations) {
					instanceList.add(InstanceInitializer.createInstance(iConfig, servletContext, shared));
				}
				InstanceInitializer.initializeInstances(instanceList);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			running=false;
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
			DBUtil.close(conn);
		}
		
	}

	public static void destroy(ServletContext servletContext) {
		running=false;
	}
	
	
	
	
	
	
	
	
	
}
