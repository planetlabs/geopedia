/**
 * 
 */
package com.sinergise.java.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class DefaultContentHandler implements ContentHandler {
	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
	// Do nothing
	}
	
	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
	// Do nothing
	}
	
	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
	// Do nothing
	}
	
	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
	// Do nothing
	}
	
	@Override
	public void setDocumentLocator(final Locator locator) {
	// Do nothing
	}
	
	@Override
	public void skippedEntity(final String name) throws SAXException {
	// Do nothing
	}
	
	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
	// Do nothing
	}
	
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
		startElement(qName, atts);
	}
	
	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		endElement(qName);
	}
	
	protected abstract void startElement(String qName, Attributes atts) throws SAXException;
	
	protected abstract void endElement(String qName) throws SAXException;
	
	@Override
	public void startDocument() throws SAXException {
	// Do nothing
	}
	
	@Override
	public void endDocument() throws SAXException {
	// Do nothing
	}
}