/*
 *
 */
package com.sinergise.java.util.settings;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import com.sinergise.common.util.settings.NamedTypedObject;
import com.sinergise.common.util.settings.NamedValue;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.string.StringSerializer;
import com.sinergise.java.util.xml.AttrsImplHelper;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ObjectStorage {
	static {
		UtilJava.initStaticUtils();
	}
	public static final String ID_ATTR_NAME   = "object_id";
	
	public static final String TYPE_ATTR_NAME = "object_type";
	
	public static final String NULL_ELEM_NAME = "null";
	
	static class IdAttrs extends AttrsImplHelper {
		String idString;
		
		public IdAttrs(final String idString) {
			this.idString = idString;
		}
		
		@Override
		public int getIndex(final String qName) {
			return qName.equals(ID_ATTR_NAME) ? 0 : -1;
		}
		
		@Override
		public int getLength() {
			return 1;
		}
		
		@Override
		public String getQName(final int index) {
			return index == 0 ? ID_ATTR_NAME : null;
		}
		
		@Override
		public String getType(final int index) {
			return index == 0 ? "CDATA" : null;
		}
		
		@Override
		public String getValue(final int index) {
			return index == 0 ? idString : null;
		}
	}
	
	static class MapAttrs extends AttrsImplHelper {
		ArrayList<NamedValue<String>> attrs    = new ArrayList<NamedValue<String>>();
		ArrayList<NamedTypedObject>   children = new ArrayList<NamedTypedObject>();
		
		public MapAttrs(final List<NamedTypedObject> allChildren, final String typeStr, final String id) {
			if (id != null) {
				attrs.add(new NamedValue<String>(ID_ATTR_NAME, id));
			}
			if (typeStr != null && typeStr.length() > 0) {
				storeType(typeStr);
			}
			for (final NamedTypedObject curO : allChildren) {
				if (tryToStore(curO.name, curO.expectedType, curO.value)) {
					continue;
				}
				children.add(curO);
			}
		}
		
		private boolean tryToStore(final String name, final ResolvedType type, final Object value) {
			if (!StringSerializer.canStore(type.rawType)) {
				return false;
			}
			try {
				attrs.add(new NamedValue<String>(name, StringSerializer.store(value, type.rawType)));
				return true;
			} catch(final Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		private void storeType(final String typeStr) {
			attrs.add(new NamedValue<String>(TYPE_ATTR_NAME, typeStr));
		}
		
		@Override
		public int getIndex(final String qName) {
			for (int i = 0; i < attrs.size(); i++) {
				if (attrs.get(i).name.equals(qName)) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		public int getLength() {
			return attrs.size();
		}
		
		@Override
		public String getQName(final int index) {
			return attrs.get(index).name;
		}
		
		@Override
		public String getValue(final int index) {
			return attrs.get(index).value;
		}
	}
	
	private static final TransformerFactory factory;
	static {
		System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		TransformerFactory toSet = null;
		try {
			toSet = TransformerFactory.newInstance();
		} catch(final Throwable e) {
			System.clearProperty("javax.xml.transform.TransformerFactory");
			// fallback
			try {
				toSet = TransformerFactory.newInstance();
			} catch(final Throwable ee) {
				ee.printStackTrace();
			}
		} finally {
			if (toSet != null) {
				toSet.setAttribute("indent-number", Integer.valueOf(2));
			}
			factory = toSet;
		}
	}
	
	public static final void store(final String rootName, final Object obj, final ResolvedType rt, final OutputStream os, final boolean storeIDs) {
		try {
			final SAXSource src = new SAXSource();
			src.setXMLReader(new SettingsToXMLWriter(rootName, obj, rt, storeIDs));
			//Wrap os in writer so that indenting works
			OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("utf-8"));
			try {
				final StreamResult res = new StreamResult(writer);
				final Transformer tr = factory.newTransformer();
				tr.setOutputProperty(OutputKeys.STANDALONE, "yes");
				tr.setOutputProperty(OutputKeys.INDENT, "yes");
				tr.setOutputProperty(OutputKeys.METHOD, "xml");
				
				tr.transform(src, res);
			} finally {
				writer.flush();
			}
		} catch(final IOException e) {
			throw new RuntimeException(e);
			
		} catch(final TransformerConfigurationException e) {
			throw new RuntimeException(e);
			
		} catch(final TransformerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final TransformerFactory getDefaultTransformerFactory() {
		return factory;
	}
	
	public static final void store(final String rootName, final Object obj, final OutputStream os) {
		store(rootName, obj, new ResolvedType(obj.getClass()), os, false);
	}
	
	public static final <T> T load(final InputStream stream, final T root) throws TransformerException {
		return (T)load(stream, root, new ResolvedType(root.getClass()), true);
	}
	
	public static final <T> T load(final InputStream stream, final ResolvedType<T> rootType) throws TransformerException {
		return load(stream, null, rootType, false);
	}
	
	public static final <T> T load(final InputStream stream, final T root, final ResolvedType<T> rootType, final boolean doIds) throws TransformerException {
		assert stream != null : "null stream";
		
		final SAXSource src = new SAXSource(new InputSource(stream));
		final SettingsFromXMLReader wr = new SettingsFromXMLReader(root, rootType, doIds);
		final SAXResult res = new SAXResult(wr);
		
		final Transformer tr = factory.newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.STANDALONE, "yes");
		tr.transform(src, res);
		return (T)wr.getRoot();
	}
}
