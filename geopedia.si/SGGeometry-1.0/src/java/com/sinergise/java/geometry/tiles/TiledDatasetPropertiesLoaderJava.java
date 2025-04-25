package com.sinergise.java.geometry.tiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sinergise.common.geometry.tiles.TiledDatasetProperties;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.java.util.state.StateHelper;
import com.sinergise.java.util.state.StateUtilJava;

public class TiledDatasetPropertiesLoaderJava {
	private static final Logger logger = LoggerFactory.getLogger(TiledDatasetPropertiesLoaderJava.class);
	
	public static boolean loadTiledDatasetProperties(final TiledDatasetProperties tdProperties, String propertiesConfigURL) 
		throws IOException, SAXException {
		try {
			URL propURL = new URL(propertiesConfigURL);
			StateGWT state = StateUtilJava.gwtFromJava(StateHelper.readState(propURL.openStream()));
			if (state!=null) {
				tdProperties.configureFromState(state);
				return true;
			}		
			return false;
		} catch (FileNotFoundException ex) {
			logger.trace("Didn't find property file for dataset at: "+propertiesConfigURL,ex);
		}
		return false;
	}
}
