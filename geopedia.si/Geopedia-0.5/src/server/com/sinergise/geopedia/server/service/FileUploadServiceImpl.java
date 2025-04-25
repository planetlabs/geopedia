package com.sinergise.geopedia.server.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.web.upload.UploadItemServlet;

public class FileUploadServiceImpl extends UploadItemServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8406898592151399089L;

	public static FileUploadServiceImpl INSTANCE = null;

	public FileUploadServiceImpl() {
		if (INSTANCE != null) {
			throw new RuntimeException("Only single instance of "
					+ FileUploadServiceImpl.class + " is allowed!");
		}
		INSTANCE = this;
	}

	public BufferedImage getSizedImage(String token, Integer reqW, Integer reqH)
			throws IOException {
		ServletUploadedFiles items = getUploadedFiles(token);
		FileItem fileItem = items.getSingleFileItem();
		if (fileItem == null)
			return null;
		BufferedImage img = ImageIO.read(fileItem.getInputStream());
		if (reqW == null || reqW < 4 || reqW > img.getWidth())
			reqW = img.getWidth();
		if (reqH == null || reqH < 4 || reqH > img.getHeight())
			reqH = img.getHeight();

		int w = Math.min(reqW, img.getWidth());
		int h = (int) Math.round((double) img.getHeight() * w / img.getWidth());
		if (h > img.getHeight() || h > reqH) {
			h = Math.min(reqH, img.getHeight());
			w = (int) Math.round((double) img.getWidth() * h / img.getHeight());
		}
		BufferedImage imgToSend = img;
		if (w != img.getWidth() || h != img.getHeight()) {
			imgToSend = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = imgToSend.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, w, h);

			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawRenderedImage(
					img,
					AffineTransform.getScaleInstance(
							(double) w / img.getWidth(),
							(double) h / img.getHeight()));
		}
		return imgToSend;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String cmd = req.getParameter("cmd");
		if ("getFile".equals(cmd)) {
			String token = req.getParameter("token");
			Integer reqW = null;
			Integer reqH = null;
			String strH = req.getParameter("h");
			if (strH != null) {
				reqH = Integer.parseInt(strH);
			}
			String strW = req.getParameter("w");
			if (strW != null) {
				reqW = Integer.parseInt(strW);
			}
			BufferedImage imgToSend = getSizedImage(token, reqW, reqH);
			if (imgToSend == null)
				return;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
			ImageIO.write(imgToSend, "jpg", ios);
			resp.setContentLength(baos.size());
			resp.setContentType(MimeType.MIME_IMAGE_JPG.toString());
			resp.getOutputStream().write(baos.toByteArray());
			return;
		}
		super.doGet(req, resp);

	}
}
