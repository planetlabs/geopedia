/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sinergise.java.swing.map.layer;

import java.awt.Graphics2D;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.common.util.settings.Settings.TypeMap;
import com.sinergise.java.swing.map.OrDisplay;
import com.sinergise.java.swing.map.PaintOperation;
import com.sinergise.java.swing.map.layer.misc.GridLayer;
import com.sinergise.java.swing.map.raster.ImagePyramidLayer;


/**
 * @author Miha Kadunc (<a href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
@TypeMap(
	names = {"GRID",			"IMAGE_PYRAMID"},
	types = {GridLayer.class,	ImagePyramidLayer.class}
	)
public interface OrLayer extends Settings {
    Object IS_BACKGROUND = null;
    
	void addedToDisplay(OrDisplay display);
	void removedFromDisplay(OrDisplay display);
	void paintLayer(Graphics2D g, DisplayCoordinateAdapter dca, PaintOperation mgr);
	Envelope getBounds();
	void setName(String name);
	String getName();
    LayerProperties getProperties();
    LayerPerformanceInfo getPerformanceInfo();
}