package com.sinergise.geopedia.client.core.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.core.entities.Feature;

public class FeatureHashMap {
	private HashMap<Integer, TableFeatures> tables = new HashMap<Integer, TableFeatures>();
	private double minX, maxX, minY, maxY;
	
	
	
	public Iterator<Map.Entry<Integer, TableFeatures>> getTableIterator() {
		return tables.entrySet().iterator();
	}
	
	
	public boolean isVisible (TiledCRS crs, int zoomLevel, int column, int row) {
		double extend = 40* crs.zoomLevels.worldPerPix(zoomLevel);
			
			Envelope tileEnvelope = new Envelope(crs.tileLeft(zoomLevel, column),
					crs.tileTop(zoomLevel, row), crs.tileRight(zoomLevel, column),
					crs.tileBottom(zoomLevel, row));
			Envelope featEnv = new Envelope(minX-extend,minY-extend,maxX+extend,maxY+extend);
			if (tileEnvelope.intersects(featEnv))
				return  true;
		return false;
	}
	
	
//	public  boolean isVisible1(int scaleId, int left, int bot)
//	{
//		// Only show tiles where something is highlighted
//		if (minX<=maxX) {
//			double around=40*TileUtil1.tilePixSizeForLevel(TileUtil1.DEFAULT_CS, scaleId);
//			int idxMinX=TileUtil1.xTileIdx(TileUtil1.DEFAULT_CS, minX-around, scaleId);
//			if (left<idxMinX) return false;
//			int idxMinY=TileUtil1.xTileIdx(TileUtil1.DEFAULT_CS, minY-around, scaleId);
//			if (bot<idxMinY) return false;
//			int idxMaxX=TileUtil1.yTileIdx(TileUtil1.DEFAULT_CS, maxX+around, scaleId);
//			if (left>idxMaxX) return false;
//			int idxMaxY=TileUtil1.yTileIdx(TileUtil1.DEFAULT_CS, maxY+around, scaleId);
//			if (bot>idxMaxY) return false;
//		}
//		
//		return true;
//	}

	
	public void clearFeatures()
	{
		Iterator i = tables.entrySet().iterator();
		while (i.hasNext()) {
			java.util.Map.Entry e = (java.util.Map.Entry) i.next();
			TableFeatures tFeats = (TableFeatures) e.getValue();
		}
		
		tables.clear();
		
		minX=Double.POSITIVE_INFINITY;
		maxX=Double.NEGATIVE_INFINITY;
		minY=Double.POSITIVE_INFINITY;
		maxY=Double.NEGATIVE_INFINITY;
	}
	
	public void addFeature(Feature feature, int tableId)
	{
		//System.out.print("Adding feat "+featureId+" of table "+tableId+": ");
		Integer tkey = new Integer(tableId);
		TableFeatures tFeats = tables.get(tkey);
		if (tFeats == null) {
			tFeats = new TableFeatures(tableId);
			tables.put(tkey, tFeats);
		}
//		if (tFeats.tableGeomType>=0 && !GeomType.isSomething(tFeats.tableGeomType)) {
//			//System.out.println("Non-geom");
//			tables.remove(tkey);
//			return;
//		}
		tFeats.addFeature(feature);
		Envelope envelope = feature.envelope;
		if (envelope!=null) {
			minX=Math.min(minX, envelope.getMinX());
			minY=Math.min(minY, envelope.getMinY());
			maxX=Math.max(maxX, envelope.getMaxX());
			maxY=Math.max(maxY, envelope.getMaxY());
		}
	}
	
	public void removeFeature(int tableId, int featId)
    {
		Integer tkey = new Integer(tableId);
		TableFeatures tFeats = tables.get(tkey);

		if (tFeats != null) {
			tFeats.removeFeature(featId);
			if (tFeats.isEmpty()) {
				tables.remove(tkey);
			}
		}
    }
	
	public boolean isEmpty()
    {
		return tables.isEmpty();
    }	
}
