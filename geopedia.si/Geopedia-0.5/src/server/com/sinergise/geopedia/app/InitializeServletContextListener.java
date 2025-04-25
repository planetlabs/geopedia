package com.sinergise.geopedia.app;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitializeServletContextListener implements ServletContextListener{

	public static final String CONTEXT_PARAM_CONFIG_NAME = "CONFIGURATION";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();		
		Main.initialize(sce.getServletContext(), context.getInitParameter(CONTEXT_PARAM_CONFIG_NAME));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Main.destroy(sce.getServletContext());
	}

}
