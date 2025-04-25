/*
 *
 */
package com.sinergise.java.swing.map.style;

import java.awt.Graphics2D;
import java.awt.Shape;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.common.util.settings.Settings.TypeMap;


@TypeMap(
	names = {"COMPOSITE",			"FILL",					"LINE"},
	types = {CompositeStyle.class,	DefaultFillStyle.class,	DefaultLineStyle.class}
)
public interface VectorStyle extends Settings {
    void draw(Shape seq, DisplayCoordinateAdapter dca, Graphics2D g);
}
