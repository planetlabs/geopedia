package com.sinergise.generics.datasource.xml;

import static com.sinergise.generics.core.EntityObject.Status.STORED;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sinergise.generics.builder.filter.JavaFilterProcessor;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.datasource.EntityDatasource;
import com.sinergise.generics.impl.XmlUtils;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.generics.server.GenericsServerSession;

public class XMLDataSource extends EntityDatasource{
	
	public static final String ATTR_XML_FILE="XMLFile";
	
	public static final String ATTR_DATASOURCE="Datasource";
	public static final String ATTR_DATASOURCES="Datasources";
	public static final String ITEM="Item";
	public static final String ITEM_IDX="idx";
	
	private String XMLFile = null;
	private String datasourceName = null;
	public XMLDataSource(Element dsElement, GenericsSettings settings, String basePath) {
		super(TYPE_XML, dsElement, settings);
		XMLFile = dsElement.getAttribute(ATTR_XML_FILE);
		datasourceName = dsElement.getAttribute(MetaAttributes.NAME);
		
		if (XMLFile==null)
			throw new IllegalArgumentException("Attribute '" + ATTR_XML_FILE + "' is missing!");
		if (datasourceName==null)
			throw new IllegalArgumentException("Attribute '" + MetaAttributes.NAME + "' is missing!");
		if (basePath.endsWith(File.separator)) {
			XMLFile=basePath+XMLFile;
		} else {
			XMLFile=basePath+File.separator+XMLFile;
		}
	}
	
	private EntityObject createEntityObjectFromElement(Element element) {
		AbstractEntityObject eo = new AbstractEntityObject(entityType.getId());
    	eo.setStatus(STORED);
    	NodeList nodes = element.getChildNodes();
    	for (int i=0;i<nodes.getLength();i++) {
			if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elm = (Element)nodes.item(i);
				TypeAttribute ta = entityType.getAttribute(elm.getNodeName());
				if (ta==null) {
					throw new IllegalArgumentException("Entity type '"+entityType.getName()+"' doesn't contain "
					   + " TypeAttribute '"+elm.getNodeName()+"'");
				}
				
				if (elm.getFirstChild() != null && 
						(elm.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE || 
								elm.getFirstChild().getNodeType() == Node.TEXT_NODE )) {
					Text textNode = (Text)elm.getFirstChild();
					String nodeData = textNode.getData();
					if (nodeData!=null)
						nodeData = nodeData.trim();
					eo.setPrimitiveValue(ta.getId(), nodeData);
				}
				
			}
		}
    	return eo;
	}

	@Override
	public ArrayValueHolder getData(DataFilter filter, int fromIdx, int toIdx, GenericsServerSession gSession)
			throws Exception {
		DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		Document xmlDocument = dbuilder.parse(new File(XMLFile).toURI().toString());
		
		NodeList xmlEntitiesList = xmlDocument.getElementsByTagName(ATTR_DATASOURCES);
		if (xmlEntitiesList.getLength()!=1) 
			throw new RuntimeException("'"+XMLFile+"' doesn't contain exactly one '"+ATTR_DATASOURCES+"' element.");
		
		Element rootElement = (Element) xmlEntitiesList.item(0);
		Element dsElement = XmlUtils.findElementByNameAttribute(rootElement, ATTR_DATASOURCE, datasourceName);		
		if (dsElement ==null)
			throw new RuntimeException("Unable to find '"+ATTR_DATASOURCE+"' element for '"+datasourceName+"'");
		
        ArrayValueHolder avh = new ArrayValueHolder(entityType.getId());

		NodeList nodes = dsElement.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elm = (Element)nodes.item(i);
				if (elm.getNodeName().equals(ITEM)) {
					EntityObject eo = createEntityObjectFromElement(elm);
					avh.add(eo);
				}
			}
		}
	
		if (filter!=null && filter instanceof SimpleFilter) {
			SimpleFilter sf = (SimpleFilter)filter;
			JavaFilterProcessor jfp = new JavaFilterProcessor(genericsSettings, entityType.getName());
			return jfp.applyFilter(sf, avh);
		}
		return avh;
	}

	@Override
	public ValueHolder processData(ValueHolder values, GenericsServerSession gSession) throws Exception {
		throw new UnsupportedOperationException("XML Datasource doesn't support data process");
	}

}
