package com.sinergise.java.util.xml;

import org.xml.sax.SAXException;

public interface SAXOutputProducer {
	public void writeData(SAXFromMemWriter out) throws SAXException;
}
