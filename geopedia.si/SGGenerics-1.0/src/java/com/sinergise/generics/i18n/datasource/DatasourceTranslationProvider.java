package com.sinergise.generics.i18n.datasource;

import java.io.File;

import org.w3c.dom.Element;

import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.datasource.EntityDatasource;
import com.sinergise.generics.i18n.Translations;

public abstract class DatasourceTranslationProvider {
	
	private static final org.slf4j.Logger sLogger =
        org.slf4j.LoggerFactory.getLogger(DatasourceTranslationProvider.class); 
	
	
	protected TypeAttribute typeAttribute;
	protected String datasourceName;
	
	protected DatasourceTranslationProvider(TypeAttribute typeAttribute, String datasourceName) {
		this.typeAttribute = typeAttribute;
		this.datasourceName = datasourceName;
	}
	
	public TypeAttribute getTypeAttribute() {
		return typeAttribute;
	}

	
	public static DatasourceTranslationProvider create(EntityDatasource ds, Element element, String basePath) {
		String type = element.getAttribute("type");
		String entityAttribute = element.getAttribute("entityAttribute");
		if (type==null)
			throw new IllegalArgumentException("Missing attribute 'type'");
		if (entityAttribute ==null)
			throw new IllegalArgumentException("Missing attribute 'entityAttribute'");
		
		TypeAttribute ta = ds.getEntityType().getAttribute(entityAttribute);
		if (ta==null)
			throw new IllegalArgumentException("EntityAttribute '"+entityAttribute+"' does not exist for EntityType '"+ds.getEntityType().getName()+"'");
		
		if (type.equalsIgnoreCase("xmlfile")) {
			String fileName = element.getAttribute("file");
			if (fileName == null)
				throw new IllegalArgumentException("Missing attribute 'file'");
			try {
				FileTranslationProvider ftp = new FileTranslationProvider(ta, ds.getDatasourceName(), new File(basePath+fileName));
				return ftp;
			} catch (Exception e) {
				sLogger.error("Unable to access file '"+fileName+"'", e);
			}
		}
		throw new IllegalArgumentException("Unsupported type '"+type+"'");
	}
	public abstract Translations getTranslations(String language);

}
