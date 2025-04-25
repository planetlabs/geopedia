package com.sinergise.java.raster.ui;

import static com.sinergise.common.util.math.MathUtil.roundToInt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.util.ui.RunnerHelper;


public class RasterGUIUtils {
	static class TestPanel extends JPanel implements MouseMotionListener, MouseListener
	{
		private static final long serialVersionUID = 1L;

		Paint bgPaint;
	    
	    RenderedImage img;
	    int offx=100; 
	    int offy=100;
	    
	    int lastx=0;
	    int lasty=0;
	    
	    public TestPanel(RenderedImage img)
	    {
	        this(img, null);
	    }
        public TestPanel(RenderedImage img, Color bgColor)
        {
	        super();
	        addMouseListener(this);
	        addMouseMotionListener(this);
	        this.img=img;
	        
	        if (bgColor == null) {
	            bgPaint = DEFAULT_GRID;
	        } else {
	            boolean darken = ColorUtil.RGBToGray(bgColor.getRGB()) > 128;
	            bgPaint = createGridPaint(16, 
	                                      bgColor.getRGB(), 
	                                      darken?ColorUtil.colorOver(bgColor.getRGB(),0x40000000)
	                                             :ColorUtil.colorOver(bgColor.getRGB(),0x40ffffff));
	        }
	    }
	    
	    @Override
		public void paintComponent(Graphics g1)
	    {
	        Graphics2D g=(Graphics2D) g1;
	        g.setPaint(bgPaint);
	        g.fillRect(0, 0, getWidth(), getHeight());
	        if (img instanceof BufferedImage) {
	        	BufferedImage bi = (BufferedImage)img;
	        	int rx = -offx;
	        	int ry = -offy;
	        	int w = getWidth();
	        	int h = getHeight();
	        	int x = 0;
	        	int y = 0;
	        	if (rx < 0) {
	        		rx=0;
	        		w -= offx;
	        		x = offx;
	        	}
	        	if (ry < 0) {
	        		ry = 0;
	        		h -= offy;
	        		y = offy;
	        	}
	        	if (w<0) w=0;
	        	if (rx+w > img.getWidth()) w = img.getWidth()-rx;
	        	if (h<0) h=0;
	        	if (ry+h > img.getHeight()) h = img.getHeight()-ry;
	        	if (w > 0 && h > 0) {
	        		g.drawImage(bi.getSubimage(rx, ry, w, h),null,x,y);
	        	}
	        } else {
	        	g.drawRenderedImage(img, AffineTransform.getTranslateInstance(offx, offy));
	        }
	    }
	
	    @Override
		public void mouseDragged(MouseEvent e)
	    {
	        int currx=e.getX();
	        int curry=e.getY();
	        offx+=(currx-lastx);
	        offy+=(curry-lasty);
	        lastx=currx;
	        lasty=curry;
	        repaint();
	    }
	
	    @Override
		public void mousePressed(MouseEvent e)
	    {
	        lastx=e.getX();
	        lasty=e.getY();
	    }
	
	    @Override
		public void mouseMoved(MouseEvent e) {}
	    @Override
		public void mouseClicked(MouseEvent e) {}
	    @Override
		public void mouseEntered(MouseEvent e) {}
	    @Override
		public void mouseExited(MouseEvent e) {}
	    @Override
		public void mouseReleased(MouseEvent e) {}
	}

	public static final Paint DEFAULT_GRID = createGridPaint(16, 0xaaaaaa, 0xeeeeee);
	
	public static Paint createGridPaint(int grid, int c1, int c2)
	{
	    int size=grid>=64?grid:grid*(64/grid);
	    int halfGrid=grid/2;
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		for (int x=0; x<size; x++) {
			for (int y=0; y<size; y++) {
				if ((x%grid >= halfGrid) != (y%grid >= halfGrid)) {
					img.setRGB(x, y, c1);
				} else {
					img.setRGB(x, y, c2);
				}
			}
		}
		
		TexturePaint paint = new TexturePaint(img, new Rectangle2D.Double(0, 0, size, size));
		
		return paint;
	}
	
	public static JFrame showImage(RenderedImage img) {
	    return showImage(img, "");
	}
    public static JFrame showImage(RenderedImage img, String name) {
        return showImage(img, name, Color.WHITE);
    }
    public static JFrame showImage(RenderedImage img, String name, Color bgColor) {
        TestPanel panel=new TestPanel(img, bgColor);
        panel.setName(name);
        return RunnerHelper.runComponent(panel, img.getWidth()+200, img.getHeight()+200);
	}

	public static void showImageScaled(RenderedImage image, double s) {
		if (s == 1) {
			showImage(image);
		} else {
			showImage(RasterUtilJava.stretchRendered(image, roundToInt(image.getWidth()*s), roundToInt(image.getHeight()*s)));
		}
	}
}
