package com.sinergise.geopedia.server;

import java.util.regex.Pattern;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.db.DBUtil;

public class HighlightWorkload extends AbstractWorkload {
	
	public static final class HighlightWorkloadLayer extends WorkloadLayer  {
		public int[] featureIds;
	}
	
	public HighlightWorkload(String path, TiledCRS tiledCRS) {
		super(path, tiledCRS);
		init();
	}
	
	public HighlightWorkload(int w, int h) {
		super(new DimI(w, h));
		init();
	}
	
	private void init() {
		filterByScale=false;
	}

	private static final String r_id = "([1-9][0-9]{0,8})";
	private static final String r_timestamp = "([@](0|[1-9][0-9]{12}))";
	private static final String r_layerSpec = "(l"+r_id+r_timestamp+")";
	private static final String r_featureId = "([1-9][0-9]{0,9})";
	private static final String r_featureIds = "("+r_featureId+"(,"+r_featureId+")*)";
	private static final String r_layerFeatures = "("+r_layerSpec+":"+r_featureIds+")";
	private static final String r_layersSpec = r_layerFeatures+"(;"+r_layerFeatures+")*";
	
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


	
	

	protected void parseLayers(String layers) {
	        String[] tmp;
	        tmp = layers.split("[;]");
	        this.layers = new HighlightWorkloadLayer[tmp.length];
			
			for (int a=0; a<tmp.length; a++) {
				HighlightWorkloadLayer wlh = new HighlightWorkloadLayer();
				String spec = tmp[a];
				// r_plainLayerSpec = "("+r_layerSpec+":"+r_featureIds+")";
				int afna = spec.indexOf('@', 1);
				int dvop = spec.indexOf(':', afna);
				wlh.tableId = DBUtil.parseInt(spec, 1, afna);
				wlh.tableTime = Long.parseLong(spec.substring(afna+1, dvop));
				String[] ids = spec.substring(dvop+1).split(",");
				wlh.featureIds = ArrayUtil.parseInt(ids);
				this.layers[a] = wlh;
	    }
	}

}
