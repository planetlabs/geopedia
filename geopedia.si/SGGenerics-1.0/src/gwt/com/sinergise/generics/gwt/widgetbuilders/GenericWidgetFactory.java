package com.sinergise.generics.gwt.widgetbuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.util.XMLUtils;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.WidgetInspector;
import com.sinergise.generics.gwt.core.WidgetInspectorListener;

public class GenericWidgetFactory {
	
	static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(GenericWidgetFactory.class); 
	
	@Deprecated 
	public static GenericWidget buildWidget (final EntityType entityType, final GenericWidget gt, 
			WidgetInspector inspector, final Map<String,String> widgetProperties ) {

		if (entityType == null) throw new RuntimeException("Provide non-null EntityType!");
		return buildWidget(gt, inspector, widgetProperties);
	}
	
	
	
	public static GenericWidget buildWidget (GenericWidget gt, WidgetInspector inspector) {
		return buildWidget(gt, inspector, new HashMap<String,String>());
	}
	
	
	public static GenericWidget buildWidget (final GenericWidget gt, 
			WidgetInspector inspector, final Map<String,String> widgetProperties ) {
		return buildWidget(gt, inspector, widgetProperties, new HashMap<String, Map<String,String>>());
		
	}
	
	
	private static void processElement(Element xmlElement, HashMap<String, HashMap<String,String> > propertyMap, Map<String,Map<String,String>> widgetProperties ) {
		NodeList nodes = xmlElement.getChildNodes();		
		for (int i=0;i<nodes.getLength();i++) {
			Node nd = nodes.item(i);
			if (nd.getNodeType() != Node.ELEMENT_NODE) continue;
			Element taElement = (Element)nd;
			
			
			if (!XMLTags.isSupportTag(taElement.getNodeName())) {				
				String taName = taElement.getAttribute(MetaAttributes.NAME);
				if (taName==null)
					throw new RuntimeException(taElement.getNodeName()+" is missing "+MetaAttributes.NAME+ " attribute!");
				HashMap<String,String> attributesMap = propertyMap.get(taName);
				if (attributesMap == null) {
					attributesMap = new HashMap<String, String>();
					XMLUtils.elementToAttributeMap(taElement, attributesMap);
					if (widgetProperties != null) {
						XMLUtils.joinAttributeMaps(attributesMap,
								widgetProperties.get(taName));
					}
					attributesMap.put(MetaAttributes.ELEMENT_TYPE, taElement.getNodeName());
					propertyMap.put(taName,attributesMap);					
				} else {
					Map<String,String> additionalAttributes = propertyMap.get(taName);
					XMLUtils.elementToAttributeMap(taElement, additionalAttributes);
					XMLUtils.joinAttributeMaps(attributesMap,
							additionalAttributes);
				}
				
			} else {
				processElement(taElement,propertyMap,widgetProperties);
			}
		}
	}
	public static ArrayList<GenericObjectProperty> generateForEntity(EntityType et, Element entityXml,Map<String,Map<String,String>> entityAttributeProperties,
			Map<String,String> widgetProperties) {
		if (entityXml==null || et == null)
			return null;
		
		 HashMap<String, HashMap<String,String> > propertyMap = new HashMap<String,HashMap<String,String>>();
		 processElement(entityXml,propertyMap, entityAttributeProperties );
		 Map<Integer,GenericObjectProperty> toSort = new HashMap<Integer,GenericObjectProperty>();
		 int i=0;
		 int unassignedPositionBegin = Integer.MAX_VALUE - propertyMap.size()-1;
		 
		 // extra ignore list, that can be changed when widget is created
		 String extraAttrIgnoreListStr  = widgetProperties.get(MetaAttributes.WIDGET_ATTRIBUTE_IGNORE_LIST );
		 ArrayList<String> extraAttrIgnoreList = new ArrayList<String>();
		 if (extraAttrIgnoreListStr!=null) {
			 String[] extIL = extraAttrIgnoreListStr.split(",");
			 for (String atName:extIL)
			 extraAttrIgnoreList.add(atName);
		 }
		 for (String attName:propertyMap.keySet()) {
			 HashMap<String,String> propAttributes =propertyMap.get(attName); 
		
			 if (extraAttrIgnoreList.contains(attName)) // ignore the attribute if it's in the extra ignore list
				 continue;
			 if (MetaAttributes.isTrue(propAttributes.get(MetaAttributes.IGNORE)))
				 continue;
			 if (MetaAttributes.isFalse(propAttributes.get(MetaAttributes.EXPORT_TO_FILE)) &&
				MetaAttributes.isTrue(propAttributes.get(MetaAttributes.HIDDEN))) 
					continue;
			 
			 
			 if (XMLTags.EntityAttribute.equalsIgnoreCase(propAttributes.get(MetaAttributes.ELEMENT_TYPE))) {
				 if (MetaAttributes.readRequiredStringAttribute(propAttributes, MetaAttributes.TYPE).equals(Types.STUB)) // SKIP STUB checking in entity datamodel
						break;
				 TypeAttribute ta = et.getAttribute(attName);
				 if (ta==null) 
					 throw new IllegalStateException("Attribute '"+attName+"' does not exist for the supplied EntityType..");
			 }

			 GenericObjectProperty prp = new GenericObjectProperty(propAttributes);
			 int positionIdx = unassignedPositionBegin+i;
			 if (propAttributes.containsKey(MetaAttributes.POSITION)) {
				positionIdx = Integer.parseInt(propAttributes.get(MetaAttributes.POSITION));
			 }
			 toSort.put(positionIdx, prp);
			 i++; 
		 }
		// sort columns
		Object[] keys = toSort.keySet().toArray();
		Arrays.sort(keys);
		ArrayList<GenericObjectProperty> propsSorted = new ArrayList<GenericObjectProperty>();
		for (Object key : keys) {
			propsSorted.add(toSort.get(key));
		}
		return propsSorted;
	}
	
	public static GenericWidget buildWidget (final GenericWidget gt, 
			WidgetInspector inspector, final Map<String,String> widgetProperties, final Map<String, Map<String,String>> entityAttributeProperties) {

		if (inspector!=null) {
			inspector.setWidgetInspectorListener(new WidgetInspectorListener() {
				@Override
				public void inspectionCompleted(Element entityXml) {
					
					EntityType entityType = GwtEntityTypesStorage.getInstance().getEntityType((entityXml.getAttribute(MetaAttributes.TYPE)));
					/*Element entityXml  = XMLUtils.findEntityByType(metadata, entityType.getName());
					if (entityXml == null)  {
						Log.error("Inspection failed to return any definition for EntityType '"+entityType.getName()+"'");
						throw new RuntimeException("Unable to find entity definition for entity '"+entityType.getName()+"'");
					}*/
					logger.trace("Inspection completed!");
					NamedNodeMap nnm = entityXml.getAttributes();
					for (int i=0;i<nnm.getLength();i++){
						Node attrNode = nnm.item(i);
						logger.trace("Processing node: '"+attrNode.getNodeName()+"'");
						if (!widgetProperties.containsKey(attrNode.getNodeName()))
							widgetProperties.put(attrNode.getNodeName(),attrNode.getNodeValue());
					}
					ArrayList<GenericObjectProperty> sortedProperties = generateForEntity
							(entityType, entityXml, entityAttributeProperties, widgetProperties);
					
					if (sortedProperties==null || sortedProperties.size()==0) {
						logger.error("No properties to build the widget from.. (missing entry in XMLEntities?)");
					}
					
					gt.setWidgetMetaAttributes(widgetProperties);

					Element masonElement = XMLUtils.findElementByName(entityXml, XMLTags.WidgetMason);
					if (masonElement==null) {
						gt.build(sortedProperties, entityType);
					}
					else {
						gt.build(sortedProperties, entityType, masonElement);
					}
				}

				@Override
				public void inspectionCompleted(EntityType entityType,
						ArrayList<GenericObjectProperty> sortedProperties,
						Map<String, String> widgetMetadata) {
					
					for (String key:widgetProperties.keySet()) {
						widgetMetadata.put(key, widgetProperties.get(key));
					}
					gt.setWidgetMetaAttributes(widgetMetadata);
					gt.build(sortedProperties, entityType);
					
				}
			});
			inspector.inspect();
		} else {
			// TODO: generate entityXML from entity..
		}

		return gt;
		
	}
}
