package com.sinergise.generics.builder.file;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.builder.Inspector;
import com.sinergise.generics.builder.InspectorException;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.impl.XmlUtils;

public class XMLEntityInspector implements Inspector{

	protected Document xmlDocument;
	protected String entitiesElementTag;
	protected HashMap<String, Element> includables;
	
	
	
	public XMLEntityInspector (Document file, String containerTag, HashMap<String, Element> includables) throws ParserConfigurationException, SAXException, IOException {
		xmlDocument = file;
		entitiesElementTag = containerTag;
		this.includables = includables;
	}
	
	protected XMLEntityInspector (Document file, String containerTag) throws ParserConfigurationException, SAXException, IOException {
		xmlDocument = file;
		entitiesElementTag = containerTag;
		this.includables = null;
	}
	@Override
	public Element inspect(Object toInspect) throws InspectorException {
		if (toInspect==null) {
			throw new NullPointerException("No element to inspect");
		}
		if (!(toInspect instanceof String))
			throw new InspectorException("Argument should be a string (table name or a view)");
		String elementName = (String) toInspect;
		NodeList xmlEntitiesList = xmlDocument.getElementsByTagName(entitiesElementTag);
		if (xmlEntitiesList.getLength()==1) {
			Element rootElement = (Element) xmlEntitiesList.item(0);
			NodeList nodes = rootElement.getChildNodes();
			for (int i=0;i<nodes.getLength();i++) {
				if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE) {
					Element elm = (Element)nodes.item(i);
					if (elm.getAttribute(MetaAttributes.TYPE).equals(elementName)) {
						return processElement(elm);
					}
				}
			}
		} else if (xmlEntitiesList.getLength()>1) 
			throw new InspectorException("XML document may contain only one '"+entitiesElementTag+"' element!");
		return null;
	}
	
	protected Element processElement (Element element) {
		String toIncludeName = element.getAttribute(MetaAttributes.META_INCLUDE);
		if (!StringUtil.isNullOrEmpty(toIncludeName) && includables != null) {
			Element toInclude = includables.get(toIncludeName);
			if (toInclude!=null) {
				
				Element master = (Element) toInclude.cloneNode(true);
				XmlUtils.combineElements(master, element, 
						MetaAttributes.NAME, MetaAttributes.NAME);
				return master;
			}
		}
		return element;
	}

}
