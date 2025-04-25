package com.sinergise.generics.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PropertyFileLanguage extends Language{

	private static final Logger logger = LoggerFactory.getLogger(PropertyFileLanguage.class);
	private Properties langProp;
	public PropertyFileLanguage(String filename) throws FileNotFoundException, IOException {
		this(new File(filename));
	}
	
	
	public PropertyFileLanguage(File f) throws FileNotFoundException, IOException {
		String name = f.getName();
		String language = name.substring(0, name.indexOf('.'));
		initialize(f,language);
	}
	
	public PropertyFileLanguage(File f, String language) throws FileNotFoundException, IOException {
		initialize(f,language);
	}
	private void initialize(File f, String language)  throws FileNotFoundException, IOException {
		String name = f.getName();
		logger.info("Loading '{}' language property file '{}' ",language,name);
		langProp = new Properties();
		try {
			InputStreamReader isReader = null;
			Class<InputStreamReader> c = InputStreamReader.class;
			Constructor<InputStreamReader> constr = c.getConstructor(InputStream.class, String.class);
			isReader = constr.newInstance(new FileInputStream(f), "UTF8");			
			Method m = langProp.getClass().getMethod("load", Reader.class);
			m.invoke(langProp, isReader);
		} catch (Exception e) {
			langProp.load(new FileInputStream(f));
			logger.warn("UTF-8 property files are not supported for this java version!");
		}
		//langProp.load(new InputStreamReader(new FileInputStream(f), "UTF8"));
		initialize(language);
	}
	
	
	@Override
	public String getLanguageString(String key) {
		return  langProp.getProperty(key);
	}

}
