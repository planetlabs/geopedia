package com.sinergise.geopedia.app;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.mozilla.javascript.ScriptableObject;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.core.crs.CRSSettingsCZE;
import com.sinergise.geopedia.core.crs.CRSSettingsGBR;
import com.sinergise.geopedia.core.crs.CRSSettingsNGA;
import com.sinergise.geopedia.core.crs.CRSSettingsSEN;
import com.sinergise.geopedia.core.crs.CRSSettingsSVN;
import com.sinergise.geopedia.core.crs.CRSSettingsTZA;
import com.sinergise.geopedia.server.HighlightWorkload;
import com.sinergise.geopedia.server.RenderWorkload;
import com.sinergise.java.util.UtilJava;

public class InstanceInitializer {
	
	public static ServerInstance createInstance(InstanceConfiguration config, ServletContext servletContext,ScriptableObject sharedScriptableObject) throws SQLException {
		CRSSettings crsSettings = null;
		switch (config.instanceId)  {
			case ServerInstance.INSTANCE_ID_GEOPEDIASI:
				crsSettings = new CRSSettingsSVN();
				break;
			case ServerInstance.INSTANCE_ID_GEOPEDIACOUK:
				crsSettings = new CRSSettingsGBR();
				break;
			case ServerInstance.INSTANCE_ID_GEOPEDIACOMNG:
				crsSettings = new CRSSettingsNGA();
				break;
			case ServerInstance.INSTANCE_ID_GEOPEDIACZ:
				crsSettings = new CRSSettingsCZE();
				break;
			case ServerInstance.INSTANCE_ID_GEOPEDIATZ:
				crsSettings = new CRSSettingsTZA();
				break;
			case ServerInstance.INSTANCE_ID_GEOPEDIASEN:
				crsSettings = new CRSSettingsSEN();
				break;
			default:
				throw new RuntimeException("Unsupported instanceId: "+config.instanceId);
		}
		return new ServerInstance(crsSettings, config, servletContext, sharedScriptableObject);
	}
	
	public static void initializeInstances(ServerInstance instnace) {
		ArrayList<ServerInstance> instances = new ArrayList<ServerInstance>();
		instances.add(instnace);
		initializeInstances(instances);
	}
	public static void initializeInstances(ArrayList<ServerInstance> instances) {		
		UtilJava.initStaticUtils();
		ServerInstance.initialize(instances);
		RenderWorkload.initialize();
		HighlightWorkload.initialize();
	}
}
