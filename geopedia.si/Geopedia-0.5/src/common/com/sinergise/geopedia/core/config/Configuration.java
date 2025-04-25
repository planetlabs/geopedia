package com.sinergise.geopedia.core.config;

import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;

public class Configuration implements Serializable {
	
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	private static final long serialVersionUID = 1L;
	
//	public static WithBounds [] SUPPORTED_CRSs = {
//		(WithBounds)HRTransforms.TILES_HR
//		(WithBounds)TnzTransforms.TILES_TNZ
//		(WithBounds)TiledCRS.GP_SLO,
//		(WithBounds)NorTransforms.TILES_NOR,
//		(WithBounds)GbrTransforms.GB_TILES
//		
//	};
	
	public int defaultThemeId = 105;
	
    public IsURLProvider publicRenderers;
    public IsURLProvider dynamicRasters;
    public String publicFeatureHighlightBasePath;

    public BaseLayer[] datasetsConf;
       
    public ThemeBaseLayers defaultBaseLayers;
    
    //FIXME: THIS HAS NO USE RIGHT??
    public Copyright[] copyrights;
    
    public ArrayList<Copyright> staticCopyrightList = new ArrayList<Copyright>();
    
    public int widgetThemeId = Integer.MIN_VALUE;
    
    public boolean hasDMV = false;
 
    
    public BaseLayer[] getDatasetsFor(int[] ids) {
        if (ids.length==0) return new BaseLayer[0];
        if (ids.length==1) return new BaseLayer[] {getBaseLayerConfiguration(ids[0])};
        throw new RuntimeException("Combos not supported anymore!");
    }

    public BaseLayer getBaseLayerConfiguration(int id) {
    	if (id<0 || id >= datasetsConf.length)
    		return null;
    	return datasetsConf[id];
    }
    
    
    public int findCopyright(BaseLayer conf, int scale) {
        String cpyName=null;
        for (int i = 0; i < conf.copyrightScales.length; i++) {
            int cpyScale=conf.copyrightScales[i];
            if (scale<=cpyScale) {
                cpyName=conf.copyrights[i];
                break;
            }
        }
        if (cpyName==null) return -1;
        for (int i = 0; i < copyrights.length; i++) {
            if (copyrights[i].id.equals(cpyName)) return i;
        }
        return -1;
    }
    
    public int findCopyrightByName(String name) {
        if (name==null) return -1;
        for (int i = 0; i < copyrights.length; i++) {
            if (copyrights[i].id.equals(name)) return i;
        }
        return -1;
    }
    
//    public int[] getDefaultStaticDatasets() {
//    	int cnt=0;
//    	for (DatasetConf dc : staticDatasets) {
//			if (dc.defaultEnabled) cnt++;
//		}
//    	int[] ret = new int[cnt];
//    	int i=0;
//    	for (DatasetConf dc : staticDatasets) {
//    		if (dc.defaultEnabled) ret[i++] = dc.id;
//		}
//    	return ret;
//    }
//    
    
    
    public boolean themeHasStaticDataset(Theme theme, int staticID) {
    	if (theme.baseLayers == null) {
    		return defaultBaseLayers.hasBaseLayer(ThemeBaseLayers.SETTING_DEFAULT, staticID);
    	} else {
    		return theme.baseLayers.hasBaseLayer(ThemeBaseLayers.SETTING_DEFAULT, staticID);
    	}
    }

}
