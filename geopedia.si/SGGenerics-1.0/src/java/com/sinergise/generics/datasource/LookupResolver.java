package com.sinergise.generics.datasource;

import java.util.HashMap;

import org.w3c.dom.Element;

import com.sinergise.common.util.format.Locale;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.LookupPrimitiveValue;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.server.GenericsServerSession;

public class LookupResolver {

	// TODO
	// - caching should be implemented on entitydatasource
	
	private static class LookupEntityKey {
		
		String lookupID;
		String locale;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((locale == null) ? 0 : locale.hashCode());
			result = prime * result
					+ ((lookupID == null) ? 0 : lookupID.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LookupEntityKey other = (LookupEntityKey) obj;
			if (locale == null) {
				if (other.locale != null)
					return false;
			} else if (!locale.equals(other.locale))
				return false;
			if (lookupID == null) {
				if (other.lookupID != null)
					return false;
			} else if (!lookupID.equals(other.lookupID))
				return false;
			return true;
		}
		
	}
	
	private static HashMap<LookupEntityKey,HashMap<String,String>> lookupCache = new HashMap<LookupEntityKey, HashMap<String,String>>();
	
	
	public static void purgeLookupCache() {
		lookupCache.clear();
	}
	
	public static void purgeLookupCache(String lookupID, Locale locale) {
		LookupEntityKey lookupKey = new LookupEntityKey();
		lookupKey.lookupID = lookupID;
		lookupKey.locale = locale.toString();		
		lookupCache.remove(lookupKey);
	}
	
	private static HashMap<String,String> getLookupFromCache(String lookupID, Locale locale) {
		LookupEntityKey lookupKey = new LookupEntityKey();
		lookupKey.lookupID = lookupID;
		lookupKey.locale = locale.toString();		
		return lookupCache.get(lookupKey);
	}
	
	private static HashMap<String,String> putLookupToCache(String lookupID, String keyName, String [] labels, GenericsServerSession gSession) {
		HashMap<String, String> lookup = new HashMap<String, String>();		
		try {
			ArrayValueHolder avh = DatasourceFactory.instance().
				getData(lookupID,  null, -1, -1, gSession);
			for (ValueHolder eoVh:avh) {
				EntityObject eo = (EntityObject)eoVh;
				String key = EntityUtils.getStringValue(eo, keyName);
				String value="";
				for (String l:labels) {
					value+=" "+EntityUtils.getStringValue(eo, l);
				}
				lookup.put(key, value);
			}
			LookupEntityKey lookupKey = new LookupEntityKey();
			lookupKey.lookupID = lookupID;
			Locale locale = Locale.getDefault();
			if (gSession!=null)
				locale = gSession.getLocale();
			lookupKey.locale = locale.toString();
			lookupCache.put(lookupKey, lookup);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lookup;
	}
	
	
	
	
	public static PrimitiveValue resolveLookup(Element lookupElement, PrimitiveValue lookupField, GenericsServerSession gSession) {
		if (lookupField==null) return null;
		
		String lookupID = lookupElement.getAttribute(MetaAttributes.LOOKUP_SOURCE);
		String labels = lookupElement.getAttribute(MetaAttributes.LOOKUP_LABELS);
		String keys = lookupElement.getAttribute(MetaAttributes.LOOKUP_KEYS);
		Locale locale = Locale.getDefault();
		if (gSession!=null)
			locale = gSession.getLocale();

		HashMap<String,String> lookupMap = getLookupFromCache(lookupID, locale);
		if (lookupMap==null) {
			String [] lookupLabels = labels.split(",");
			lookupMap = putLookupToCache(lookupID, keys, lookupLabels, gSession);
		}
		LookupPrimitiveValue lupv = new LookupPrimitiveValue(lookupField);
		lupv.lookedUpValue = lookupMap.get(lookupField.value);
		return lupv;
	}
}
