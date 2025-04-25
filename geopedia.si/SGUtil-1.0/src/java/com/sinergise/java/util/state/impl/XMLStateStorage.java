/*
 * Copyright (c) 2004 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package com.sinergise.java.util.state.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateFactory;
import com.sinergise.java.util.state.StateStorage;

/**
 * This class is an implementation of the <code>StateStorage</code> that uses a XML file to store the array of the <code>State</code>
 * objects.
 * 
 * @author dvitas
 */
public class XMLStateStorage extends DefaultStateStorage {
	/**
	 * DOCUMENT ME!
	 * 
	 * @param outStates DOCUMENT ME!
	 * @param os DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public static final void storeStates(final State[] outStates, final OutputStream os) throws IOException {
		storeStates(Arrays.asList(outStates), os);
	}
	
	private static final void storeStates(final List<State> outStates, final OutputStream os) throws IOException {
		try {
			// setup XML
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.newDocument();
			final Element mainEl = doc.createElement("states");
			doc.appendChild(mainEl);
			
			for (int i = 0; i < outStates.size(); i++) {
				final State s = outStates.get(i);
				final Element el = doc.createElement("state");
				mainEl.appendChild(el);
				saveState(doc, el, s);
			}
			
			if (os == null) {
				return;
			}
			
			// use this instead of casting because it doesn't need to be the Crimson parser
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(os));
			
			// ((XmlDocument)doc).write(os);
			// close file buffers
			os.flush();
			os.close();
		} catch(final Exception e) {
			e.printStackTrace();
			
			final IOException exc = new IOException("Unable to store state! " + e.getMessage());
			exc.initCause(e);
			throw exc;
		}
	}
	
	private static final void readStateFromNode(final Node node, final State state) {
		final NamedNodeMap nnm = node.getAttributes();
		
		// load attributes
		String key;
		
		// load attributes
		String value;
		
		for (int i = 0; i < nnm.getLength(); i++) {
			key = nnm.item(i).getNodeName();
			value = nnm.item(i).getNodeValue();
			
			// if(key.startsWith("ID")) {
			// if(!value.startsWith("null"))
			// state.setID(value);
			// }
			// else
			state.putString(key, value);
		}
		
		// and childs if any
		final NodeList nl = node.getChildNodes();
		
		for (int i = 0; i < nl.getLength(); i++) {
			final Node child = nl.item(i);
			
			if (child instanceof Element) {
				final State s = state.createState(child.getNodeName());
				readStateFromNode(child, s);
			}
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param is DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public static final State[] loadStates(final InputSource is) throws IOException {
		final List<State> retList = new ArrayList<State>();
		loadStates(is, retList);
		
		final State[] ret = new State[retList.size()];
		retList.toArray(ret);
		
		return ret;
	}
	
	private static final void loadStates(final InputSource is, final List<State> retStates) throws IOException {
		if (is == null) {
			return;
		}
		
		try {
			if (is.getByteStream() == null && is.getCharacterStream() == null) {
				return;
			}
			
			// setup DOM and parse
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(is);
			NodeList nl = doc.getChildNodes();
			
			// node "states" should be the first node in the doc
			Node statesNode = null;
			
			for (int i = 0; i < nl.getLength(); i++) {
				if ("states".equals(nl.item(i).getNodeName())) {
					statesNode = nl.item(i);
					
					break;
				}
			}
			
			if (statesNode == null) {
				throw new IOException("Unable to find node for states in " + is.toString());
			}
			
			// nl is now states so get its childs
			nl = statesNode.getChildNodes();
			
			for (int i = 0; i < nl.getLength(); i++) {
				final State state = StateFactory.createState();
				final Node node = nl.item(i);
				
				if (!(node instanceof Element)) {
					continue;
				}
				
				readStateFromNode(node, state);
				retStates.add(state);
			}
		} catch(final Exception e) {
			final IOException exc = new IOException("Unable to read state from " + is.getSystemId() + ": " + e.getMessage());
			exc.initCause(e);
			throw exc;
		}
	}
	
	/**
			             *
			             */
	public XMLStateStorage() {
		super();
	}
	
	/**
	 * Creates a new XMLStateStorage object.
	 * 
	 * @param ss DOCUMENT ME!
	 */
	public XMLStateStorage(final StateStorage ss) {
		super(ss);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param is DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	@Override
	public void load(final InputStream is) throws IOException {
		load(new InputSource(is));
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param is DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void load(final InputSource is) throws IOException {
		loadStates(is, states);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#load(java.io.InputStream)
	 */
	@Override
	public void load(final String filePath, final String applicationName) throws IOException {
		if (delegate != null) {
			delegate.load(filePath, applicationName);
			addAll(delegate.getStates());
			
			return;
		}
		
		// get the InputStream if file exists
		final InputStream is = getInputStream(filePath, applicationName + ".xml");
		
		if (is != null) {
			try {
				load(is);
			} catch(final IOException e) {
				final IOException ex = new IOException("Failed to load from file '" + filePath + "/" + applicationName + ".xml'");
				ex.initCause(e);
				throw ex;
			}
		}
	}
	
	private static final void saveState(final Document doc, final Element el, final State s) {
		// el.setAttribute("ID", (s.getID()==null)?"null":s.getID());
		final Iterator<String> iter = s.keySet().iterator();
		
		while (iter.hasNext()) {
			final String key = iter.next();
			final Object value = s.getObject(key);
			
			if (value instanceof State) {
				final Element el1 = doc.createElement(key);
				el.appendChild(el1);
				saveState(doc, el1, (State)value);
				
				continue;
			}
			
			final String aa = String.valueOf(value);
			// System.out.println("KEY "+key+ " VALUE "+value);
			el.setAttribute(key, aa);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.OutputStream)
	 */
	@Override
	public void store(final String filePath, final String applicationName) throws IOException {
		final OutputStream os = getOutputStream(filePath, applicationName + ".xml");
		store(os);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.FileInputStream, java.lang.String)
	 */
	@Override
	public void store(final OutputStream os) throws IOException {
		storeStates(states, os);
	}
}

/* __oOo__ */
