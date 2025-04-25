package com.sinergise.generics.core.util;

import java.util.Map;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.sinergise.generics.core.MetaAttributes;

public class XMLUtils {
	
	public static Element findElementByName(Element base, String name) {
		if (name.equals(base.getNodeName())) {
			return base;
		}
		NodeList nodes = base.getElementsByTagName(name);
		if (nodes==null || nodes.getLength()==0)
			return null;
		for (int i=0;i<nodes.getLength();i++) {
			Node chNode = nodes.item(i);
			Element found = findElementByName((Element)chNode,name);
			if (found!=null)
				return found;
		}
		return null;
	}
	
	
	
	public static Element findElementByAttributeValue(Element base, String attributeName, String attributeValue) {
		if (base.hasAttribute(attributeName)) {
			if (base.getAttribute(attributeName).equals(attributeValue)) {
				return base;
			}
		}

		NodeList children = base.getChildNodes(); 
		for (int i=0;i<children.getLength();i++) {
			Node chNode = children.item(i);
			if (chNode.getNodeType() == Node.ELEMENT_NODE) {
				Element found = findElementByAttributeValue((Element)chNode,attributeName,attributeValue);
				if (found!=null)
					return found;
			}
		}
	return null;
	}
	public static Element findEntityByType(Element base, String type) 
	{
			if (base.hasAttribute(MetaAttributes.TYPE)) {
				if (base.getAttribute(MetaAttributes.TYPE).equals(type)) {
					return base;
				}
			}

			NodeList children = base.getChildNodes(); 
			for (int i=0;i<children.getLength();i++) {
				Node chNode = children.item(i);
				if (chNode.getNodeType() == Node.ELEMENT_NODE) {
					Element found = findEntityByType((Element)chNode,type);
					if (found!=null)
						return found;
				}
			}
		return null;
	}
	
	
	public static void joinAttributeMaps(Map<String,String> dst, Map<String,String> src) {
		if (src==null||dst==null)
			return;
		for (String key:src.keySet()) {
			dst.put(key, src.get(key));
		}
	}
	
	public static void elementToAttributeMap(Element element, Map<String,String> map) {
		if (element == null)
			return;
		NamedNodeMap attributes = element.getAttributes();
		for (int i=0;i<attributes.getLength();i++)  {
			Node at = attributes.item(i);
			map.put(at.getNodeName(),at.getNodeValue());
		}
		map.put(MetaAttributes.ELEMENT_TYPE, element.getNodeName());
	}
}
