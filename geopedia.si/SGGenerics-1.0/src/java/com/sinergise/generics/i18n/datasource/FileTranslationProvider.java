package com.sinergise.generics.i18n.datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.i18n.Translations;

public class FileTranslationProvider extends DatasourceTranslationProvider {
	private File translationFile;
	public FileTranslationProvider(TypeAttribute ta, String datasourceName, File f) throws FileNotFoundException, IOException {
		super(ta, datasourceName);
		if (!f.exists())
			throw new RuntimeException("File not found: '"+f.getName()+"'");
		translationFile = f;
	}
	
	
	private Translations getDefaultTranslation () {
		return null;
	}
	
	@Override
	public Translations getTranslations(String language) {
		
		try {
			DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();			
			Document xmlDocument = dbuilder.parse(translationFile.toURI().toString());
			
			Element dsHolderNode = (Element) xmlDocument.getFirstChild();
			String entityAttributeName = typeAttribute.getName();
			NodeList trList = dsHolderNode.getElementsByTagName("DS-"+datasourceName);
			if (trList.getLength()<=0) 
				return getDefaultTranslation();
			for (int i=0;i<trList.getLength();i++) {					
				Element trHolder = (Element) trList.item(i);
				String eaName = trHolder.getAttribute("entityAttribute");
				if (eaName!=null && entityAttributeName.equals(eaName)) { 			
					NodeList langList = trHolder.getElementsByTagName(language);
					if (langList.getLength()!=1)
						return getDefaultTranslation();
					Element langElement = (Element) langList.item(0);
					NodeList nodes = langElement.getElementsByTagName("tr");
					HashMap<String,String> translations = new HashMap<String,String>();
					for (int j=0;j<nodes.getLength();j++) {
						Element elm = (Element)nodes.item(j);
						String key = elm.getAttribute("key");
						String value = elm.getAttribute("value");
						if (key!=null && value!=null) {
							translations.put(key, value);
						}
					}
					if (translations.size()>0) 
						return new SimpleEOAttributeTranslation(translations);
					else 
						return getDefaultTranslation();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return getDefaultTranslation();
	}
	
}
