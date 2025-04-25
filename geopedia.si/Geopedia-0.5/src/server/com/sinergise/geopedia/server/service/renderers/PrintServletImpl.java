package com.sinergise.geopedia.server.service.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.config.ServerConfiguration;
import com.sinergise.geopedia.core.constants.MimeType;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.rendering.GraphicsUtils;
import com.sinergise.geopedia.server.HighlightWorkload;
import com.sinergise.geopedia.server.PediaServlet;
import com.sinergise.geopedia.server.RenderWorkload;
import com.sinergise.geopedia.server.Request;

public class PrintServletImpl extends PediaServlet {
	private static final long serialVersionUID = -3986692399664217489L;
	
	private static final Logger logger = LoggerFactory.getLogger(PrintServletImpl.class);
	
	private static BufferedImage watermarkImage = null;
	@Override
	protected boolean executePedia(Request req) throws ServletException, IOException, GeopediaException {
		Session session = req.getSession();
		return renderPrintResult(session, req);
	}
	
	protected boolean renderPrintResult(Session session, Request req) throws ServletException, IOException, GeopediaException {

		if (session == null)
			return req.sendError404();
		ServerInstance instance = session.getServerInstance();
		TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();

		int scale = req.getInt(Request.M_GET, "s", Integer.MIN_VALUE);
		if (scale == Integer.MIN_VALUE)
			return false;

		float quality = req.getFloat(Request.M_GET, "quality", 1);		
		String type = req.get(Request.M_GET, "type");
		if (StringUtil.isNullOrEmpty(type)){
			 type = MimeType.MIME_IMAGE_JPG;
		}
		
		long cX = req.getLong(Request.M_GET, "x", Long.MIN_VALUE);
		long cY = req.getLong(Request.M_GET, "y", Long.MIN_VALUE);
		if (cX == Long.MIN_VALUE || cY == Long.MIN_VALUE) {
			return false;
		}
		DimI tileSize = tiledCRS.tileSizeInPix(scale);
		int w = req.getInt(Request.M_GET, "w", tileSize.w());
		int h = req.getInt(Request.M_GET, "h", tileSize.h());
		String layersDraw = req.get(Request.M_GET, "drw");
		String hltDraw = req.get(Request.M_GET, "hlt");
		boolean highlight=false;
		if (!StringUtil.isNullOrEmpty(hltDraw))
			highlight=true;
		RenderWorkload work = RenderWorkload.parsePath(layersDraw, scale, cX, cY, w, h, tiledCRS);
		work.opaque=true;
		if (!highlight) {
			work.addScale=true;
			work.addWatermark=true;
		}
		
		int baseLayerId = req.getInt(Request.M_GET, "b", Integer.MIN_VALUE);
		if (baseLayerId != Integer.MIN_VALUE) {
			work.baseLayers = new int[] { baseLayerId };
		}

		BufferedImage img = GraphicsUtils.initAndWrap(GraphicsUtils.allocImage(work.w, work.h), work.w, work.h, work.opaque);
		WorkloadRenderer renderer = new WorkloadRenderer(work, session, instance);
		if (!renderer.go(img)) {
			return req.sendError404();
		}
		HighlightWorkload hlWork = new HighlightWorkload(w, h);
		if (highlight) {
			hlWork.addScale=true;
			hlWork.addWatermark=true;
			hlWork.initialize(hltDraw, scale, cX, cY, w, h, tiledCRS);
			renderer = new WorkloadHighlightRenderer(hlWork, session, instance);
			if (!renderer.go(img)) {
				return req.sendError404();
			}
		}
		
		boolean doWatermark = req.get(Request.M_GET, "nowmi") == null;
		if(doWatermark){
			addWatermark(img, instance);
			addScale(img, scale);
		}
		
		RenderUtils.sendImage(img, req, true,type,quality);
		return true;
	}

	public static void addWatermark(BufferedImage img, ServerInstance instance) {
		Graphics2D g2d = (Graphics2D) img.getGraphics();
		BufferedImage wmi = getWaterMarkImage(instance.getServerConfiguration());
		if(wmi != null) {
			int x = 10;
			int y = img.getHeight()	- 10 - wmi.getHeight();		
			g2d.drawImage(wmi, x, y, null);
		}
	}
	
	private static BufferedImage getWaterMarkImage(ServerConfiguration serverConfiguration) {
		if(watermarkImage == null) {
			String wmiPath = serverConfiguration.getStringProperty(ServerConfiguration.PROP_COPYRIGHT_WATERMARK);
			try {
				watermarkImage = ImageIO.read(PrintServletImpl.class.getClassLoader().getResourceAsStream(wmiPath));
			} catch (Exception e) {
				logger.error("getWatermarkImage exception: ",e);
			}
		}
		return watermarkImage;
	}
	
	public static void addScale(BufferedImage mainImg, int scale) {
		Graphics2D mainG = mainImg.createGraphics();
		 int[] sizes=new int[] {
		            76, 76, 92, 92, 
		            122, 122, 98, 
		            98, 98, 78, 
		            78, 78, 94, 94, 
		            125, 125, 100, 
		            100, 100, 100, 
		            80, 80, 96, 96, 
		            128, 128, 102, 
		            102, 102, 102, 
		            82};
		    String[] texts=new String[] {
		            "10.000 km","5000 km","3000 km","1500 km",
		            "1000 km","500 km", "200 km", 
		            "100 km", "50 km", "20 km", 
		            "10 km", "5 km", "3 km", "1,5 km", 
		            "1 km", "500 m", "200 m", 
		            "100 m", "50 m", "25 m", 
		            "10 m", "5 m", "3 m", "1,5 m",
		            "1 m", "50 cm", "20 cm",
		            "10 cm", "5 cm", "2,5 cm",
		            "1 cm"
		            };
		   if (scale<0 || scale >= sizes.length) 
			   return;
		   
		//TODO externalize?
		int marginRight = 10;
		int marginBottom = 10;
		FontMetrics fm = mainG.getFontMetrics();
		int textWidth = fm.stringWidth(texts[scale]);
		int textHeight = fm.getHeight();

		int height = textHeight+6;
		int width = sizes[scale];
		int x = mainImg.getWidth()-width-marginRight; 
		int y = mainImg.getHeight()-marginBottom-height;	
		
		Color oldColor = mainG.getColor();
		Stroke oldStroke = mainG.getStroke();
		mainG.setColor(Color.WHITE);
		mainG.fillRect(x, y, width, height);
		mainG.setColor(Color.BLACK);
		mainG.drawLine(x, y, x, y+height);
		mainG.drawLine(x+width, y, x+width, y+height);
		
		mainG.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
		mainG.drawLine(x, y, x+width, y);
		mainG.drawLine(x, y+height, x+width, y+height);
		
		mainG.drawString(texts[scale], x+(width-textWidth)/2, y+height-3);
		mainG.setColor(oldColor);
		mainG.setStroke(oldStroke);
		
	}

}
