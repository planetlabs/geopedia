package com.sinergise.geopedia.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;

import com.sinergise.geopedia.core.entities.WebLink;
import com.sinergise.geopedia.core.entities.WebLink.LinksCollection;

public class LinkStorage {
	
	private static final String DEFAULT_LANGUAGE = "si";

	public static final LinkStorage EMPTY_STORAGE = new LinkStorage();
	
	private HashMap<String,LinksCollection > linkStorage = new HashMap<String,LinksCollection>();
	
	private LinkStorage() {}
	
	public LinkStorage(File linksDirectory) {
		if (!linksDirectory.isDirectory()) {
			throw new IllegalArgumentException(linksDirectory.getPath()+" does not exist or is not a directory!");
		}
		
		File[] langFiles = linksDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.contains(".properties"))
					return true;
				return false;
			}
		});
		
		if (langFiles.length>0) {
			// build languages
			for (File lang:langFiles) {
				try {
					processLinkFile(lang);
				} catch (Exception ex) {
					System.err.println("Error while processing language file: "+lang.getName());
					ex.printStackTrace();
				}
			}
		}
	}
	
	
	public LinksCollection getLinksForLanguage(String lang) {
		LinksCollection ll = linkStorage.get(lang);
		if (ll!=null)
			return ll;
		return linkStorage.get(DEFAULT_LANGUAGE);
	}
	
	
	
	private void processLinkFile(File linkFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Properties linkProp = new Properties();	
		linkProp.load(new InputStreamReader(new FileInputStream(linkFile), "UTF8"));
		
		String language = linkProp.getProperty("language", null);
		if(language == null)
			throw new RuntimeException(linkFile.getName()+" is missing 'language' property!");
		
		String linkCountStr = linkProp.getProperty("linkCount",null);
		if (linkCountStr == null)
			throw new RuntimeException(linkFile.getName()+" is missing 'linkCount' property!");
		int linkCount = Integer.parseInt(linkCountStr);
		
		LinksCollection  links = new LinksCollection();
		linkStorage.put(language,links);
		for (int i=0;i<linkCount;i++) {
			String name = linkProp.getProperty("link"+i+".name",null);
			String displayName = linkProp.getProperty("link"+i+".displayName",null);
			String groupName = linkProp.getProperty("link"+i+".group",WebLink.SYSTEM_GROUP);
			String URL  = linkProp.getProperty("link"+i+".URL",null);
			String description = linkProp.getProperty("link"+i+".description","");
			if (URL==null || name == null)
				continue;
			WebLink.Group group = links.get(groupName);
			if (group==null) {
				group= new WebLink.Group();
				links.put(groupName,group);
			}
			
			WebLink wl = new WebLink();
			wl.displayName = displayName;
			wl.URL = URL;
			wl.name = name;
			wl.description = description;
			group.put(name,wl);
			
		}

	}
	
	
}
