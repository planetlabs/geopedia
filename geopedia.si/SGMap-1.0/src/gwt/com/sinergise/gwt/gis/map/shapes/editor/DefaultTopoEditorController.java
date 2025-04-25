package com.sinergise.gwt.gis.map.shapes.editor;

import java.util.Collection;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.gwt.gis.map.shapes.snap.GridIndexedSnapProvider;
import com.sinergise.gwt.gis.map.shapes.snap.PolygonAreaSnapProvider;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class DefaultTopoEditorController extends TopoEditorController {
	
	protected SnapProvider layersSnapProvider;
	protected PolygonAreaSnapProvider areaSnapProvider;

	public DefaultTopoEditorController(MapComponent map) {
		super(map);
		
		registerSnapProvider(layersSnapProvider = new GridIndexedSnapProvider(map));
		registerSnapProvider(areaSnapProvider = new PolygonAreaSnapProvider(this, new SelectableImpl(false)));
		
		//enable layers snap provider by default
		layersSnapProvider.getEnabled().setSelected(true);
	}

	public SnapProvider getLayersSnapProvider() {
		return layersSnapProvider;
	}
	
	public PolygonAreaSnapProvider getAreaSnapProvider() {
		return areaSnapProvider;
	}

	/**
	 * //TODO: this is an ugly hack that should be removed once Polygon building is fixed;
	 * current problem is that active self-intersecting multipolygon is returned when a polygon 
	 * is split by a single segment with a small number of segments
	 *  
	 * @param ret
	 * @return
	 */
	@Deprecated
	protected Collection<CFeature> pruneByExtractingActivePolygon(Collection<CFeature> ret) {
		if (ret.isEmpty()) {
			return ret;
		}
		CFeature[] c = ret.toArray(new CFeature[ret.size()]);
		CFeature actv = c[0];
		Polygon activePoly = null;
		Geometry oldActiveGeom = actv.getGeometry();
		
		//Select the first part of multipolygon as the active polygon
		if (oldActiveGeom instanceof Polygon) {
			activePoly = (Polygon)oldActiveGeom;
		} else { // oldActiveGeom instanceof MultiPolygon 
			activePoly = ((MultiPolygon)oldActiveGeom).get(0);
			actv.setGeometry(activePoly);
		}
		
		//Remove the active polygon from all other multipolygon geometries
		for (int i = 1; i < c.length; i++) {
			Geometry g = c[i].getGeometry();
			if (g instanceof MultiPolygon) {
				MultiPolygon mp = (MultiPolygon)g;
				if (mp.contains(activePoly)) {
					Polygon[] newArr = new Polygon[mp.size()-1];
					int j = 0;
					for (Polygon p : mp) {
						if (!activePoly.equals(p)) {
							newArr[j++] = p;
						}
					}
					if (newArr.length == 1) {
						c[i].setGeometry(newArr[0]);
					} else {
						c[i].setGeometry(new MultiPolygon(newArr));
					}
				}
			}
		}
		return ret;
	}
	
}
