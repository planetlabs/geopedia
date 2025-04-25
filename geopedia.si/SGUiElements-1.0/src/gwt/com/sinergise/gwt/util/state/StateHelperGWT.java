package com.sinergise.gwt.util.state;


import java.util.ArrayList;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;

public class StateHelperGWT {

	public static StateGWT[] readState(final String content) {
		if (StringUtil.isNullOrEmpty(content))
			return new StateGWT[0];
		Document stateDom = XMLParser.parse(content);


		NodeList statesList = stateDom.getElementsByTagName("states");
		if (statesList.getLength() == 0)
			return new StateGWT[0];

		NodeList stateNodes = ((Element)statesList.item(0)).getElementsByTagName("state");
		StateGWT[] states = new StateGWT[stateNodes.getLength()];
		for (int i = 0; i < states.length; i++) {
			states[i] = new StateGWT();
			readState(stateNodes.item(i), states[i]);
		}

		return states;
	}


	private static boolean isNodeArray(final ArrayList<Element> chElements) {
		if (chElements.size() < 2) {
			return false;
		}
		Element e1 = chElements.get(0);
		Element e2 = chElements.get(1);
		if (e1.getTagName().equals(e2.getTagName())) {
			return true;
		}
		return false;
	}

	public static void readState(final Node node, final StateGWT state) {
		final NamedNodeMap nnm = node.getAttributes();
		if (nnm != null) {
			for (int i = 0; i < nnm.getLength(); i++) {
				String key = nnm.item(i).getNodeName();
				String value = nnm.item(i).getNodeValue();
				state.putString(key, value);
			}
		}
		NodeList nodes = node.getChildNodes();
		ArrayList<Element> chElements = new ArrayList<Element>();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				chElements.add((Element)nodes.item(i));
			}
		}

		final boolean nodeArray = isNodeArray(chElements);
		for (int i = 0; i < chElements.size(); i++) {
			String nodeName = (chElements.get(i)).getTagName();
			if (nodeArray) {
				nodeName = nodeName + i;
			}
			final StateGWT s = state.createState(nodeName);
			readState(chElements.get(i), s);
		}

	}
}
