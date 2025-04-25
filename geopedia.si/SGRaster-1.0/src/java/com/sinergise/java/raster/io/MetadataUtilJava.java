package com.sinergise.java.raster.io;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class MetadataUtilJava {
	protected static void printMetadataNode(Node meta) {
		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.transform(new DOMSource(meta), new StreamResult(System.out));
			System.out.println();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
