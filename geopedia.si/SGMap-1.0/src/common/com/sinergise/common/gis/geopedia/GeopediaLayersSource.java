/*
 *
 */
package com.sinergise.common.gis.geopedia;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.gis.map.model.layer.LayerSpec;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.naming.Identifier;

public class GeopediaLayersSource implements LayersSource {
    public static final String SOURCE_TYPE_GP = "Geopedia";

	public static abstract class GeopediaAbstractSpec extends LayerSpec {
    	private static final long serialVersionUID = 1L;
    	
    	public String title;
    	protected Envelope bounds;
    	public TiledCRS crs;
    	protected boolean opaque = false;
    	
    	 public GeopediaAbstractSpec(String id, String title) {
    		 this(id, title, GeopediaLayersSource.GP_TILES);
    	 }
    	
        public GeopediaAbstractSpec(String id, String title, TiledCRS crs) {
            super(INSTANCE, id);
            this.title=title;
            this.crs = crs;
            bounds = crs.getBounds().mbr;
        }
        
        @Override
        public Envelope getBoundingBox() {
        	return bounds;
        }

        public String getTitle() {
        	return title;
        }

        @Override
        public List<? extends LayerSpec> getChildren() {
        	return Collections.emptyList();
        }
        
        public TiledCRS getCRS() {
        	return crs;
        }

        abstract boolean isStatic();

        public void setOpaque(boolean opaque) {
			this.opaque = opaque;
		}
        
		public boolean isOpaque() {
			return opaque;
		}
    }
    public static class GeopediaLayerSpec extends GeopediaAbstractSpec {
    	private static final long serialVersionUID = 1L;
    	
        final int layerId;
        final String[] styles;
        
        public GeopediaLayerSpec(String title, int layerId, String[] themeStyles) {
        	this(GeopediaLayersSource.GP_TILES, title, layerId, themeStyles);
        }
        
        public GeopediaLayerSpec(TiledCRS crs, String title, int layerId, String[] themeStyles) {
        	super(String.valueOf(layerId),title, crs);
            this.layerId=layerId;
            this.styles=themeStyles;
        	setOpaque(false);
        }
        @Override
		boolean isStatic() {
            return false;
        }
    }

    public static class GeopediaStaticSpec extends GeopediaAbstractSpec {
    	private static final long serialVersionUID = 1L;
    	
        public String fullPath;
        public String imageExtension;

        public GeopediaStaticSpec(String title, String dataset, int maxLevel) {
        	this(title,  dataset, "http://dof505.geopedia.si"+dataset, "jpg", maxLevel);
        }

        public GeopediaStaticSpec(String title, String dataset, String fullPath, String imageExtension, int maxLevel) {
        	this(GeopediaLayersSource.GP_TILES, title, dataset, fullPath, imageExtension, maxLevel);
        }
        
        public GeopediaStaticSpec(TiledCRS crs, String title, String dataset, String fullPath, String imageExtension, int maxLevel) {
        	super(dataset, title, crs.createWithMaxLevel(maxLevel));
        	this.fullPath=fullPath;
        	this.imageExtension=imageExtension;
        	setOpaque(imageExtension.equalsIgnoreCase("jpg") || imageExtension.equalsIgnoreCase("jpeg"));
       }

		@Override
		boolean isStatic() {
            return true;
        }

		public String getDatasetPath() {
			return getLocalID();
		}

		public String getFullPath() {
			return fullPath;
		}

		public String getImageExtension() {
			return imageExtension;
		}
    }

    public static final TiledCRS GP_TILES = new GeopediaTiledCRS("Geopedia tiles", 19);

    public static final int MAX_LEVEL = 19;

    public static final int MIN_LEVEL = 7;

    public static final String NAME_LY_HS="HS";
    public static final String NAME_LY_KATOBC="KO";
    public static final String NAME_LY_NASELJA="NA";
    public static final String NAME_LY_OBCINE="OB";
    public static final String NAME_LY_REZI25="REZI 25";

    public static final String NAME_ST_DMV="DMR 12,5";
    public static final String NAME_ST_DMV_COLOR="DMR 12,5 barvni";
    public static final String NAME_ST_DOF="DOF 5";
    public static final String NAME_ST_DOF_DMV="DOF in DMR";
    public static final String NAME_ST_DTK="DTK";
    public static final String NAME_ST_DTK_250="DTK 250";
    public static final String NAME_ST_DTK_50="DTK 50";
    public static final String NAME_ST_DTK_25="DTK 25";
    public static final String NAME_ST_TK="Karte GI";

	public static final String NAME_GURS_DPK_250 = "GURS DPK 250";
	public static final String NAME_GURS_DTK_50 = "GURS DTK 50";

    public static final GeopediaLayersSource INSTANCE = new GeopediaLayersSource();
    static {
    	INSTANCE.initialize();
    }
    static char[] numChars = MathUtil.NUMERAL_CHARS;

    static int[] numCharsPerOrd;

    public static void appendLayerSpec(GeopediaLayer lyr, StringBuffer buf) {
        GeopediaAbstractSpec spc=(GeopediaAbstractSpec)lyr.getSpec();
        if (spc.isStatic()) throw new IllegalArgumentException("Only true layers (not rasters) can be passed");
        GeopediaLayerSpec ls=(GeopediaLayerSpec)spc;
        if (ls.styles==null || ls.styles.length==0) appendLayerSpec(ls.layerId, buf);
        else appendThemeLayerSpec(ls.layerId, ls.styles[0], buf);
    }

    public static void appendLayerSpec(int layerId, StringBuffer buf) {
        buf.append('l');
        buf.append(layerId);
        buf.append('@');
        buf.append(0);
        buf.append(':');
        appendLfst(buf);
    }
    public static void appendLfst(StringBuffer buf) {
      buf.append("lsft");
    }
    public static void appendThemeLayerSpec(int layerId, String themeLinkStr,  StringBuffer buf) {
        if (themeLinkStr==null || themeLinkStr.length()<1) {
            appendLayerSpec(layerId, buf);
            return;
        }
        buf.append(themeLinkStr);
        buf.append('.');
        buf.append(layerId);
        buf.append('@');
        buf.append(0);
        buf.append(':');
        appendLfst(buf);
    }
    public synchronized static int numCharsPerOrdinateForLevel(int level) {
        if (numCharsPerOrd == null) {
            numCharsPerOrd = new int[MAX_LEVEL - MIN_LEVEL + 1];
            numCharsPerOrd[0] = 1;
            for (int i = 1; i < numCharsPerOrd.length; i++) {
                numCharsPerOrd[i] = Long.toHexString((1 << i) - 1).length();
            }
        }
        return numCharsPerOrd[level - MIN_LEVEL];
    }
    
    private HashMap<String, GeopediaAbstractSpec> layers = new HashMap<String, GeopediaAbstractSpec>();
    private Identifier id=new Identifier(Identifier.ROOT,"geopedia");
    private GeopediaLayersSource() {
    }
    
    @Override
	public String getLocalID() {
    	return id.getLocalID();
    }
    
    @Override
	public Identifier getQualifiedID() {
    	return id;
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T extends LayersSource> void asyncInitialize(AsyncCallback<? super T> cb) {
		cb.onSuccess((T)this);
	}

    @Override
	public GeopediaLayer createLayer(LayerSpec spec) {
        if (!(spec instanceof GeopediaAbstractSpec)) {
            throw new IllegalArgumentException("Spec not a Geopedia spec");
        }
        return new GeopediaLayer((GeopediaAbstractSpec) spec);
    }
    public GeopediaLayer createLayer(String name) {
        return createLayer(findLayerSpecByTitle(name));
    }
    
    @Override
	public LayerSpec findLayerSpec(String layerId, boolean ignoreCase) {
    	for (Iterator<?> it = layers.entrySet().iterator(); it.hasNext();) {
			LayerSpec spc = (LayerSpec) it.next();
			if (layerId.equalsIgnoreCase(spc.getLocalID())) return spc;
		}
        return null;
    }
    
    public LayerSpec findLayerSpecByTitle(String title) {
        return layers.get(title.toLowerCase());
    }
    
    @Override
	public String getTypeIdentifier() {
        return SOURCE_TYPE_GP;
    }

    private void initialize() {
        registerLayer(new GeopediaStaticSpec(NAME_ST_DOF, "/pre/dof", 18));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DOF_DMV, "/pre/dof+dmv", 18));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DMV, "/pre/dmv", 18));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DMV_COLOR, "/pre/dmc+dmv", 18));
        registerLayer(new GeopediaStaticSpec(NAME_ST_TK, "/pre/dtk", 18));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DTK, "/pre/pzs/merged", 16));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DTK_25, "/pre/pzs/25K_jpg_final", 16));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DTK_50, "/pre/pzs/50K_jpg_final", 15));
        registerLayer(new GeopediaStaticSpec(NAME_ST_DTK_250, "/pre/pzs/250K_jpg_final", 13));

		registerLayer(new GeopediaStaticSpec(NAME_GURS_DTK_50, "/pre/dtk50", "http://dmv01.geopedia.si/pre/dtk50",
			"png", 15));
		registerLayer(new GeopediaStaticSpec(NAME_GURS_DPK_250, "/pre/dpk250", "http://dmv01.geopedia.si/pre/dpk250",
			"png", 13));

        registerLayer(new GeopediaLayerSpec(NAME_LY_HS, 450, new String[]{"t297@0.1365"}));
        registerLayer(new GeopediaLayerSpec(NAME_LY_NASELJA, 410, new String[]{"t297@0.1363"}));
        registerLayer(new GeopediaLayerSpec(NAME_LY_KATOBC, 253, new String[]{"t297@0.1364"}));
        registerLayer(new GeopediaLayerSpec(NAME_LY_OBCINE, 408, new String[]{"t297@0.1362"}));
        
        registerLayer(new GeopediaLayerSpec(NAME_LY_REZI25, 1173, new String[]{"t297@0.1366"}));
    }

    @Override
	public boolean isInitialized() {
        return true;
    }
    private void registerLayer(GeopediaAbstractSpec spec) {
        String nameLC = spec.getTitle().toLowerCase();
        layers.put(nameLC, spec);
    }
    
    @Override
	public boolean supports(String capability) {
        return false;
    }
}
