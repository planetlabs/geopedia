package com.sinergise.geopedia.server;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.core.common.TileUtil;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.db.DBUtil;

public abstract class AbstractWorkload {
	public static class WorkloadLayer {
		public int themeId;
		public int ttlId;
		public int tableId;
		public boolean hasTheme;
		public long themeTime;
		public long tableTime;
		public int drawOnOff;
	}
	
	public int tileLevel;
	public double wMinX, wMaxX, wMinY, wMaxY;
	public double pixSize;
	public WorkloadLayer[] layers;
	
    public int w=Globals.TILESIZE;
    public int h=Globals.TILESIZE;
	
	public int[] baseLayers;
	public boolean addWatermark = false;
	public boolean addScale = false;
    public boolean opaque=false;
    public String mime;
    public boolean filterByScale=true;
    
    
    public void initialize(String layers, int scale, long cx, long cy, int w, int h, TiledCRS tiledCRS) {
		initializeWindow(scale, cx, cy, w, h, tiledCRS);
        this.parseLayers(layers);
    }
    
    public void initializeWindow(int scale, long cx, long cy, int w, int h, TiledCRS tiledCRS) {
		this.tileLevel=scale;
        this.pixSize = tiledCRS.zoomLevels.worldPerPix(scale);
        this.wMinX = cx - 0.5*(this.pixSize*w);
        this.wMinY = cy - 0.5*(this.pixSize*h);
        this.wMaxX = this.wMinX + this.pixSize * w;
        this.wMaxY = this.wMinY + this.pixSize * h;
        this.w = w;
        this.h = h;
    }
    
    public void initializeWindow(Envelope worldMBR, int w, int h, TiledCRS tiledCRS) {
    	initializeWindow(worldMBR, w, h, tiledCRS, Integer.MAX_VALUE);
    }
    
    public void initializeWindow(Envelope worldMBR, int w, int h, TiledCRS tiledCRS, int maximumTileLevel) {
        this.w = w;
        this.h = h;

        double wPerPx = Math.max(worldMBR.getWidth() / w, worldMBR.getHeight() / h);
    	this.tileLevel = tiledCRS.zoomLevels.lastGreaterOrEqualPix(wPerPx);
    	
    	if (this.tileLevel > maximumTileLevel) {
    		this.tileLevel = maximumTileLevel;
    	}
    	
        this.pixSize = tiledCRS.zoomLevels.worldPerPix(tileLevel);
        this.wMinX = worldMBR.getCenterX() - 0.5*(this.pixSize*w);
        this.wMinY = worldMBR.getCenterY() - 0.5*(this.pixSize*h);
        this.wMaxX = this.wMinX + this.pixSize * w;
        this.wMaxY = this.wMinY + this.pixSize * h;
    }
    
   public AbstractWorkload(DimI tileSize) {
	   h = tileSize.h();
	   w = tileSize.w();
   }

   public AbstractWorkload(String path, TiledCRS tiledCRS) {
    	String[] tmp = path.split("[/]");
		
		String tileID = tmp[1];
		String layers = tmp[2];
		int tileLevel =TileUtil.zoomLevelFromTileLevelChar(tileID.charAt(1),tiledCRS);
		DimI tileSize = tiledCRS.tileSizeInPix(tileLevel);		
		h = tileSize.h();
		w = tileSize.w();
		
		int ordSize = TileUtilGWT.numCharsPerOrdinateForLevel(tileLevel, tiledCRS);

		int tileX = DBUtil.parseHexInt(path, 3, 3 + ordSize);
        int tileY = DBUtil.parseHexInt(path, 3 + ordSize, 3 + ordSize + ordSize);

        setWindowForTile(tileLevel, tileX, tileY, tiledCRS);
        
		parseLayers(layers);
    }
   
    public void setWindowForTile(int tileLevel, int tileX, int tileY, TiledCRS tiledCRS) {
	    this.tileLevel = tileLevel;
	    pixSize = tiledCRS.zoomLevels.worldPerPix(tileLevel);
	    wMinX = tiledCRS.tileLeft(tileLevel, tileX);
	    wMinY = tiledCRS.tileBottom(tileLevel, tileY);
	    wMaxX = tiledCRS.tileRight(tileLevel, tileX);
	    wMaxY = tiledCRS.tileTop(tileLevel, tileY);
	}
    
    
    protected abstract void parseLayers(String layers);


	public boolean filterByScale() {
		return filterByScale;
	}
	
}
