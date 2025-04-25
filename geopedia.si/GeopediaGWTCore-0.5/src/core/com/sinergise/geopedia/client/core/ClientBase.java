package com.sinergise.geopedia.client.core;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.cluster.swift.SwiftClusterSinergise;
import com.sinergise.common.util.server.ServersCluster;
import com.sinergise.common.util.server.ServersClusterMap;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.client.core.i18n.ExceptionMessages;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.symbology.GWTSymbologyUtils;
import com.sinergise.geopedia.client.ui.panels.LanguageSelector;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.util.LanguageSettings;

public abstract class ClientBase {

	protected abstract void createBaseLayout(LanguageSettings languageSettings);
	
	protected void setLanguage() {
		Language lang = LanguageSelector.getCurrentLanguage();
		RemoteServices.getSessionServiceInstance().setLanguage(lang, new AsyncCallback<LanguageSettings>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(Messages.INSTANCE.errorLoadingLanguage()+": '"+ExceptionI18N.getLocalizedMessage(caught)+"'");
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(LanguageSettings result) {
				 Repo.createInstance();
				 createBaseLayout(result);	
				 ClientSession.initPinger();
				 ClientSession.notifyAlreadyLoggedIn();
				 
			}
		});
	}
	protected void loadConfigAndInitialize() {		
		
		GWTSymbologyUtils.initialize();
        ClientSession.start(new AsyncCallback<Void>() {
            public void onFailure(Throwable caught) {
            	Window.alert(ExceptionMessages.INSTANCE.InitialSessionCreationFailed());
            	caught.printStackTrace();
            }

            public void onSuccess(Void result) {
                loadConfiguration();
            }
        });
    }
	
	
	protected void loadConfiguration() {
		ServersClusterMap.initialize();
		ServersClusterMap.registerCluster(SwiftClusterSinergise.SWIFT_CLUSTER);
		ServersClusterMap.registerCluster(ServersCluster.createPlainHTTPServersCluster("pediaFileserver", new String[]{
				"http://dof501.geopedia.si/","http://dof502.geopedia.si/"}));
		
        RemoteServices.getMetaServiceInstance().getConfiguration(new AsyncCallback<Configuration>() {
            public void onFailure(Throwable caught) {
            	Window.alert(Messages.INSTANCE.errorLoadingConfiguration()+": '"+ExceptionI18N.getLocalizedMessage(caught)+"'");
            	caught.printStackTrace();
            }

            public void onSuccess(Configuration result) {
               ClientGlobals.configuration = result;
               setLanguage();
            }
        });
	}
	
}
