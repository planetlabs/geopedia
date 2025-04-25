package com.sinergise.generics.gwt.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.util.XMLUtils;
import com.sinergise.generics.gwt.widgetbuilders.FormWidgetBuilder;
import com.sinergise.generics.gwt.widgets.components.AbstractWizard;
import com.sinergise.generics.gwt.widgets.components.WizardPanel;
import com.sinergise.gwt.ui.UniversalPanel;



public class MasonWidget extends AbstractMasonWidget {

	
	
	private UniversalPanel 	basePanel;
	private SimplePanel		statusPanel;
	
	private AbstractWizard wizard = null;
	
	
	public AbstractWizard getWizard() {
		return wizard;
	}
	
	public MasonWidget() {
		basePanel = new UniversalPanel();
		basePanel.addStyleName(STYLE_MASON_CORE);
		widgetBuilder = new FormWidgetBuilder();
		initWidget(basePanel);
	}
	
	
	
	
	@Override
	protected ArrayList<String> buildWidget(NodeList nodes, InsertPanel widgetContainer) {
		ArrayList<String> widgetAttributes = new ArrayList<String>();
		for (int i=0;i<nodes.getLength();i++) {
			Element wgElement=(Element)nodes.item(i);
			
			
			
			Map<String,String> wgAttributeMap = new HashMap<String, String>();
			XMLUtils.elementToAttributeMap(wgElement, wgAttributeMap);
			
			
			if (WIZARD.equals(wgElement.getNodeName())) {				
				String type = MetaAttributes.readRequiredStringAttribute(wgAttributeMap, MetaAttributes.TYPE);
				AbstractWizard wiz = WizardFactory.getInstance().createWizard(type);
				
				NodeList wizNodes = wgElement.getChildNodes();
				for (int j=0;j<wizNodes.getLength();j++) {
					Element wizElement=(Element)wizNodes.item(j);
					if (WIZARD_SECTION.equals(wizElement.getNodeName())) {						
						Map<String,String> sectionAttributeMap = new HashMap<String, String>();
						XMLUtils.elementToAttributeMap(wizElement, sectionAttributeMap);		
						
						WizardPanel panel = buildWizardSectionWidget(sectionAttributeMap, wizElement);
						wiz.addWizardPanel(panel);
						panel.addStyleName(STYLE_MASON_SECTION);
					} else {
						throw new IllegalArgumentException("Illegal node name '"+wizElement.getNodeName()+"' inside '"+WIZARD+"' section!");
					}
				}
				wizard = wiz;
				wizard.create();
				widgetContainer.add(wiz);

			} else {
				UniversalPanel sectionWidget = new UniversalPanel();
				if (MetaAttributes.WIDGET_FLEXTABLE.equals(wgElement.getNodeName())) {
					widgetAttributes.addAll(buildFlexTableWidget(sectionWidget, wgAttributeMap, wgElement));
				} else if (MetaAttributes.WIDGET_DIV.equals(wgElement.getNodeName())) {
					widgetAttributes.addAll(buildDivWidget(sectionWidget, wgAttributeMap, wgElement));
				}
				
				if (sectionWidget!=null) {
					applyStyle(sectionWidget, wgAttributeMap);
					String titleLabel = MetaAttributes.readStringAttr(wgAttributeMap, MetaAttributes.LABEL, null);
					if (titleLabel!=null && titleLabel.length()>0) {
						sectionWidget.setTitle(titleLabel);
					} 
	
					sectionWidget.addStyleName(STYLE_MASON_SECTION);
				}
				widgetContainer.add(sectionWidget);
			}					

		}		
		return widgetAttributes;
	}


	@Override
	protected void buildStatusWidget(Map<String,String> masonAttributes, InsertPanel widgetContainer) {
		
		boolean shouldAddStatusPanel = false;		
		shouldAddStatusPanel	= MetaAttributes.readBoolAttr(masonAttributes, META_ADDSTATUSPANEL, shouldAddStatusPanel);
		
		if(shouldAddStatusPanel){		
			
			statusPanel = new SimplePanel();
			statusPanel.addStyleName(STYLE_MASON_STATUS);
			
			widgetContainer.add(statusPanel);			
		}
		
	}
	
	

	@Override
	protected Widget getBasePanel() {
		return basePanel;
	}

	public SimplePanel getStatusPanel() {
		return statusPanel;
	}


	
}

/**
extends GenericWidget{

	
		private static final String STYLE_MASON_SECTION ="masonSection";
		private static final String STYLE_MASON_CORE ="masonWidget";
		private static final String WIDGET_FLEXTABLE="FlexTable";
		private static final String WIDGET_DIV="Div";
		
		
		private Map<String,GenericObjectProperty> propertyMap = new HashMap<String, GenericObjectProperty>();
		private Map<String,Widget> widgetMap = new HashMap<String, Widget>();
	
		private UniversalPanel basePanel;
		
		
		
		// Only for Freemasons! ;)
		public MasonWidget() {
			basePanel = new UniversalPanel();
			basePanel.addStyleName(STYLE_MASON_CORE);
			initWidget(basePanel);
		}
		
		public Map<String,GenericObjectProperty> getGenericObjectPropertyMap() {
			return propertyMap;
		}
		
		public Map<String,Widget> getWidgetMap() {
			return widgetMap;
		}
		
		@Override
		protected void bindProcessors() {
			for (String atName:propertyMap.keySet()) {
				GenericObjectProperty property = propertyMap.get(atName);
				Widget w = widgetMap.get(atName);
				if (w!=null)
					bindProcessors(w, 0, property, this);
			}
			
		}
		
		
	
		@Override
		public void build(List<GenericObjectProperty> properties,
				Map<String, String> tableMetaAttr, EntityType entityType) {
				throw new IllegalStateException("Mason widget requires mason attributes!");

		}
		
		public void build(List<GenericObjectProperty> properties, Map<String, String> tableMetaAttr, EntityType entityType, Element masonElement) {
			setEntityType(entityType);
			Map<String,String> masonAttributes = new HashMap<String, String>();
			XMLUtils.elementToAttributeMap(masonElement, masonAttributes);
			String widgetStyle = MetaAttributes.readStringAttr(masonAttributes, MetaAttributes.NAME, null);
			if (widgetStyle!=null) {
				basePanel.addStyleName(widgetStyle);
			}
			
			NodeList childNodes = masonElement.getChildNodes();
			
			for (GenericObjectProperty p:properties){  // convert list to map 
				propertyMap.put(p.getName(),p);
			}
			
			
			for (int i=0;i<childNodes.getLength();i++) {
				Element wgElement=(Element)childNodes.item(i);
				
				
				
				UniversalPanel sectionWidget = new UniversalPanel();
				Map<String,String> wgAttributeMap = new HashMap<String, String>();
				XMLUtils.elementToAttributeMap(wgElement, wgAttributeMap);

				if (WIDGET_FLEXTABLE.equals(wgElement.getNodeName())) {
					buildFlexTableWidget(sectionWidget, wgAttributeMap, wgElement);
				} else if (WIDGET_DIV.equals(wgElement.getNodeName())) {
					buildDivWidget(sectionWidget, wgAttributeMap, wgElement);
				}
				
				if (sectionWidget!=null) {
					String styleName = MetaAttributes.readStringAttr(wgAttributeMap, MetaAttributes.NAME, null);
					if (styleName!=null) {
						sectionWidget.setStyleName(styleName);
					}
					String titleLabel = MetaAttributes.readStringAttr(wgAttributeMap, MetaAttributes.LABEL, null);
					if (titleLabel!=null && titleLabel.length()>0) {
						sectionWidget.setTitle(titleLabel);
					}

					sectionWidget.addStyleName(STYLE_MASON_SECTION);
					basePanel.add(sectionWidget);
				}					

			}
			widgetCreated();
		}
		
		private void buildDivWidget(UniversalPanel holder, Map<String,String> divAttributes, Element wgElement) {
			NodeList childNodes = wgElement.getChildNodes();
			
			for (int i=0;i<childNodes.getLength();i++){
				Element el=(Element)childNodes.item(i);
				String name = el.getAttribute(MetaAttributes.NAME);
				if (name == null)
					throw new IllegalArgumentException("Unable to find "+MetaAttributes.NAME+" attribute");
				GenericObjectProperty property = propertyMap.get(name);
				if (property==null)
					throw new RuntimeException("Unable to find '"+XMLTags.EntityAttribute+"' with name '"+name+"'");
				
				Map<String,String> elementAttributes = new HashMap<String, String>(property.getAttributes());				
				XMLUtils.elementToAttributeMap(el, elementAttributes);
				
				
				Widget w = widgetBuilder.buildWidget(property.getName(),elementAttributes);
				widgetMap.put(name, w);
				holder.add(w);
			}
			
		}
		
		
		private void buildFlexTableWidget(UniversalPanel holder, Map<String,String> flexTableAttributes, Element wgElement) {
			FlexTableBuilder fBuilder = new FlexTableBuilder();
			NodeList childNodes = wgElement.getChildNodes();
			
			List<GenericObjectProperty> propList = new ArrayList<GenericObjectProperty>();
			
			
			for (int i=0;i<childNodes.getLength();i++){
				Element el=(Element)childNodes.item(i);
				String name = el.getAttribute(MetaAttributes.NAME);
				if (name == null)
					throw new IllegalArgumentException("Unable to find "+MetaAttributes.NAME+" attribute");
				GenericObjectProperty property = propertyMap.get(name);
				if (property==null)
					throw new RuntimeException("Unable to find '"+XMLTags.EntityAttribute+"' with name '"+name+"'");
				
				Map<String,String> elementAttributes = new HashMap<String, String>(property.getAttributes());				
				XMLUtils.elementToAttributeMap(el, elementAttributes);
				
				GenericObjectProperty prp = new GenericObjectProperty(elementAttributes);
				propList.add(prp);		
			}
			
			FlexTableWidgetHelper helper = new FlexTableWidgetHelper() {
				
				@Override
				protected Widget getWidget(GenericObjectProperty prop) {
					String name=prop.getName();
					if (widgetMap.containsKey(name))
						return widgetMap.get(name);
					
					Widget w = widgetBuilder.buildWidget(prop.getName(), prop.getAttributes());
					widgetMap.put(name, w);
					return w;
				}
			};
			
			helper.buildFlexTableWidget(fBuilder, propList, flexTableAttributes);
			holder.add(fBuilder.buildTable());

		}
		
		
		

}
*/