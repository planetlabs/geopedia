package com.sinergise.geopedia.client.core.events;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sinergise.geopedia.core.entities.Feature;


/***
 * Fired when feature info related action occurs (new feature is selected, zoom to feature is requested,...)
 * 
 * 
 * @author pkolaric
 *
 */
public class FeatureInfoEvent extends Event<FeatureInfoEvent.Handler> {

	public static FeatureInfoEvent createShowDetailsAndHighlightEvent(Feature feature) {
		FeatureInfoEvent fie = new FeatureInfoEvent(feature);
		fie.highlight();
		fie.showFeatureDetails();
		return fie;
	}
	
	public interface Handler {
		void onFeatureInfo(FeatureInfoEvent event);
	}

	private Feature feature;
	
	/**
	 * Zoom to feature
	 */
	private boolean zoomTo=false;
	/**
	 * Highlight feature
	 */
	private boolean highlight=false;
	/**
	 * Show feature details
	 */
	private boolean showDetails=false;
	/**
	 * Update feature
	 */
	private boolean featureUpdated=false;
	
	public FeatureInfoEvent() {
		this(null);
	}
	public FeatureInfoEvent(Feature feature) {
		this.feature = feature;
	}
	
	
	private static final Type<FeatureInfoEvent.Handler> TYPE =
		        new Type<FeatureInfoEvent.Handler>();
	
	public static HandlerRegistration register(EventBus eventBus, FeatureInfoEvent.Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	} 

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onFeatureInfo(this);
		
	}
	
	public FeatureInfoEvent zoomTo() {
		if (feature!=null)
			this.zoomTo=true;
		return this;
	}
	
	public boolean hasZoomTo() {
		return zoomTo;
	}
	
	public FeatureInfoEvent highlight() {
		if (feature!=null)
			this.highlight=true;
		return this;
	}
	
	public boolean hasHighlight() {
		return highlight;
	}
	
	public FeatureInfoEvent showFeatureDetails() {
		if (feature!=null)
			this.showDetails=true;
		return this;
	}
	
	public boolean hasShowDetails() {
		return showDetails;
	}
	
	
	public FeatureInfoEvent updateFeature() {
		if (feature!=null)
			this.featureUpdated=true;
		return this;
	}
	
	public boolean hasFeatureUpdate() {
		return featureUpdated;
	}
	
	public Feature getFeature() {
		return feature;
	}
	
	
}