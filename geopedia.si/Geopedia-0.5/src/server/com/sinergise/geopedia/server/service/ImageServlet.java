package com.sinergise.geopedia.server.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.UserMgmt;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.Image;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.rendering.GraphicsUtils;
import com.sinergise.geopedia.server.PediaServlet;
import com.sinergise.geopedia.server.Request;
import com.sinergise.geopedia.server.UploadedFile;
import com.sinergise.util.MD5;

public class ImageServlet extends PediaServlet {
	private static final String[] allowedMimes = { "image/png", "image/jpeg", "image/pjpeg", };
	
	private static final int MAX_IMAGE_LENGTH = 512 * 1024;
	private static final int MAX_WIDTH = 1600;
	private static final int MAX_HEIGHT = 1200;

	public static final String GET_REQ = "/get/";

	public static final String UPLOAD_REQ = "/upload";

	@Override
	protected boolean executePedia(Request req) throws IOException {
		
		if (req.method == Request.M_POST && req.getPathInfo().contains(UPLOAD_REQ)) {
			return uploadImage(req);
		} else if (req.method == Request.M_GET && req.getPathInfo().contains(GET_REQ)) {
			return doFetch(req);
		} else {
			return req.sendError404();
		}
	}

	protected boolean doFetch(Request req) throws IOException {
		Session sess = req.getSession();
		if (sess == null) {
			req.res.sendError(HttpServletResponse.SC_FORBIDDEN, "Seja ni vzpostavljena");
			return true;
		}
		ServerInstance instance = null;
		try {
			instance = ServerInstance.getInstance(sess.getInstanceId());
		} catch (GeopediaException ex) {
			return req.sendError404();
		}
		int img_id;
		try {
			img_id = Integer.parseInt(req.getPathInfo().substring(GET_REQ.length()));
		} catch (NumberFormatException e) {
			return req.sendError404();
		}

		if (img_id < 1)
			return req.sendError404();

		Image image;
		try {
			image = instance.getDB().getImage(img_id, true);
		} catch (SQLException e) {
			req.res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			req.echo("Napaka: " + e.getMessage());
			return false;
		}

		int reqW = req.getInt(Request.GET, "w", 0);
		int reqH = req.getInt(Request.GET, "h", 0);

		if (reqW < 4 || reqW > image.width)
			reqW = image.width;
		if (reqH < 4 || reqH > image.height)
			reqH = image.height;

		int w = Math.min(reqW, image.width);
		int h = (int) Math.round((double) image.height * w / image.width);
		if (h > image.height || h > reqH) {
			h = Math.min(reqH, image.height);
			w = (int) Math.round((double) image.width * h / image.height);
		}

		if (w == image.width && h == image.height) {
			req.res.setContentLength(image.data.length);
			req.setContentType(image.mime);
			req.getOutputStream().write(image.data);
			return false;
		}

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);

		BufferedImage orig = ImageIO.read(new ByteArrayInputStream(image.data));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawRenderedImage(orig, AffineTransform.getScaleInstance((double) w / image.width, (double) h / image.height));
		req.setContentType("image/jpeg");

		byte[] data = GraphicsUtils.jpegOf(img, 90);
		req.setContentLength(data.length);
		req.getOutputStream().write(data);

		return true;
	}

	protected boolean uploadImage(Request req) throws IOException {
		req.setContentType("text/html");

		Session sess = req.getSession();
		int user_id;
		try {
			user_id = UserMgmt.requireUserId(sess);
		} catch (UpdateException e) {
			return req.echo("Niste prijavljeni (SID: "+sess.getSessionHeaderValue()+")");
		}
		ServerInstance instance = null;
		try {
		 instance = ServerInstance.getInstance(sess.getInstanceId());
		} catch (GeopediaException ex) {
			return req.echo("invalid instance ID!");
		}

		UploadedFile[] files = req.getAllFiles();
		if (files == null || files.length == 0 || files[0] == null)
			return req.echo("Ni datoteke");

		if (files.length > 1)
			return req.echo("Preveč datotek");

		UploadedFile file = files[0];
		String mime = null;
		for (String tmp : allowedMimes)
			if (tmp.equalsIgnoreCase(file.contentType))
				mime = tmp;

		if (mime == null)
			return req.echo("Dovoljene so samo slike tipov JPEG in PNG.<br>(tip slike: " + file.contentType + ")");

		if (file.tempFile == null)
			return req.echo("Ni vsebine");

		if (file.tempFile.length() > MAX_IMAGE_LENGTH)
			return req.echo("Dolžina datoteke je omejena na " + (MAX_IMAGE_LENGTH / 1024) + "k");

		int width, height;
		try {
			BufferedImage img = ImageIO.read(file.tempFile);
			width = img.getWidth();
			height = img.getHeight();
		} catch (Exception e) {
			return req.echo("Nepravilen zapis datoteke");
		}

		if (width < 1 || width > MAX_WIDTH || height < 1 || height > MAX_HEIGHT)
			return req.echo("Slike so lahko velike največ " + MAX_WIDTH + " x " + MAX_HEIGHT + " pikslov");

		int len = (int) file.tempFile.length();
		byte[] bytes = new byte[len];
		try {
			RandomAccessFile raf = new RandomAccessFile(file.tempFile, "r");
			try {
				raf.readFully(bytes);
			} finally {
				try {
					raf.close();
				} catch (IOException ignored) {
					// what can i do?
				}
			}
		} catch (IOException e) {
			return req.echo("Prišlo je do napake pri branju: " + e.getMessage());
		}

		String md5 = MD5.hash32(bytes);

		int blob_id;
		try {
			blob_id = instance.getDB().createImage(user_id, mime, width, height, bytes, md5);
		} catch (SQLException e) {
			return req.echo("Prišlo je do napake pri vnosu v bazo: " + e.getMessage());
		}
		if (blob_id > 0)
			return req.echo("OK:" + blob_id);
		else
			return req.echo("Neznana napaka");
	}

	/**
	 * @see com.sinergise.geopedia.server.EasyServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(Request.M_POST, req, resp, MAX_IMAGE_LENGTH);
	}

	/**
	 * @see com.sinergise.geopedia.server.EasyServlet#doIt(int, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	private void doIt(int reqType, HttpServletRequest req, HttpServletResponse resp, int maxSize) throws ServletException, IOException {
		if (isMethodSupported(reqType)) {
			doRequest(new Request(reqType, req, resp, maxSize));
		} else {
			fail(req, resp, "Method " + methodName(reqType) + " not supported");
		}
	}
}
