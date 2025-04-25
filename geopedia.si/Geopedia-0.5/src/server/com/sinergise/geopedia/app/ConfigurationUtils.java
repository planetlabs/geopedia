package com.sinergise.geopedia.app;

import java.util.HashMap;

import com.google.gson.Gson;
import com.sinergise.geopedia.core.config.ThemeBaseLayers;

public class ConfigurationUtils {
	private static Gson gson = new Gson();
	public static String themeDatasetsToString(ThemeBaseLayers baseLayers) {
		return gson.toJson(baseLayers);
	}

	public static ThemeBaseLayers themeDatasetsFromString(String string) {
		try {
		return gson.fromJson(string, ThemeBaseLayers.class);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	
	public static void main(String[]args) {
		ThemeBaseLayers tds = new ThemeBaseLayers();
		Integer [][] dfl = new Integer[][]{
			new Integer[]{2},
			new Integer[]{1,3},
			new Integer[]{4}
		};
		Integer [][] pro = new Integer[][]{
				new Integer[]{2},
				new Integer[]{1,3},
				new Integer[]{4,5,0}
			};
		tds.datasets.put(ThemeBaseLayers.SETTING_DEFAULT, dfl);
		tds.datasets.put(ThemeBaseLayers.SETTING_PRO, pro);
		tds.defaultDataset=4;
		
	
		
		Gson gson = new Gson();
		HashMap<String,String> sdf = new HashMap<String,String>();
		sdf.put("VERSION","1.3.0");
		sdf.put("FORMAT","image/jpeg");
		
		System.out.println(gson.toJson(sdf));
		
	
	}

	
	
}
