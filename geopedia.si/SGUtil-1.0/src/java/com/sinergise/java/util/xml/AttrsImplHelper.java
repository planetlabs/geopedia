/*
 *
 */
package com.sinergise.java.util.xml;

import org.xml.sax.Attributes;

public abstract class AttrsImplHelper implements Attributes {
	@Override
	public int getIndex(final String uri, final String localName) {
		return getIndex(localName);
	}
	
	@Override
	public String getLocalName(final int index) {
		return getQName(index);
	}
	
	@Override
	public String getType(final String qName) {
		return getType(getIndex(qName));
	}
	
	@Override
	public String getType(final String uri, final String localName) {
		return getType(localName);
	}
	
	@Override
	public String getURI(final int index) {
		return "";
	}
	
	@Override
	public String getValue(final String qName) {
		return getValue(getIndex(qName));
	}
	
	@Override
	public String getValue(final String uri, final String localName) {
		return getValue(localName);
	}
	
	@Override
	public String getType(final int index) {
		return "CDATA";
	}
}