/*
 *
 */
package com.sinergise.gwt.gis.map.ui.controls;


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.util.StyleConsts;


public class ScaleBox extends Composite {
	private static final NumberFormatter LEN_FORMATTER = ApplicationContext.getInstance().getDefaultLengthFormatter();
    private static final double[] PREFIXES = new double[]{1, 5, 2.5, 7.5};
    
    HTML lbl=new HTML("? m");
    FlowPanel vp;
    private DisplayCoordinateAdapter dca;
    
    public ScaleBox(DisplayCoordinateAdapter dca) {
        this.dca=dca;
        
        SimplePanel left = new SimplePanel();
        SimplePanel right = new SimplePanel();
        
        left.setStyleName ("left");
        right.setStyleName("right");
        lbl.setStyleName  ("scaleLbl");
        
        vp = new FlowPanel();
        vp.setWidth("100px");
        vp.add(right);
        vp.add(left);
        vp.add(lbl);
        initWidget(vp);
        setStyleName(StyleConsts.MAP_SCALE_DISPLAY_BOX);
        
        dca.addCoordinatesListener(new CoordinatesListener() {
            @Override
			public void coordinatesChanged(double newX, double newY,
                    double newScale, boolean coordsChanged, boolean scaleChanged) {
                if (scaleChanged) updateScale();
            }
            @Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {
            }
        });
    }
    
    double[] tempArr=new double[2];
    public void updateScale() {
        double len = MathUtil.clamp(40, dca.getDisplayWidth()/3, 100) * dca.worldLengthPerPix;
        double rndLen = MathUtil.roundToList(len, PREFIXES, tempArr);
        int size=(int)Math.round(rndLen/dca.worldLengthPerPix);

        vp.setWidth(size+"px");

        lbl.setHTML(LEN_FORMATTER.format(rndLen));
    }
}