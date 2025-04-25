package com.sinergise.generics.impl;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.i18n.I18nProvider;


public class XmlUtils {
	
	private static final DocumentBuilderFactory docBuilderFactory;
	static {
		docBuilderFactory = DocumentBuilderFactory.newInstance();
	}
	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return docBuilderFactory.newDocumentBuilder();
	}
	
	public static Element findElementByNameAttribute(Element rootElement, String elementName, String nameAttribute ) {
		
		NodeList nodes = rootElement.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elm = (Element)nodes.item(i);
				String name = elm.getAttribute(MetaAttributes.NAME);
				if (nameAttribute.equals(name))
					return elm;
			}
		}
		return null;
	}
	
	private static void combineElement(Element master, Element toCombine) {
		NamedNodeMap attributesToCombine = toCombine.getAttributes();

		for ( int loop = 0, length = attributesToCombine.getLength(); loop < length; loop++ ) {
			Node nodeToAdd = attributesToCombine.item( loop );

			String attributeToAddName = nodeToAdd.getNodeName();
			String attributeToAddValue = nodeToAdd.getNodeValue();

			if ( attributeToAddValue == null || attributeToAddValue.length() == 0 )
				master.removeAttribute( attributeToAddName );
			master.setAttribute( attributeToAddName, attributeToAddValue );
		}
		
	}
	
	public static void combineElements( Element master, Element toAdd, String topLevelAttributeToCombineOn, String childAttributeToCombineOn)
	{		
		combineElement(master, toAdd);

		
		NodeList masterChildren = master.getChildNodes();
		NodeList defaultNodes = toAdd.getElementsByTagName(XMLTags.EntityAttributeDefaults);
		if (defaultNodes!=null && defaultNodes.getLength()>0) {
			Node dNode = defaultNodes.item(0);
			if (dNode instanceof Element) {
				Element defaults= (Element)dNode;
				for (int i=0;i<masterChildren.getLength();i++) {
					Node masterNode = masterChildren.item( i );
					if (!(masterNode instanceof Element))
						continue;
					if (!XMLTags.EntityAttribute.equals(masterNode.getNodeName()))
						continue;
					Element  masterNodeElement = (Element)masterNode;
					combineElement(masterNodeElement,defaults);
				}
			}
		}
		
		// Combine child elements: for each child...

		NodeList childrenToAdd = toAdd.getChildNodes();

		ArrayList<String> childNamesAdded = new ArrayList<String>();
		
		Node nodeLastMasterCombinePoint = null;

		outerLoop: for ( int addLoop = 0, addLength = childrenToAdd.getLength(); addLoop < addLength; addLoop++ )
		{
			Node nodeChildToAdd = childrenToAdd.item( addLoop );

			if ( !( nodeChildToAdd instanceof Element ) )
				continue;
			
			if (XMLTags.EntityAttributeDefaults.equals(nodeChildToAdd.getNodeName()))
				continue;
			
			Element childToAdd = (Element) nodeChildToAdd;			
			String childToAddName = childToAdd.getAttribute( topLevelAttributeToCombineOn );

			if ( childToAddName == null || "".equals( childToAddName ) )
				throw new RuntimeException( "Child node #" + addLoop + " (" + childToAdd.getNodeName() + ") has no @" + topLevelAttributeToCombineOn );

			if ( !childNamesAdded.add( childToAddName ) )
				throw new RuntimeException( "Element has more than one child with @" + topLevelAttributeToCombineOn + " '" + childToAddName + "'" );

			// ...find one with the same @name in the 'master'...

			for ( int masterLoop = 0, masterLength = masterChildren.getLength(); masterLoop < masterLength; masterLoop++ )
			{
				Node nodeMasterChild = masterChildren.item( masterLoop );

				if ( !( nodeMasterChild instanceof Element ) )
					continue;

				Element masterChild = (Element) nodeMasterChild;
				String masterChildName = masterChild.getAttribute( topLevelAttributeToCombineOn );

				if ( !childToAddName.equals( masterChildName ) )
					continue;

				String nodeNameInMaster = masterChild.getNodeName();
				String nodeNameInAdd = childToAdd.getNodeName();

				if ( !nodeNameInMaster.equals( nodeNameInAdd ) )
					throw new RuntimeException( "Matching elements named '" + masterChildName + "', but existing one is a '" + nodeNameInMaster + "' whilst new one is a '" + nodeNameInAdd + "'" );

				// ...and combine them

				if ( masterLoop == masterLength - 1 )
					nodeLastMasterCombinePoint = null;
				else
					nodeLastMasterCombinePoint = masterChild;

				combineElements( masterChild, childToAdd, childAttributeToCombineOn, childAttributeToCombineOn);
				continue outerLoop;
			}

			// If no such child exists, add one either immediately after the
			// last matched master...

			if ( nodeLastMasterCombinePoint != null )
			{
				Element imported =(Element) master.getOwnerDocument().importNode(childToAdd, true);
				master.insertBefore( imported, nodeLastMasterCombinePoint.getNextSibling() );
				nodeLastMasterCombinePoint = imported;
				continue;
			}

			// ...or simply at the end

			master.appendChild( master.getOwnerDocument().importNode(childToAdd, true));
			
		}
	}

	public static String nodeToString( Node node)  {
		return nodeToString(node, null);
	}
	
	public static String nodeToString( Node node, I18nProvider intProvider )
	{
		if ( !( node instanceof Element ))
			return "";

		StringBuilder builder = new StringBuilder();

		// Open tag

		String nodeName = node.getNodeName();
		builder.append( "<" );
		builder.append( nodeName );

		// Attributes (if any)

		NamedNodeMap attributes = node.getAttributes();
		
		for ( int loop = 0, length = attributes.getLength(); loop < length; loop++ )
		{
			Node attribute = attributes.item( loop );
			builder.append( " " );
			builder.append( attribute.getNodeName() );
			builder.append( "=\"" );
			String value = attribute.getNodeValue();
			if (intProvider!=null)
				value = intProvider.getAttributeTranslation(attribute.getNodeName(), value);
			builder.append( value );
			builder.append( "\"" );
		}

		builder.append( ">" );

		// Children (if any)

		NodeList children = node.getChildNodes();

		for ( int loop = 0, length = children.getLength(); loop < length; loop++ )
		{
			builder.append( nodeToString( children.item( loop ), intProvider ) );
		}

		// Close tag

		builder.append( "</" );
		builder.append( nodeName );
		builder.append( ">" );

		return builder.toString();
	}

	/**
	 * Ensures MetaAttributes.LABEL on all EntityAttributes that are first child of Entity 
	 *  
	 * @param node  XMLTabs.Entity element
	 * @return 
	 */
	public static void fixLabels (Element node) {
		if (XMLTags.Entity.equals(node.getNodeName())) {
			NodeList children = node.getChildNodes();

			for ( int loop = 0, length = children.getLength(); loop < length; loop++ ) {
				Node child = children.item(loop);
				if (!XMLTags.isSupportTag(child.getNodeName()) ) {
					Element el = (Element)child;
					if (!el.hasAttribute(MetaAttributes.LABEL)) {
						String label = el.getAttribute(MetaAttributes.NAME);
						if (label!=null)
							el.setAttribute(MetaAttributes.LABEL, label);
					}
				}
			}
		}
	}
}
