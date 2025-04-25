package com.sinergise.geopedia.client.ui.panels.results;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.events.FeatureInfoEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.feature.FeatureInfoContentPanel;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Table;
/*
 * FIXME: when a new field is added and selected as reptext, already displayed feature's reptext is not updated!
 */
public class FeatureInfoWidget extends FlowPanel implements HasText {
	private Feature feature;
	private FeatureInfoContentPanel featureInfoWidget = null;
	private FlowPanel sri =null;	
	private ToggleButton btnHighlight;
	private PushButton btnZoomTo;
	private Table table;
	private boolean detailsShown = false;
    private Image symbolImage;
    private SimplePanel titleText;
    
    
    private FlowPanel btnHolder = new FlowPanel();
    
    public FeatureInfoWidget(final Feature feature, Table table) {
    	addStyleName("featureHolder");
    	this.feature=feature;
    	buildItem();
    	this.table = table;
    	detailsShown = false;
    	
    	FeatureInfoEvent.register(ClientGlobals.eventBus, new FeatureInfoEvent.Handler() {
			
			@Override
			public void onFeatureInfo(FeatureInfoEvent event) {
				if (feature.equals(event.getFeature())) {
					
					if (event.getFeature().isDeleted()) {
						removeFromParent();
						return;
					}
					
					if (event.hasFeatureUpdate()) {
						FeatureInfoWidget.this.feature = event.getFeature();
						updateUI();	
					}
					
					
					if (event.hasShowDetails()) {
						showContent();
					} else {
						hideContent();
					}
					if(event.hasHighlight()) {
						btnHighlight.setDown(true);
					}
				} else {
					hideContent();
				}
				
			}
		});
    }
    
   
    public FlowPanel getButtonsHolder() {
    	return btnHolder;
    }
    
    public Table getTable() {
    	return table;
    }
    public GeomType getGeometryType () {
    	return table.geomType;
    }
    
    public FeatureInfoContentPanel getFeatureInfoContentPanel() {
    	if (featureInfoWidget == null) {
    		FeatureInfoContentPanel fip = FeatureInfoContentPanel.createNew(FeatureInfoWidget.this);
    		fip.setFeature(feature, table);
    		fip.setStyleName("content");
    		add(fip);
    		featureInfoWidget = fip;
    	}
    	return featureInfoWidget;
    }
    
    
    
    private void updateUI() {
    	if (titleText!=null) {
    		DOM.setInnerHTML(titleText.getElement(), feature.getTextDesc());
    	}
    	
    	if (symbolImage!=null && feature.styleSymbolId!=Integer.MIN_VALUE) {
			String imgURL = "sicon/sym/"+Integer.toString(feature.styleSymbolId)+
			"?c1=0x"+Integer.toHexString(feature.styleColor)+
			"&ss=20";
			symbolImage.setUrl(GWT.getHostPageBaseURL()+imgURL);
    	}
    	if (featureInfoWidget!=null) {
    		featureInfoWidget.setFeature(feature, table);
    	}
    }

    
    
    
    private void buildItem() {
		sri = new FlowPanel();
		
		sri.setStyleName("resultsItem");
		

		SimplePanel num = new SimplePanel();
		num.setStyleName("tocka");
		if (feature.styleSymbolId!=Integer.MIN_VALUE) {
			symbolImage = new Image();
			num.add(symbolImage);
		}
		
		
		
		titleText = new SimplePanel()  {
		      @Override
				public void onBrowserEvent(Event event) {
					super.onBrowserEvent(event);
		            if (DOM.eventGetType(event)==Event.ONCLICK) {
		            	// click shows/hides feature
		            	FeatureInfoEvent fiEvent = new FeatureInfoEvent(feature);
		            	if (!detailsShown) {
		            		fiEvent.highlight();
		            		fiEvent.showFeatureDetails();
		            	}
		            	ClientGlobals.eventBus.fireEvent(fiEvent);
		            }
				}
			};
		titleText.sinkEvents(Event.ONCLICK);
		titleText.setStyleName("naslov");
		
		
		Element b = DOM.createElement("b");
		btnHighlight = new ToggleButton();
		btnHighlight.getElement().appendChild(b);
		btnHighlight.addStyleName("highlight");
		btnHighlight.setTitle(Messages.INSTANCE.highlight());
		btnHighlight.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FeatureInfoEvent fiEvent = new FeatureInfoEvent(feature);
				fiEvent.showFeatureDetails();
				if (btnHighlight.isDown()) {
					fiEvent.highlight();
				}
				ClientGlobals.eventBus.fireEvent(fiEvent);
			}
		});
		
		boolean hasZoomTo = this.feature.getGeometryType() != null && !GeomType.NONE.equals(this.feature.getGeometryType());
			
		if(hasZoomTo){
			Element c = DOM.createElement("b");
			btnZoomTo = new PushButton();
			btnZoomTo.getElement().appendChild(c);
			btnZoomTo.addStyleName("zoomto");
			btnZoomTo.setTitle(Messages.INSTANCE.zoomTo());
			btnZoomTo.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					FeatureInfoEvent fie = new FeatureInfoEvent(feature);
					fie.zoomTo();
					if (btnHighlight.isDown())
						fie.highlight();
					fie.showFeatureDetails();
					ClientGlobals.eventBus.fireEvent(fie);
				}
			});
		}
		btnHolder.add(btnHighlight);
		if(hasZoomTo)
			btnHolder.add(btnZoomTo);
		btnHolder.setStyleName("btnHolder");
		
		sri.add(btnHolder);
		sri.add(num);
		sri.add(titleText);
		add(sri);
		updateUI();
    }
    
      
   
    public String getText() {
            return DOM.getInnerText(getElement());
    }

    public void setText(String text) {
            DOM.setInnerText(getElement(), (text == null) ? "" : text);
    }


	private void hideContent() {
		getFeatureInfoContentPanel().setVisible(false);
		sri.removeStyleDependentName("shown");
		detailsShown=false;
		btnHighlight.setDown(false);
	}


	private void showContent() {
		getFeatureInfoContentPanel().setVisible(true);
		sri.addStyleDependentName("shown");
		detailsShown=true;
	}

	public Feature getFeature() {
		return feature;
	}

}
