package com.sinergise.generics.gwt.widgets;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.util.XMLUtils;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.CreationResolver;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.widgets.components.WizardPanel;
import com.sinergise.generics.gwt.widgets.helpers.FlexTableWidgetHelper;
import com.sinergise.gwt.ui.UniversalPanel;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

public abstract class AbstractMasonWidget extends GenericWidget {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMasonWidget.class);

	protected static final String STYLE_MASON_SECTION ="masonSection";
	protected static final String STYLE_MASON_CORE ="masonWidget";
	protected static final String STYLE_MASON_STATUS ="masonStatusSection";
	
	protected static final String WIZARD="Wizard";
	protected static final String WIZARD_SECTION="WizardSection";
	
	
	public static final String META_ADDSTATUSPANEL = "addStatusPanel";
	
	
	protected CreationResolver creationResolver = new CreationResolver();
		
	
	private Map<String,Widget> widgetMap = new HashMap<String, Widget>();

	
	
	

	
	
	@Override
	public Map<String,GenericObjectProperty> getGenericObjectPropertyMap() {
		return propertyMap;
	}
	
	@Override
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
			 EntityType entityType) {
			throw new IllegalStateException("Mason widget requires mason attributes!");

	}
	
	
	protected abstract Widget getBasePanel();
	
	protected abstract ArrayList<String> buildWidget(NodeList nodes, InsertPanel widgetContaienr);
	protected abstract void buildStatusWidget(Map<String,String> masonAttributes, InsertPanel widgetContaienr);
	
	@Override
	public void build(List<GenericObjectProperty> properties, EntityType entityType, Element masonElement) {
		creationResolver.begin();
		creationResolver.addCreationListener(new CreationListener() {
			
			@Override
			public void creationCompleted(IsCreationProvider w) {
				widgetCreated();
			}
		});

		setEntityType(entityType);
		Map<String,String> masonAttributes = new HashMap<String, String>();
		XMLUtils.elementToAttributeMap(masonElement, masonAttributes);
		String widgetStyle = MetaAttributes.readStringAttr(masonAttributes, MetaAttributes.NAME, null);
		if (widgetStyle!=null) {
			getBasePanel().addStyleName(widgetStyle);
		}
		
		
		for (GenericObjectProperty p:properties){  // convert list to map 
			propertyMap.put(p.getName(),p);
		}
		

		NodeList childNodes = masonElement.getChildNodes();
		buildWidget(childNodes, (InsertPanel)getBasePanel());
		buildStatusWidget(masonAttributes,(InsertPanel)getBasePanel());				
		creationResolver.done();
	}
	
	protected ArrayList<String> buildDivWidget(UniversalPanel holder, Map<String,String> divAttributes, Element wgElement) {
		
		boolean showLables = MetaAttributes.readBoolAttr(divAttributes, MetaAttributes.WIDGET_DIV_SHOWLABLES, false);
		NodeList childNodes = wgElement.getChildNodes();
		ArrayList<String> widgetAttributes = new ArrayList<String>();
		for (int i=0;i<childNodes.getLength();i++){
			Element el=(Element)childNodes.item(i);
			String name = el.getAttribute(MetaAttributes.NAME);
			if (name == null)
				throw new IllegalArgumentException("Unable to find "+MetaAttributes.NAME+" attribute");
			GenericObjectProperty property = propertyMap.get(name);
			if (property==null) {
				continue;
//				throw new RuntimeException("Unable to find '"+XMLTags.EntityAttribute+"' with name '"+name+"'");
			}
			
			Map<String,String> elementAttributes = new HashMap<String, String>(property.getAttributes());				
			XMLUtils.elementToAttributeMap(el, elementAttributes);
			
			
			Widget w = widgetBuilder.buildWidget(property.getName(),elementAttributes);
			creationResolver.addIfCreationProvider(w);
			widgetMap.put(name, w);
			if (showLables) {
				holder.add(new Label(property.getLabel()));
			}
			holder.add(w);
			widgetAttributes.add(property.getName());
		}
		return widgetAttributes;
	}
	
	
	protected void applyStyle(Widget w, Map<String,String> wgAttributeMap) {
		if (w==null)
			return;
		String styleName = MetaAttributes.readStringAttr(wgAttributeMap, MetaAttributes.NAME, null);
		if (styleName!=null) {
			w.setStyleName(styleName);
		}
	}					
	protected ArrayList<String>  buildFlexTableWidget(UniversalPanel holder, Map<String,String> flexTableAttributes, Element wgElement) {
		FlexTableBuilder fBuilder = new FlexTableBuilder();
		NodeList childNodes = wgElement.getChildNodes();
		
		List<GenericObjectProperty> propList = new ArrayList<GenericObjectProperty>();
		
		ArrayList<String> widgetAttributes = new ArrayList<String>();
		
		for (int i=0;i<childNodes.getLength();i++){
			Element el=(Element)childNodes.item(i);
			String name = el.getAttribute(MetaAttributes.NAME);
			if (name == null)
				throw new IllegalArgumentException("Unable to find "+MetaAttributes.NAME+" attribute");
			logger.trace("Processing EntityAttribute {}.",name);
			GenericObjectProperty property = propertyMap.get(name);
			if (property==null) {
				continue;
			}
			
			Map<String,String> elementAttributes = new HashMap<String, String>(property.getAttributes());				
			XMLUtils.elementToAttributeMap(el, elementAttributes);
			
			GenericObjectProperty prp = new GenericObjectProperty(elementAttributes);
			propList.add(prp);		
			widgetAttributes.add(prp.getName());
		}
		
		FlexTableWidgetHelper helper = createFlexTableWidgetHelper();
		
		helper.buildFlexTableWidget(fBuilder, propList, flexTableAttributes);
		try {
			holder.add(fBuilder.buildTable());
		} catch (Throwable th) {
			throw new RuntimeException("Error building FlexTable for: '" 
					+ MetaAttributes.readStringAttr(flexTableAttributes, MetaAttributes.NAME, "unknown")+"'", th);
		}
		return widgetAttributes;
	}
	
	protected Widget flexTableGetWidget(GenericObjectProperty prop) {
		String name=prop.getName();
		if (widgetMap.containsKey(name))
			return widgetMap.get(name);
		
		Widget w = widgetBuilder.buildWidget(prop.getName(), prop.getAttributes());
		creationResolver.addIfCreationProvider(w);
		widgetMap.put(name, w);
		return w;
	}

	protected FlexTableWidgetHelper createFlexTableWidgetHelper() {
		return new FlexTableWidgetHelper() {
			@Override
			protected Widget getWidget(GenericObjectProperty prop) {
				return flexTableGetWidget(prop);
			}
		};
	}
	
	
	
	/** wizards **/
	/*
	
	public Map<String,WizardPanel> getWizardMap() {
		//return wizardsMap;
		return null;
	}
	
	
	public void addWizardActionListener(NamedObjectActionListener listener) {
		wizardActionListeners.add(listener);
	}
	
	private void wizardActionPerformed(String wizardName,Object action) {
		for (NamedObjectActionListener nol:wizardActionListeners) {
			nol.actionPerformed(wizardName, action);
		}
	}
	*/
	
	protected WizardPanel buildWizardSectionWidget(Map<String,String> wizardAttributes, Element wgElement) {
		NodeList nodes = wgElement.getChildNodes();
		/*
		String titleLabel = MetaAttributes.readStringAttr(wizardAttributes, MetaAttributes.LABEL, null);
		final String wizardName = MetaAttributes.readRequiredStringAttribute(wizardAttributes, MetaAttributes.NAME);
		*/
		WizardPanel panel = new WizardPanel(wizardAttributes);
		applyStyle(panel, wizardAttributes);
		panel.setAttributeList(buildWidget(nodes,panel));
		
		/*
		Button next= new Button(widgetConstants.masonWidgetWizardButtonNext());
		next.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//wizardActionPerformed(wizardName,WIZARD_ACTION_PART_COMPLETED);
			}
		});
		
		panel.add(next);*/
		return panel;
	}
	
}