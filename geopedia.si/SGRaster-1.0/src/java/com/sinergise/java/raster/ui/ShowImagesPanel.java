package com.sinergise.java.raster.ui;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class ShowImagesPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<ArrayList<BufferedImage>> images;
	static final Paint grid = createGrid();
	int numX = 0;
	int numY = 0;
	
	public ShowImagesPanel() {
	    Thread t=new Thread(new Runnable() {
            @Override
			public void run() {
                while(true) {
                    try {
                        Thread.sleep(100);
                        repaint();
                    } catch (Exception e) {
                    }
                }
            }
        });
	    t.setDaemon(true);
	    t.setPriority(Thread.NORM_PRIORITY-1);
	    t.start();
    }
	
	public void setImage(int x, int y, BufferedImage img)
	{
		if (images == null)
			images = new ArrayList<ArrayList<BufferedImage>>();
		
		while (images.size() <= y)
			images.add(new ArrayList<BufferedImage>());
		
		ArrayList<BufferedImage> row = images.get(y);
		while (row.size() <= x)
			row.add(null);
		
		row.set(x, img);
	
		if (x >= numX) numX = x + 1;
		if (y >= numY) numY = y + 1;
		
		repaint();
	}
	
	public BufferedImage getImage(int x, int y) {
		return images.get(y).get(x);
	}
	
	static int OFFSET = 3;
	
	@Override
	protected void paintComponent(Graphics g1)
	{
	    super.paintComponent(g1);
	    
	    Graphics2D g = (Graphics2D)g1;
	    g.setPaint(grid);
	    g.fillRect(0, 0, getWidth(), getHeight());
	    
	    int posY = OFFSET;
	    
	    for (int y=0; y<numY; y++) {
	    	int rowHeight = rowHeight(y);
	    	if (rowHeight <= 0)
	    		continue;
	    	
	    	int posX = OFFSET;
	    	for (int x=0; x<numX; x++) {
	    		int colWidth = colWidth(x);
	    		if (colWidth <= 0)
	    			continue;
	    		
	    		if (images.get(y).size() > x) {
	    			BufferedImage img = images.get(y).get(x);
	    			if (img != null) {
	    				g.drawImage(
							img,
							posX + ((colWidth - img.getWidth()) >>> 1),
							posY + ((rowHeight - img.getHeight()) >>> 1),
							null);
	    			}
	    		}
	    		
	    		posX += colWidth + OFFSET;
	    	}
	    	
	    	posY += rowHeight + OFFSET;
	    }
	}
	
	int rowHeight(int y)
	{
		int max = 0;
		for (BufferedImage img : images.get(y))
			if (img != null)
				max = Math.max(max, img.getHeight());
		return max;
	}
	
	int colWidth(int x)
	{
		int max = 0;
		for (int y=0; y<numY; y++) {
			if (images.get(y).size() <= x)
				continue;
			
			BufferedImage img = images.get(y).get(x);
			if (img != null)
				max = Math.max(max, img.getWidth());
		}
		return max;
	}
	
	public static Paint createGrid()
	{
	    int size = 64;
	    int gridSize = 16;
	    int halfGrid=gridSize/2;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		for (int x=0; x<size; x++) {
			for (int y=0; y<size; y++) {
				if ((x%gridSize >= halfGrid) != (y%gridSize >= halfGrid)) {
					img.setRGB(x, y, 0xaaaaaa);
				} else {
					img.setRGB(x, y, 0xeeeeee);
				}
			}
		}
		
		TexturePaint paint = new TexturePaint(img, new Rectangle2D.Double(0, 0, size, size));
		
		return paint;
	}
	
	public static ShowImagesPanel framed(String title)
	{
		ShowImagesPanel sip = new ShowImagesPanel();
		try {
		    showFramed(title, sip);
		} catch (HeadlessException e) {
		    //do nothing
        }
		
		return sip;
	}
	
	public static void showFramed(String title, Container panel)
	{
		JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setContentPane(panel);
		frame.setVisible(true);
	}
}
