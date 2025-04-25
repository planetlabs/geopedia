package com.sinergise.java.raster.util;

import java.io.File;

import com.sinergise.common.geometry.tiles.TiledCRS;

public class GiselleLayerMapping {

    public static String toGiselleLayerXML(boolean on, TiledCRS cs, int maxLevel, String type, File outDir) {
		String layerName = outDir.getName();
		if ("out".equals(layerName)) {
			layerName = outDir.getParentFile().getName();
		}
		String baseDir = outDir.getAbsolutePath() + "/";
		double res = cs.zoomLevels.worldPerPix(maxLevel) / TiledCRS.GP_SLO.zoomLevels.worldPerPix(maxLevel);
		
//		<Layer name="CIR" on="false" color="ff000000" type="WMS_GP"
//			topoType="8" remote="true" fromScale="10" toScale="500000000" hidden="false"
//			snapon="false" active="false" group="Rastri" host="http://10.88.1.31/dof/cir/"
//			layers="DOF" onoff="" genratedTileSize="256" tileOffsetX="430000.0"
//			tileOffsetY="480000.0" worldOffsetX="0.0" worldOffsetY="0.0"
//			minLevel="7" maxLevel="18" prefixChar="M" imageExtension=".jpg"
//			timeoutSeconds="10">
//			<Line alpha="1.0" color="ff00ff00" draw="true" name="Line"
//				width="1.0" units="pixels" capJoin="2" />
//			<Text alpha="1.0" color="ff000000" draw="false" name="Text"
//				font="Dialog-plain-12" rotation="0" value="" size="12" units="pixels"
//				maxScale="-1" calcOptimalPt="true" calcOptimalPtWindow="false"
//				drawGlow="true" glowColor="ffffffff" offsetX="0" offsetY="0" align="center" />
//			<RasterProperty alpha="1.0" color="ff000000" draw="true"
//				name="RasterProperty" fromScale="500" toScale="5000" showLines="0"
//				offsetx="0.0" offsety="0.0" transparent="false" />
//		</Layer>
		
		return "<Layer name=\""+layerName+"\" on=\""+on+"\" " + "fromScale=\"0\" toScale=\"500000000\" \n" +
			"host=\""+baseDir+"\" \n"+
			"worldOffsetX=\""+cs.tileLeft(maxLevel, 0)+"\" worldOffsetY=\""+cs.tileBottom(maxLevel, 0)+"\" " +
			"tileOffsetX=\"0\" tileOffsetY=\"0\" timeoutSeconds=\"10\" \n"+
			"genratedTileSize=\""+cs.tileSizeInPix(maxLevel).w()+"\" " +
			"minLevel=\""+cs.getMinLevelId()+"\" maxLevel=\""+maxLevel+"\" \n"+
			"prefixChar=\""+cs.getTilePrefixChar()+"\" imageExtension=\"."+type+"\" resX=\""+res+"\" resY=\""+res+"\" "+
			"namesInterlaced=\""+cs.isInterlacedName()+"\" \n" +
			"layers=\"DOF\" snapon=\"false\" active=\"false\" color=\"ffffffff\" type=\"WMS_GP\" topoType=\"8\" remote=\"true\" hidden=\"false\" >\n"+
			"    <Line draw=\"false\" name=\"Line\" />\n" +
			"    <Text draw=\"false\" name=\"Text\" />\n" +
			"    <RasterProperty alpha=\"1.0\" color=\"ffffffff\" draw=\"true\" name=\"RasterProperty\" transparent=\"false\" />\n"+
			"</Layer>";
	}
    
}
