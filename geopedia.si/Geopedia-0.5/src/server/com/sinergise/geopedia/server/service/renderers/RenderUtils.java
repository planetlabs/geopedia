package com.sinergise.geopedia.server.service.renderers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.app.Main;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.constants.MimeType;
import com.sinergise.geopedia.rendering.GraphicsUtils;
import com.sinergise.geopedia.server.RenderWorkload;
import com.sinergise.geopedia.server.Request;

public class RenderUtils {
	private static final Logger logger = LoggerFactory.getLogger(RenderUtils.class);

	public static boolean sendImage(BufferedImage img, Request req, boolean shouldCache, String mime) 
			throws IOException {
		return sendImage(img, req, shouldCache, mime, 1);
	}
	public static boolean sendImage(BufferedImage img, Request req, boolean shouldCache, String mime, float quality)
			throws IOException {
		if (shouldCache) {
			long time = System.currentTimeMillis();

			req.res.setHeader("X-Served-By", Main.serverName);
			req.res.setDateHeader("Date", time);
			req.res.setDateHeader("Expires", time + 30L * 24 * 3600 * 1000); // after
																				// 30
																				// days
			req.res.setDateHeader("Last-Modified", time);
			req.res.setHeader("Cache-Control", "max-age=2592000");
		}

		req.res.setHeader("Access-Control-Allow-Origin", "*");
		req.setContentType(mime);
		MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(req.getOutputStream());
		try {

			Iterator<ImageWriter> iwIterator = ImageIO.getImageWritersByFormatName(MimeType.imageIOType(mime));
			if (iwIterator == null || !iwIterator.hasNext()) {
				logger.error(String.format("Failed to get ImageWriter for mime-type '%s'", mime));
				return false;
			}
			ImageWriter writer = iwIterator.next();
			try {
				ImageWriteParam iwp = writer.getDefaultWriteParam();
				if (quality != 1) {
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality(quality);
				}
				writer.setOutput(mcios);
				writer.write(null, new IIOImage(img, null, null), iwp);
				//req.res.setHeader("Content-Length", Long.toString(mcios.getStreamPosition()));
			} finally {
				writer.dispose();
			}
		} finally {
			mcios.close();
		}
		return true;
	}

	public static boolean checkImageSizeLimit(Request req, RenderWorkload work) throws IOException {
		if (work.w > Globals.MAX_IMAGE_WIDTH || work.h > Globals.MAX_IMAGE_HEIGHT) {
			work.w = 350;
			work.h = 200;
			BufferedImage errorImg = GraphicsUtils.initAndWrap(GraphicsUtils.allocImage(work.w, work.h), work.w,
					work.h, work.opaque);
			Graphics2D errorG = errorImg.createGraphics();
			errorG.setColor(Color.RED);
			errorG.drawString("Tako velike slike zal ni mozno prikazati.", 10, 75);
			errorG.drawString("Za vec informacij kontaktirajte info@geopedia.si.", 10, 100);
			sendImage(errorImg, req, true, work.mime);
			return false;
		}
		return true;
	}
}
