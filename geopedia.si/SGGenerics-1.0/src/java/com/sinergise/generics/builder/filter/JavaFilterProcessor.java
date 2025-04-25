package com.sinergise.generics.builder.filter;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.util.FilterProcessorBase;
import com.sinergise.generics.java.GenericsSettings;

public class JavaFilterProcessor extends FilterProcessorBase{

	private GenericsSettings settings;
	private Map<String,Element> entityMetadataMap = null;
	public JavaFilterProcessor (GenericsSettings settings, String entityTypeName) {
		this.settings = settings;
		createMetadataMap(entityTypeName);
	}

	private static String conditionForRegexp(String string) {	
		string = string.replace("*","\\E(.*)\\Q");
		string = "\\Q"+string+"\\E";
		return string;
	}
	
	@Override
	protected boolean matchesString(String filterValue, String valueToMatch) {
		return valueToMatch.matches(conditionForRegexp(filterValue));
	}
	
	
	private void createMetadataMap(String entityTypeName) {
		entityMetadataMap = new HashMap<String, Element>();
		Element entityMetadata = settings.getEntityMetadataMap().get(entityTypeName);

		if (entityMetadata!=null) {
			NodeList ents = entityMetadata.getElementsByTagName(XMLTags.EntityAttribute);
			for (int i=0;i<ents.getLength();i++) {
				Element el = (Element)ents.item(i);
				entityMetadataMap.put(el.getAttribute(MetaAttributes.NAME), el);
			}
		}
	}
	@Override
	protected String getMetaAttribute(String entityAttributeName,
			String metaAttributeName) {

		Element element  = entityMetadataMap.get(entityAttributeName);
		if (element==null)
			return null;
		return element.getAttribute(metaAttributeName);
	}
		
	
	public ArrayValueHolder applyFilter(SimpleFilter filter, ArrayValueHolder values) {		
		if (filter==null|| filter.getFilterData()==null)
			return values;
		ArrayValueHolder avh = new ArrayValueHolder(values.getType());
		for (ValueHolder vh:values) {
			EntityObject eo = (EntityObject)vh;
			if (matchesFilter(filter, eo)) {
				avh.add(eo);
			}
		}
		return avh;
	}


}
