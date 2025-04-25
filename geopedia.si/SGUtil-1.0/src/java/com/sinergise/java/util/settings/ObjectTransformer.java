package com.sinergise.java.util.settings;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import com.sinergise.common.util.settings.ResolvedType;

public class ObjectTransformer {
	boolean                 storeIDs      = false;
	HashMap<String, String> outKeys       = new HashMap<String, String>();
	{
		outKeys.put(OutputKeys.METHOD, "xml");
		outKeys.put(OutputKeys.STANDALONE, "yes");
		outKeys.put(OutputKeys.INDENT, "yes");
	}
	
	public ObjectTransformer() {
		super();
	}
	
	protected Transformer createTransformer() throws TransformerConfigurationException {
		final Transformer tr = ObjectStorage.getDefaultTransformerFactory().newTransformer();
		for (final Map.Entry<String, String> e : outKeys.entrySet()) {
			tr.setOutputProperty(e.getKey(), e.getValue());
		}
		return tr;
	}
	
	public <T> void transform(final String rootName, final T rootObj, final ResolvedType<T> rootType, final OutputStream os) throws TransformerException {
		transform(rootName, rootObj, rootType, new StreamResult(os));
	}
	
	public <T> void transform(final String rootName, final T rootObj, final ResolvedType<T> rootType, final StreamResult res) throws TransformerException {
		final SAXSource src = new SAXSource();
		src.setXMLReader(new SettingsToXMLWriter(rootName, rootObj, rootType, storeIDs));
		createTransformer().transform(src, res);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> void transform(final String rootName, final T rootObj, final StreamResult res) throws TransformerException {
		transform(rootName, rootObj, new ResolvedType(rootObj.getClass()), res);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> void transform(final String rootName, final T rootObj, final OutputStream os) throws TransformerException {
		transform(rootName, rootObj, new ResolvedType(rootObj.getClass()), os);
	}
	
	public void setStoreIDs(final boolean storeIDs) {
		this.storeIDs = storeIDs;
	}
	
	/**
	 * @see OutputKeys
	 */
	public void setOutputProperty(final String key, final String value) {
		outKeys.put(key, value);
	}
}
