/**
 * 
 */
package com.sinergise.java.util.xml;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

public class SAXFromMemWriter implements XMLReader, ContentHandler {
	public static final class Tag {
		public String uri;
		public String localName;
		public String qName;

		public Tag(String uri, String localName, String qName) {
			super();
			this.uri = uri;
			this.localName = localName;
			this.qName = qName;
		}
		
		@Override
		public String toString() {
			return uri+" "+localName+" "+qName;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((localName == null) ? 0 : localName.hashCode());
			result = prime * result + ((qName == null) ? 0 : qName.hashCode());
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Tag other = (Tag)obj;
			if (localName == null) {
				if (other.localName != null) return false;
			} else if (!localName.equals(other.localName)) return false;
			if (qName == null) {
				if (other.qName != null) return false;
			} else if (!qName.equals(other.qName)) return false;
			if (uri == null) {
				if (other.uri != null) return false;
			} else if (!uri.equals(other.uri)) return false;
			return true;
		}
		
	}
	
	protected ContentHandler           ch;
	
	protected DTDHandler               dtd;
	
	protected EntityResolver           eRes;
	
	protected ErrorHandler             erh;
	
	protected HashMap<String, Boolean> features;
	{
		features = new HashMap<String, Boolean>();
		features.put("http://xml.org/sax/features/namespaces", TRUE);
		features.put("http://xml.org/sax/features/namespace-prefixes", FALSE);
	}
	
	protected HashMap<String, Object>  properties;
	
	protected SAXOutputProducer        producer;

	public SAXFromMemWriter(final SAXOutputProducer producer) {
		this.producer = producer;
	}
	
	@Override
	public ContentHandler getContentHandler() {
		return null;
	}
	
	@Override
	public DTDHandler getDTDHandler() {
		return null;
	}
	
	@Override
	public EntityResolver getEntityResolver() {
		return null;
	}
	
	@Override
	public ErrorHandler getErrorHandler() {
		return null;
	}
	
	@Override
	public boolean getFeature(final String name) {
		return features.get(name).booleanValue();
	}
	
	@Override
	public Object getProperty(final String name) throws SAXNotRecognizedException {
		if (properties == null) {
			throw new SAXNotRecognizedException(name + " is not in the properties map");
		}
		return properties.get(name);
	}
	
	@Override
	public void parse(final String systemId) throws IOException, SAXException {
		parse(new InputSource(systemId));
	}
	
	@Override
	public void parse(final InputSource input) throws SAXException {
		elStack = new Stack<Tag>();
		writeHeader();
		writeContent();
		writeFooter();
	}
	
	protected void writeHeader() throws SAXException {
		ch.startDocument();
	}
	
	protected void writeContent() throws SAXException {
		producer.writeData(this);
	}
	
	public void startElement(final String name) throws SAXException {
		startElement(name, (Attributes)null);
	}
	
	public void startElement(final String name, final String[][] attrNamesValues) throws SAXException {
		startElement(name, new DefaultAttributes(attrNamesValues));
	}
	
	Stack<Tag> elStack;
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.util.xml.SimpleSAXOutput#startElement(java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(final String name, final Attributes attrs) throws SAXException {
		startElement("", name, name, attrs);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.util.xml.SimpleSAXOutput#writeText(java.lang.String)
	 */
	public void writeText(final String text) throws SAXException {
		ch.characters(text.toCharArray(), 0, text.length());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.util.xml.SimpleSAXOutput#endElement(java.lang.String)
	 */
	public void endElement(final String name) throws SAXException {
		endElement("", name, name);
	}
	
	public void endCurrentElement() throws SAXException {
		final Tag tg = elStack.pop();
		ch.endElement(tg.uri, tg.localName, tg.qName);
	}
	
	public void writeTextElement(final String name, final Attributes attrs, final String text) throws SAXException {
		startElement(name, attrs);
		if (text != null && text.length() > 0) {
			writeText(text);
		}
		endCurrentElement();
	}
	
	public void writeTextElement(final String name, final String text) throws SAXException {
		writeTextElement(name, null, text);
	}
	
	protected void writeFooter() throws SAXException {
		ch.endDocument();
	}
	
	@Override
	public void setContentHandler(final ContentHandler handler) {
		ch = handler;
	}
	
	@Override
	public void setDTDHandler(final DTDHandler handler) {
		dtd = handler;
	}
	
	@Override
	public void setEntityResolver(final EntityResolver resolver) {
		eRes = resolver;
	}
	
	@Override
	public void setErrorHandler(final ErrorHandler handler) {
		erh = handler;
	}
	
	@Override
	public void setFeature(final String name, final boolean value) {
		features.put(name, Boolean.valueOf(value));
	}
	
	@Override
	public void setProperty(final String name, final Object value) {
		if (properties == null) {
			properties = new HashMap<String, Object>();
		}
		properties.put(name, value);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		ch.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		ch.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		ch.endDocument();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		ch.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		ch.endPrefixMapping(prefix);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		ch.startElement(uri, localName, qName, atts);
		elStack.push(new Tag(uri, localName, qName));
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Tag tg = elStack.peek();
		if (!tg.equals(new Tag(uri, localName, qName))) {
			throw new IllegalStateException(tg + " is not the last open element");
		}
		ch.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		ch.characters(chars, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] chars, int start, int length) throws SAXException {
		ch.ignorableWhitespace(chars, start, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		ch.processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		ch.skippedEntity(name);
	}
	
	
}
