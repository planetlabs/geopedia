package com.sinergise.geopedia.client.ui.map.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;

public class ScaleLabel extends FlowPanel  implements CoordinatesListener{

	private SimplePanel bar_l;
	private DisplayCoordinateAdapter dca;
	
	private int[] sizes=new int[] {
	            76, 76, 92, 92, 
	            122, 122, 98, 
	            98, 98, 78, 
	            78, 78, 94, 94, 
	            125, 125, 100, 
	            100, 100, 100, 
	            80, 80, 96, 96, 
	            128, 128, 102, 
	            102, 102, 102, 
	            82};
	private String[] texts=new String[] {
	            "10.000 km","5000 km","3000 km","1500 km",
	            "1000 km","500 km", "200 km", 
	            "100 km", "50 km", "20 km", 
	            "10 km", "5 km", "3 km", "1,5 km", 
	            "1 km", "500 m", "200 m", 
	            "100 m", "50 m", "25 m", 
	            "10 m", "5 m", "3 m", "1,5 m",
	            "1 m", "50 cm", "20 cm",
	            "10 cm", "5 cm", "2,5 cm",
	            "1 cm"
	            };
	 
	    
	
	
	public ScaleLabel(DisplayCoordinateAdapter dca) {
		setStyleName("geopedia-scale");
		
		this.dca = dca;
		bar_l = new SimplePanel();
		bar_l.setStyleName("geopedia-scale-bar_l");
		SimplePanel bar_r = new SimplePanel();
		bar_r.setStyleName("geopedia-scale-bar_r");
		
		add(bar_l);
		add(bar_r);
		
		updateScale(getScaleLevel(dca.getScale()));		
		dca.addCoordinatesListener(this);
	}
	
	private int getScaleLevel(double scale) {
		return dca.getPreferredZoomLevels().nearestZoomLevel(scale, dca.pixSizeInMicrons);
	}

	private void updateScale(int zoomLevel) {
		bar_l.setWidth((sizes[zoomLevel]-4)+"px");
		DOM.setInnerText(bar_l.getElement(), texts[zoomLevel]);		
	}
	
	
	

	@Override
	public void coordinatesChanged(double newX, double newY, double newScale,
			boolean coordsChanged, boolean scaleChanged) {
		if (scaleChanged) {
			updateScale(getScaleLevel(newScale));
		}	
		
	}

	@Override
	public void displaySizeChanged(int newWidthPx, int newHeightPx) {
		// TODO Auto-generated method stub
		
	}
}
