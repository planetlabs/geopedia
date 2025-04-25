/*
 * Copyright (c) 2003 by Cosylab d.o.o.
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

package com.sinergise.java.util.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <code>StateHelper</code> ... DOCUMENT ME!
 * 
 * @author dvitas
 * @version $Id: StateHelper.java 17890 2006-08-25 08:34:01Z dvitas $
 */
public class StateHelper {
	private static DocumentBuilderFactory documentBuilderFactory = null;
	
	public static DocumentBuilder getDOMBuilder() {
		if (documentBuilderFactory == null) {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);
			documentBuilderFactory.setNamespaceAware(false);
		}
		try {
			return documentBuilderFactory.newDocumentBuilder();
		} catch(final ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void nodeToState(final Node node, final State state) {
		final NamedNodeMap nnm = node.getAttributes();
		String key;
		String value;
		
		for (int i = 0; i < nnm.getLength(); i++) {
			key = nnm.item(i).getNodeName();
			value = nnm.item(i).getNodeValue();
			state.putString(key, value);
		}
		
		// and childs if any
		final NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			final Node child = nl.item(i);
			if (child instanceof Element) {
				final State s = state.createState(child.getNodeName());
				nodeToState(child, s);
			}
		}
	}
	
	private static Document getDocumnet(final InputStream inputStream) {
		try {
			return getDOMBuilder().parse(inputStream);
		} catch(final Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static Document getDocumnet(final InputSource inputSource) {
		try {
			return getDOMBuilder().parse(inputSource);
		} catch(final Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static Node getStartingNode(final Document doc) {
		final NodeList nl = doc.getChildNodes();
		Node node = null;
		
		for (int i = 0; i < nl.getLength(); i++) {
			node = nl.item(i);
			if (node instanceof Element) {
				break;
			}
		}
		
		return node;
	}
	
	private static String processValueString(final String inString) {
		if (inString == null) {
			return "null";
		}
		
		final int len = inString.length();
		final StringBuffer retBuf = new StringBuffer();
		
		for (int i = 0; i < len; i++) {
			final char c = inString.charAt(i);
			
			if (c > 127 || c == '&' || c == '"' || c == '<' || c == '>') {
				retBuf.append("&#" + ((int)c) + ";");
			} else {
				retBuf.append(c);
			}
		}
		
		return retBuf.toString();
	}
	
	private static String processKey(final String key) {
		if (key.startsWith("Layers")) return key;
		if (key.startsWith("Layer")) return "Layer";
		return key;
	}
	
	public static void writeState(final Writer writer, final String name, final State state) throws IOException {
		boolean hasChilds = false;
		writer.write("<" + name);
		
		Iterator<String> iter = state.keySet().iterator();
		
		while (iter.hasNext()) {
			final String key = iter.next();
			final Object value = state.getObject(key);
			
			if (value instanceof State) {
				hasChilds = true;
				continue;
			}
			
			final String aa = processValueString(value.toString());
			writer.write(" " + key + "=\"" + aa + "\" ");
		}
		
		if (hasChilds) {
			writer.write(">\n");
		} else {
			writer.write("/>\n");
		}
		
		iter = state.keySet().iterator();
		
		while (iter.hasNext()) {
			String key = iter.next();
			final Object value = state.getObject(key);
			
			if (value instanceof State) {
				key = processKey(key);
				writeState(writer, key, (State)value);
			}
		}
		
		if (hasChilds) {
			writer.write("</" + name + ">\n");
		}
	}
	
	private static boolean isNodeArray(final Node[] nodes) {
		if (nodes.length < 2) {
			return false;
		}
		if (nodes[0].getNodeName().equals(nodes[1].getNodeName())) {
			return true;
		}
		if (nodes[0].getNodeName().indexOf("Layer") != -1 && nodes[1].getNodeName().indexOf("Layer") != -1) {
			return true;
		}
		return false;
	}
	
	public static Node[] getChildNodes(final Node node) {
		final NodeList nl = node.getChildNodes();
		final int numNodes = nl.getLength();
		if (numNodes == 0) {
			return new Element[0];
		}
		
		final ArrayList<Node> elements = new ArrayList<Node>();
		for (int i = 0; i < numNodes; i++) {
			final Node child = nl.item(i);
			if (child instanceof Element) {
				elements.add(child);
			}
		}
		return elements.toArray(new Node[elements.size()]);
	}
	
	public static State readState(final String content) throws IOException, SAXException {
		return StateHelper.readState(new InputSource(new StringReader(content)));
	}
	
	public static State readState(final InputSource is) throws IOException, SAXException {
		final Document doc = StateHelper.getDOMBuilder().parse(is);
		Node statesNode = doc.getDocumentElement();
		// backward compability check
		if ("states".equalsIgnoreCase(statesNode.getNodeName())) {
			statesNode = StateHelper.getChildNodes(statesNode)[0];
		}
		final State state = StateFactory.createState();
		StateHelper.readState(statesNode, state);
		return state;
	}
	
	public static State readState(final InputStream is) throws IOException, SAXException {
		final Document doc = StateHelper.getDOMBuilder().parse(is);
		Node statesNode = doc.getDocumentElement();
		// backward compability check
		if ("states".equalsIgnoreCase(statesNode.getNodeName())) {
			statesNode = StateHelper.getChildNodes(statesNode)[0];
		}
		final State state = StateFactory.createState();
		StateHelper.readState(statesNode, state);
		return state;
	}
	
	public static void readState(final Node node, final State state) {
		final NamedNodeMap nnm = node.getAttributes();
		
		// load attributes
		String key;
		
		// load attributes
		String value;
		
		for (int i = 0; i < nnm.getLength(); i++) {
			key = nnm.item(i).getNodeName();
			value = nnm.item(i).getNodeValue();
			state.putString(key, value);
		}
		
		// and childs if any
		final Node[] nodes = getChildNodes(node);
		final boolean nodeArray = isNodeArray(nodes);
		for (int i = 0; i < nodes.length; i++) {
			String nodeName = nodes[i].getNodeName();
			if (nodeArray) {
				nodeName = nodeName + i; // internaly store with unique keys
			}
			final State s = state.createState(nodeName);
			readState(nodes[i], s);
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param stateString DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static State fromXML(String stateString) {
		if (!stateString.startsWith("<?xml")) {
			stateString = "<?xml version=\"1.0\" standalone=\"yes\"?>\n" + stateString;
		}
		
		final State state = StateFactory.createState();
		nodeToState(getStartingNode(getDocumnet(new InputSource(new StringReader(stateString)))), state);
		
		return state;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param is DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static State fromXML(final InputStream is) {
		final State state = StateFactory.createState();
		nodeToState(getStartingNode(getDocumnet(is)), state);
		
		return state;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param elementName DOCUMENT ME!
	 * @param state DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String toXMLpart(final String elementName, final State state) {
		try {
			final StringWriter sw = new StringWriter();
			writeState(sw, elementName, state);
			
			return sw.toString();
		} catch(final IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String toXML(final State state) {
		return toXML(state, null);
		
	}
	
	public static String toXML(final State state, String rootElementName) {
		try {
			final StringWriter sw = new StringWriter();
			sw.write("<?xml version=\"1.0\" standalone=\"yes\"?>\n");
			if (rootElementName == null) {
				rootElementName = "Giselle";
			}
			writeState(sw, rootElementName, state);
			return sw.toString();
		} catch(final IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(final String[] args) throws IOException {
		final File f = new File("./LandUseState.xml");
		System.out.println("Trying state " + f.getAbsolutePath());
		FileInputStream is = new FileInputStream(f);
		try {
			final State state = fromXML(is);
			System.out.println("State xml");
			state.writeXML(new OutputStreamWriter(System.out));
			System.err.println("\n\nState tag");
			System.err.println(toXMLpart("MyName", state));
		} catch(final Exception e) {
			e.printStackTrace();
			is.close();
		}
	}
}

/* __oOo__ */
