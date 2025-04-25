package com.sinergise.common.geometry.crs.misc;

import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.tiles.TiledCRS;

public class SenegalTransforms {

	public static UTM WGS84_UTM28N = TransverseMercator.createWGS84N(28);
	
	public static final TMToGeographic FROM_WGS_UTM28N_TO_WGS84 = new TMToGeographic(WGS84_UTM28N);
	public static final GeographicToTM FROM_WGS84_TO_WGS_UTM28N = FROM_WGS_UTM28N_TO_WGS84.inverse();
	
	public static TiledCRS SEN_TILES = TiledCRS.createDefault(WGS84_UTM28N, "Senegal tiles", 35000, 1100000, 512, 6);
	
//	public static void main(String[] args) {
//		Envelope m = SEN_TILES.getBounds().mbr;
//		LineString ls = new LineString(new double[] {
//			m.getMinX(), m.getMinY(),
//			m.getMaxX(), m.getMinY(),
//			m.getMaxX(), m.getMaxY(),
//			m.getMinX(), m.getMaxY(),
//			m.getMinX(), m.getMinY()
//		});
//		System.out.println(WKTWriter.write(ls));
//		TileUtilJava.saveAsXML(SEN_TILES, System.out);
//	}
	
}
