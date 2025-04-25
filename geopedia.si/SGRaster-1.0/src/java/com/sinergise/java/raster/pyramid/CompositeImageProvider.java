/*
 *
 */
package com.sinergise.java.raster.pyramid;

import java.io.IOException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.java.raster.core.OffsetBufferedImage;


public class CompositeImageProvider implements TileProviderJava {
    private TileProviderJava[] sources;
    private int maxScale;
    private final TiledCRS cs;
    public CompositeImageProvider(TileProviderJava[] sources) {
        this.sources=sources;
        this.cs = sources[0].getTiledCRS();
        
        maxScale=0;
        for (TileProviderJava tp : sources) {
            maxScale=Math.max(maxScale, tp.getMaxLevelId());
            if (!cs.equals(tp.getTiledCRS())) throw new IllegalArgumentException("Incompatible TiledCRSs: "+cs+" and "+tp.getTiledCRS());
        }
    }
    
    @Override
	public TiledCRS getTiledCRS() {
    	return cs;
    }
    
    @Override
	public OffsetBufferedImage getTile(int scale, int x, int y) throws IOException {
    	OffsetBufferedImage out=null;
        int maxOpq = 0;
        for (maxOpq = sources.length-1; maxOpq >= 0; maxOpq--) {
					if (sources[maxOpq].isOpaque(scale, x, y)) break;
				}
        for (int i = maxOpq; i < sources.length; i++) {
        		if (!sources[i].hasTile(scale, x, y)) continue;
        		if (out==null) {
        			out=sources[i].getTile(scale, x, y);
        		} else {
        			sources[i].renderTile(out, scale, x, y);
        		}
        }
        return out;
    }
    @Override
	public boolean renderTile(OffsetBufferedImage tileImg, int scale, int x, int y) throws IOException {
    	boolean ret=false;
      for (int i = 0; i < sources.length; i++) {
        ret |= sources[i].renderTile(tileImg, scale, x, y);
      }
      return ret;
    }
    
    @Override
	public boolean hasData(int scale, int x, int y) throws IOException {
      for (int i = 0; i < sources.length; i++) {
        if (sources[i].hasData(scale, x, y)) {
        	return true;
        }
      }
      return false;
    }
    
    @Override
	public boolean hasTile(int scale, int x, int y) throws IOException {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].hasTile(scale, x, y)) return true;
        }
        return false;
    }
    
    @Override
	public boolean isOpaque(int scale, int x, int y) throws IOException {
    	for (int i = 0; i < sources.length; i++) {
        if (sources[i].isOpaque(scale, x, y)) return true;
      }
      return false;
    }
    
    @Override
	public int getMaxLevelId() {
        return maxScale;
    }
    
    @Override
	public long estimateNumTiles() throws IOException {
    	long ret= 0;
    	for (TileProviderJava src : sources) {
			ret += src.estimateNumTiles();
		}
    	return ret;
    }
}
