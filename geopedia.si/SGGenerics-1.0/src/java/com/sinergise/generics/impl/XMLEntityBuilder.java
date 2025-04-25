package com.sinergise.generics.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.TypeAttribute.DatabaseIgnore;

public class XMLEntityBuilder {
	private static final Logger logger = LoggerFactory.getLogger(XMLEntityBuilder.class);
	
	public static EntityType inspect (Element element) {
		
		String entityName = element.getAttribute(MetaAttributes.TYPE);
		EntityType entity = GeneratedEntityTypeStorage.getInstance().createEntityType(entityName);
		NodeList childNodes = element.getElementsByTagName("EntityAttribute");
		int taId=0;
		for (int i=0;i<childNodes.getLength();i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType()==Node.ELEMENT_NODE) {
				Element ea = (Element) node;
				if (MetaAttributes.isFalse(ea.getAttribute(MetaAttributes.IGNORE))) {
					if (ea.getNodeName().equalsIgnoreCase(XMLTags.EntityAttribute)) {
						String type = ea.getAttribute(MetaAttributes.TYPE);
						if (type==null || type.length()==0) {
							logger.error(XMLTags.EntityAttribute+" '{}' is missing "+MetaAttributes.TYPE+ " attribute in entity '"+entityName+"'!", ea.getAttribute(MetaAttributes.NAME));
							throw new RuntimeException("Type for attribute: '"+ea.getAttribute(MetaAttributes.NAME)+"' in entity '"+entityName+"' is undefined!");
						}
						TypeAttribute ta = new TypeAttribute(taId,
								ea.getAttribute(MetaAttributes.NAME),
								Integer.parseInt(ea.getAttribute(MetaAttributes.TYPE)),
								true);
						
						
						String sqlTypeStr =ea.getAttribute(MetaAttributes.SQL_TYPE);
						if (sqlTypeStr!=null && sqlTypeStr.length()>0)
							ta.setSQLType(Integer.parseInt(sqlTypeStr));
						
						
						String dbIgnore = ea.getAttribute(MetaAttributes.DBIGNORE);
						if (dbIgnore!=null&& dbIgnore.trim().length()>0) {
							if (Boolean.toString(true).equalsIgnoreCase(dbIgnore) || MetaAttributes.VAL_DBIGNORE_FULL.equals(dbIgnore)) {
								ta.setDBIgnore(DatabaseIgnore.FULL);
							} else if(MetaAttributes.VAL_DBIGNORE_RONLY.equals(dbIgnore)) {
								ta.setDBIgnore(DatabaseIgnore.READONLY);
							} else if(MetaAttributes.VAL_DBIGNORE_WONLY.equals(dbIgnore)) {
								ta.setDBIgnore(DatabaseIgnore.WRITEONLY);
							}
						}
						logger.debug(XMLTags.EntityAttribute+": {} type: {}",ta.getName(), ta.getPrimitiveType());
						if (ea.getAttribute(MetaAttributes.PRIMARYKEY)!=null) { // TODO maybe check if it's really true
							entity.setPrimaryKeyId(taId);
						}
						entity.addTypeAttribute(ta);
						taId++;
					}
				}
			}			
		}
		return entity;
	}
}
