package com.sinergise.generics.datasource;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;

import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.i18n.Translations;
import com.sinergise.generics.i18n.datasource.DatasourceTranslationProvider;
import com.sinergise.generics.impl.GeneratedEntityTypeStorage;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.generics.server.GenericsServerSession;

public abstract class EntityDatasource {
	public static final int TYPE_ORACLE = 1;
	public static final int TYPE_XML = 2;
	public static final int TYPE_MYSQL = 3;
	public static final int TYPE_MSSQL = 4;
	public static final int TYPE_POSTGRES = 5;
	
	protected String defaultLocale = null;
	protected String id;
	protected EntityType entityType;
	protected GenericsSettings genericsSettings;
	protected int dsType;
	
	protected ArrayList<DatasourceTranslationProvider> translationProviders = null;
	
	public EntityDatasource(int dsType, Element dsElement, GenericsSettings settings) {
		this.dsType=dsType;
		this.genericsSettings = settings;
		id = dsElement.getAttribute(MetaAttributes.NAME);
		defaultLocale = dsElement.getAttribute("defaultLocale");
		
		String entityTypeName = dsElement.getAttribute(MetaAttributes.TYPE);
		entityType = GeneratedEntityTypeStorage.getInstance().getEntityType(entityTypeName);
		if (entityType == null) {
			throw new RuntimeException("Unable to find EntityType '"+entityTypeName+"'");
		}
	}
	public EntityDatasource (int dsType, String id, EntityType entityType) {
		this.dsType = dsType;
		this.id=id;
		this.entityType = entityType;
	}
	
	public void setTranslationProviders(ArrayList<DatasourceTranslationProvider> providers) {
		translationProviders = providers;
	}
	
	protected HashMap<Integer, Translations> prepareLanguageTranslations(String language) {
		if (translationProviders ==null)
			return null;
		HashMap<Integer, Translations> trMap = new HashMap<Integer, Translations>();		
		for (DatasourceTranslationProvider dsp: translationProviders) {
			Translations tr = dsp.getTranslations(language);
			trMap.put(dsp.getTypeAttribute().getId(), tr);
		}
		return trMap;
	}
	
	protected String translateEntityAttribute(HashMap<Integer,Translations> translations, String value, Integer typeAttributeID) {
		if (translations==null || translations.size()==0)
			return value;
		Translations tr = translations.get(typeAttributeID);
		if (tr==null)
			return value;
		return tr.getLanguageString(value);
	}

	public String getDatasourceName() {
		return id;
	}
	public EntityType getEntityType() {
		return entityType;
	}
	
	public abstract ArrayValueHolder getData (DataFilter filter, int fromIdx, int toIdx, GenericsServerSession gSession) throws Exception;
	public abstract ValueHolder processData(ValueHolder values, GenericsServerSession gSession) throws Exception;

}
