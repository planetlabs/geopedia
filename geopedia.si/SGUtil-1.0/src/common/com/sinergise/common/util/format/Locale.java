package com.sinergise.common.util.format;

import java.io.Serializable;
import java.util.HashMap;

import com.sinergise.common.util.string.StringUtil;



public class Locale implements Serializable {
	private static HashMap<String, Locale> localesByLanguage = new HashMap<String, Locale>();
	private static HashMap<String, Locale> localesByCountry = new HashMap<String, Locale>();

	public static final Locale SI = createLocale("sl_SI");
	public static final Locale HR = createLocale("hr_HR");
	public static final Locale EN = createLocale("en_US");
	public static final Locale MK = createLocale("mk_MK");
	public static final Locale MU = createLocale("en_MU");
	public static final Locale RO = createLocale("ro_RO");
	public static final Locale CZ = createLocale("cs_CZ");
	public static final Locale AZ = createLocale("az_AZ");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3360038678522743747L;
	public static final Locale UNKNOWN = new Locale("");
	
	private String language;
	private String country;
	
	@Deprecated /** Serialization only */
	protected Locale() {}
	
	private static Locale createLocale(String locString) {
		Locale loc = new Locale(locString);
		localesByLanguage.put(loc.getLanguage(), loc);
		if (!StringUtil.isNullOrEmpty(loc.getCountry())) {
			localesByCountry.put(loc.getCountry(), loc);
		}
		return loc;
	}
	
	private Locale (String localeName) {
		String[] parts = localeName.toLowerCase().split("[-_]");
		this.language = parts[0].toLowerCase();
		this.country = parts.length>1 ? parts[1].toUpperCase() : "";
	}
	
	
	public static Locale forName(String name) {
		if (name==null) throw new IllegalArgumentException("Can't create null locale!");
		Locale loc = resolveNameAliases(name);
		if (loc!=null) {
			return loc;
		}
		String[] parts = name.split("[-_]");
		
		String language = parts[0].toLowerCase();
		String country = parts.length>1 ? parts[1].toUpperCase() : "";

		Locale ret = getCached(language, country);
		if (ret != null) {
			return ret;
		}
		return createLocale(name.toLowerCase());
	}
	
	private static Locale getCached(String language, String country) {
		Locale ret = localesByLanguage.get(language);
		if (ret != null && ret.getCountry().equalsIgnoreCase(country)) {
			return ret;
		}
		ret = localesByCountry.get(country);
		if (ret != null && ret.getLanguage().equalsIgnoreCase(language)) {
			return ret;
		}
		return null;
	}

	/**
	 * Resolve any aliases for the locale 
	 * @param name locale name/alias
	 * @return locale name
	 */
	private static Locale resolveNameAliases(String name) {
		if (name==null) {
			return null;
		}
		if (name.equalsIgnoreCase("default")) {
			return Locale.getDefault();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return country.isEmpty() ? language : language+"-"+country;
	}

	public static Locale getDefault() {
		return I18nProvider.App.getInstance().getDefaultLocale();
	}

	public String getLanguage() {
		return language;
	}

	public String getCountry() {
		return country;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
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
		Locale other = (Locale)obj;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		return true;
	}	
	
}
