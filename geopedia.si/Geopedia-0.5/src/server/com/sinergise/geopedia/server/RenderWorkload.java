package com.sinergise.geopedia.server;

import java.util.regex.Pattern;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.db.DBUtil;

/**
 *
 * workload path specification:
 * 
 * path := '/' servlet '/' tileID '/' layers
 * servlet := 'rr' | 'rp'
 * tileID := // tile id as defined in coords_mapping.txt
 * layers := layerspec (',' layerspec)*
 * 
 * layerspec := directlayer | themelayer
 * 
 * directlayer := 'l' table_id '@' table_timestamp ':' drawonoff
 * table_id := idnumber
 * table_timestamp := timestamp
 * drawonoff := ('l' | 'f' | 's' | 't'){1,4}
 * 
 * themelayer := 't' theme_id '@' theme_timestamp '.' link_id '.' table_id '@' table_timestamp ':' drawonoff
 * theme_id := idnumber
 * link_id := idnumber
 * theme_timestamp := timestamp
 * 
 * idnumber := // number between 1 and 999999999
 * timestamp := // 0 or number between 1000000000000 and 9999999999999
 * 
 * layer timestamps must be table.lastDataChange timestamps
 * theme timestamps must be theme.lastMetaChange timestamps
 * drawonoff letters signify 'l'ine, 'f'ill, 's'tyle, 't'ext, at least one must be present
 * 
 */
public class RenderWorkload extends AbstractWorkload { 
	
    
	

	public RenderWorkload(String path, TiledCRS tiledCRS) {
		super(path, tiledCRS);
	}

	public RenderWorkload(DimI tileSize) {
		super(tileSize);
	}
	
	public RenderWorkload() {
		super(DimI.EMPTY);
	}

	public static final String r_id = "([1-9][0-9]{0,8})";
	public static final String r_timestamp = "([@](0|[1-9][0-9]{12}))";
	public static final String r_whatspec = "([:][lfst]{1,4})";
	public static final String r_plainLayerSpec = "(l"+r_id+r_timestamp+r_whatspec+")";
	public static final String r_themeLayerSpec = "(t"+r_id+r_timestamp+"[.]"+r_id+"[.]"+r_id+r_timestamp+r_whatspec+")";
	public static final String r_plainOrTheme = "("+r_plainLayerSpec+"|"+r_themeLayerSpec+")";
	public static final String r_layersSpec = r_plainOrTheme+"([,]"+r_plainOrTheme+")*([@][1-9][0-9]{8})?";
//	public static final String r_rpPath = "[/](" + ServUtil.r_lAll_SLO + ")[/]" + r_layersSpec;

	private static String createRenderPath(TiledCRS tiledCRS) {
		return  "[/](" + ServUtil.createTilePattern(tiledCRS) + ")[/]" + r_layersSpec;
	}

	private static Pattern[] renderSpecs;
	
	/**
	 * Must be called AFTER <code>com.sinergise.geopedia.ServerInstance</code> is initialized
	 */
	public static void initialize() {
		renderSpecs = new Pattern[ServerInstance.getMaxInstanceId()+1];
		for (ServerInstance instance:ServerInstance.allInstances()) {
			if (instance!=null) { // might be uninitialized
				renderSpecs[instance.getId()] =  Pattern.compile(createRenderPath(instance.getCRSSettings().getMainCRS()));
			}
		}
	}
	
	
	public static boolean isValidRenderPath(int tiledCRSid, String path) {
		return renderSpecs[tiledCRSid].matcher(path).matches();
	}


	
	public void setWindowForTile(int tileLevel, int tileX, int tileY, TiledCRS tiledCRS) {
	    this.tileLevel = tileLevel;
	    pixSize = tiledCRS.zoomLevels.worldPerPix(tileLevel);
	    wMinX = tiledCRS.tileLeft(tileLevel, tileX);
	    wMinY = tiledCRS.tileBottom(tileLevel, tileY);
	    wMaxX = tiledCRS.tileRight(tileLevel, tileX);
	    wMaxY = tiledCRS.tileTop(tileLevel, tileY);
	  
//
//	    wMinX = TileUtil1.DEFAULT_CS.tileMinX(tileLevel, tileX);
//	    wMinY = TileUtil1.DEFAULT_CS.tileMinY(tileLevel, tileY);
//	    wMaxX = wMinX + pixSize * Globals.TILESIZE;
//	    wMaxY = wMinY + pixSize * Globals.TILESIZE;
	}
    

    public static RenderWorkload parsePath(String layers, int scale, long cx, long cy, int w, int h, TiledCRS tiledCRS) {
		RenderWorkload out = new RenderWorkload(tiledCRS.tileSizeInPix(scale));
		out.initialize(layers, scale, cx, cy, w, h, tiledCRS);
        return out;
    }
    

    protected void parseLayers(String layers) {
        String[] tmp = layers.length()>0?layers.split("[,]"):new String[0];
        this.layers = new WorkloadLayer[tmp.length];
		
		for (int a=0; a<tmp.length; a++) {
			WorkloadLayer l = new WorkloadLayer();
			this.layers[a] = l;
			String spec = tmp[a];
			if (spec.startsWith("l")) {
				l.hasTheme = false;
				l.themeId = 0;
				l.ttlId = 0;
				l.themeTime = 0;
				// r_plainLayerSpec = "(l"+r_id+r_timestamp+r_whatspec+")"
				int afna = spec.indexOf('@', 1);
				int dvop = spec.indexOf(':', afna);
				l.tableId = DBUtil.parseInt(spec, 1, afna);
				l.tableTime = Long.parseLong(spec.substring(afna+1, dvop));
				l.drawOnOff = parseOnOff(spec, dvop+1, spec.length());
				
			} else if (spec.startsWith("t")) {
				l.hasTheme = true;
				// r_themeLayerSpec = "(t"+r_id+r_timestamp+"[.]"+r_id+"[.]"+r_id+r_timestamp+r_whatspec+")"
				int afna1 = spec.indexOf('@', 1);
				int pika1 = spec.indexOf('.', afna1+1);
				int pika2 = spec.indexOf('.', pika1+1);
				int afna2 = spec.indexOf('@', pika2+1);
				int dvop = spec.indexOf(':', afna2+1);
				
				l.themeId = DBUtil.parseInt(spec, 1, afna1);
				l.themeTime = Long.parseLong(spec.substring(afna1+1, pika1));
				l.ttlId = DBUtil.parseInt(spec, pika1+1, pika2);
				l.tableId = DBUtil.parseInt(spec, pika2+1, afna2);
				l.tableTime = Long.parseLong(spec.substring(afna2+1, dvop));
				l.drawOnOff = parseOnOff(spec, dvop+1, spec.length());
			} else {
				throw new IllegalStateException("unknown layer spec: "+spec);
			}
		}
    }
    /**
     * added this method to be used by WebMapServlet
     */
//    public static RenderWorkload parseLayers(String layers) {
//        RenderWorkload out = new RenderWorkload();
//        parseLayers(layers, out);
//        return out;
//    }

	private static int parseOnOff(String spec, int start, int end)
    {
		int out = ThemeTableLink.ON_DISPLAY;

		while (start < end) {
			switch(spec.charAt(start++)) {
			case 'l': out |= ThemeTableLink.ON_LINE; break;
			case 'f': out |= ThemeTableLink.ON_FILL; break;
			case 's': out |= ThemeTableLink.ON_SYMBOL; break;
			case 't': out |= ThemeTableLink.ON_TEXT; break;
			}
		}
		
		return out;
    }

	//TODO: Check if this is used and delete if it isn't
	public int[] getInterleavedIds()
    {
		int[] out = new int[2 * layers.length];
		int pos = 0;
		for (WorkloadLayer l : layers) {
			out[pos++] = l.themeId;
			out[pos++] = l.tableId;
		}
		return out;
	}


	public boolean hasBaseLayers() {
		if (baseLayers==null || baseLayers.length==0)
			return false;
		return true;
	}
}
