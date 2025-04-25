package com.sinergise.geopedia.client.ui.map.widgets;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.layers.Rasters;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.config.Copyright;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;
import com.sinergise.gwt.util.html.CSS;

public class CopyrightPanel extends FlowPanel implements CoordinatesListener,
		Rasters.Listener {

	private String copyrightDate = "\u00A9" + Globals.COPYRIGHT_DATE + " ";
	private Rasters rasters;
	private DisplayCoordinateAdapter dca;

	private  ArrayList<Copyright> staticCopyrights;

	private ArrayList<Copyright> currentCopyrights = new ArrayList<Copyright>();

	public CopyrightPanel(Rasters rasters, DisplayCoordinateAdapter dca) {
		this.rasters = rasters;
		this.dca = dca;
		staticCopyrights = createStaticCopyrights();

		rasters.addListener(this);
		dca.addCoordinatesListener(this);
		updateLabel();

	}
	protected ArrayList<Copyright> createStaticCopyrights() {
		ArrayList<Copyright> list =  new ArrayList<Copyright>();
		
		Configuration config = ClientGlobals.configuration;
		if(config.staticCopyrightList.isEmpty()){
			//default
			Copyright sgs = new Copyright();
			sgs.id = "sinergise";
			sgs.name = "Sinergise d.o.o.";
			sgs.refURL = "http://www.sinergise.com";
			
			
			list.add(sgs);
		} else {
			list.addAll(config.staticCopyrightList);
		}
		return list;
	}
	private void addSeparator() {
		add(new InlineHTML(" | "));
	}

	public ArrayList<Copyright> getCopyrights() {
		return currentCopyrights;
	}

	private void updateLabel() {

		Configuration config = ClientGlobals.configuration;
		clear();
		currentCopyrights.clear();			

		InlineHTML copyLbl = new InlineHTML(copyrightDate);
		add(copyLbl);

		if (staticCopyrights!=null) {
			boolean separator = false;
			InlineHTML comma = new InlineHTML(",&nbsp;");
			CSS.marginLeft(comma.getElement(), -5);
			CSS.marginRight(comma.getElement(), 0);
			for (final Copyright cpy:staticCopyrights) {
				Anchor anch = new Anchor(cpy.name);
				anch.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.open(cpy.refURL, "_blank", null);
					}
	
				});
				if (separator) {
					add(comma);
				}
				separator = true;
				add(anch);
			}
		}
	
		if (config != null) {
			boolean addSeparator = false;
			
			BaseLayer visibleBaseLayer = rasters.getVisibleBaseLayer();
			if (visibleBaseLayer != null) {
				int scaleLevel = dca.getPreferredZoomLevels().nearestZoomLevel(
						dca.getScale(), dca.pixSizeInMicrons);
				ArrayList<Copyright> copyrightList = visibleBaseLayer.getCopyrights(scaleLevel);
				if (copyrightList.size()>0) {
					addSeparator();
					add(new InlineHTML(Messages.INSTANCE.CopyrightPanel_Data()));
					
					for (final Copyright cpy:copyrightList) {
						currentCopyrights.add(cpy);
						Anchor anch = new Anchor(cpy.name);
						anch.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Window.open(cpy.refURL, "_blank", null);
							}
	
						});
						if (addSeparator) {
							add(new InlineHTML(",&nbsp;"));
						}
						addSeparator = true;
						
	
						add(anch);
					}
				}
				
			}

		}

	}

	@Override
	public void rastersChanged(boolean justOnOff) {
		updateLabel();
	}


	@Override
	public void coordinatesChanged(double newX, double newY, double newScale,
			boolean coordsChanged, boolean scaleChanged) {
		if (scaleChanged) {
			updateLabel();
		}
	}

	@Override
	public void displaySizeChanged(int newWidthPx, int newHeightPx) {
		// TODO Auto-generated method stub
		
	}
}
