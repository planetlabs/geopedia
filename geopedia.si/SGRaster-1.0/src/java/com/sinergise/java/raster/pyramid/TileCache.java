/*
 *
 */
package com.sinergise.java.raster.pyramid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;



public class TileCache implements Closeable {
    private ArrayList<SoftReference<BufferedImage>> cache=new ArrayList<SoftReference<BufferedImage>>();
    private ArrayList<SoftReference<BufferedImage>> created=new ArrayList<SoftReference<BufferedImage>>();
    
    private int w;
    private int h;
    private int type;
    private Color bg;
    
    /**
     * @param w
     * @param h
     * @param type
     * @param bg
     */
    public TileCache(int w, int h, int type, int bg) {
        super();
        this.w = w;
        this.h = h;
        this.type = type;
        this.bg = new Color(bg, true);
    }
    
    public BufferedImage getTile() {
    	synchronized (cache) {
    		while (!cache.isEmpty()) {
    			BufferedImage ret=cache.remove(0).get();
    			if (ret!=null) return clear(ret);
    		}
    	}
        return createTile();
    }
    
    private BufferedImage clear(BufferedImage in) {
        Graphics2D gr=in.createGraphics();
        gr.setBackground(bg);
        gr.clearRect(0, 0, w, h);
        return in;
    }
    
    public BufferedImage createTile() {
    	BufferedImage ret = clear(createCompatible(w, h));
    	//TODO: Remove cache altogether and rename this to TileFactory
    	// or make createTile() private and make sure the cache works properly
//    	created.add(new SoftReference<BufferedImage>(ret));
    	return ret;
    }
    
    protected boolean isMine(BufferedImage tile) {
    	for (Iterator<SoftReference<BufferedImage>> it = created.iterator(); it.hasNext();) {
    		BufferedImage bi = it.next().get();
    		if (bi==null) it.remove();
				if (bi==tile) return true;
			}
    	return false;
    }

    /**
     * @param tile
     */
    public void releaseTile(BufferedImage tile) {
    	synchronized (cache) {
    		if (isMine(tile)) {
    			cache.add(new SoftReference<BufferedImage>(tile));
    		}
    	}
    }
    
    public int getImageType() {
        return type;
    }

	public BufferedImage createCompatible(int newW, int newH) {
		return new BufferedImage(newW, newH, type);
	}
	
	@Override
	public void close() {
		if (cache != null) {
			cache.clear();
		}
		if (created != null) {
			created.clear();
		}
	}
}
