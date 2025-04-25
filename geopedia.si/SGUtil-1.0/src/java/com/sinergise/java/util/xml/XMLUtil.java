package com.sinergise.java.util.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLUtil {
	
	public static String getChildText(final Element parent, final String childName) {
		try {
			final NodeList nodes = parent.getElementsByTagName(childName);
			final Element childElement = (Element)nodes.item(0);
			return childElement.getTextContent();
		} catch(final Exception e) {
			// e.printStackTrace();
			// System.out.println("warn: no " + childName + " element found");
			return null;
		}
	}
	
}
