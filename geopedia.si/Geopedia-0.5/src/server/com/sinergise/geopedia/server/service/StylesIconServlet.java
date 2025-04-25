package com.sinergise.geopedia.server.service;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import com.sinergise.geopedia.core.constants.Icons;
import com.sinergise.geopedia.core.constants.MimeType;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.rendering.GraphicsUtils;
import com.sinergise.geopedia.rendering.SymDraw;
import com.sinergise.geopedia.server.PediaServlet;
import com.sinergise.geopedia.server.Request;
import com.sinergise.geopedia.server.service.renderers.RenderUtils;
import com.sinergise.geopedia.style.SymLoader;

public class StylesIconServlet extends PediaServlet
{ 
    protected boolean executePedia(Request req) throws IOException
	{
		String path = req.getPathInfo();
		if (path == null)
			return req.sendError404();

		int color1 = req.getInt(Request.GET, "c1", 0xFF000000);
		int color2 = req.getInt(Request.GET, "c2", 0xFFFFFFFF);
		int bgColor = req.getInt(Request.GET, "bg", 0);

		int fSize = req.getInt(Request.GET, "fs", 16);
		float lSize = req.getFloat(Request.GET, "ls", 3);
//		int sSize = req.getInt(Request.GET, "ss", Math.min(SYM_ICON_HEIGHT, SYM_ICON_WIDTH) - 1);
//		int sSize = req.getInt(Request.GET, "ss", Math.min(SYM_ICON_HEIGHT, SYM_ICON_WIDTH) - 2); // TODO:drejmar
		int sSize = req.getInt(Request.GET, "ss", Math.min(Icons.Sizes.SYM_ICON_HEIGHT, Icons.Sizes.SYM_ICON_WIDTH));
		
		
		boolean bold = req.getInt(Request.GET, "bold", 0) != 0;
		boolean italic = req.getInt(Request.GET, "italic", 0) != 0;

		if (path.matches("^/fill/[0-9]+$")) {
			try {
				//return sendFill(req, Integer.parseInt(path.substring(6)), color1, color2, bgColor);
				return sendSym(req, Integer.parseInt(path.substring(6)), Icons.Sizes.FILL_ICON_WIDTH, color1, color2, bgColor);
			} catch (NumberFormatException e) {
				return req.sendError404();
			}
		} else if (path.matches("^/line/[0-9]+$")) {
			try {
				return sendLine(req, Integer.parseInt(path.substring(6)), lSize, color1, color2, bgColor);
			} catch (NumberFormatException e) {
				return req.sendError404();
			}
		} else if (path.matches("^/sym/[0-9]+$")) {
			try {
				return sendSym(req, Integer.parseInt(path.substring(5)), sSize, color1, color2, bgColor);
			} catch (NumberFormatException e) {
				return req.sendError404();
			}
		} else if (path.matches("^/font/[0-9]+$")) {
			try {
				return sendFont(req, Integer.parseInt(path.substring(6)), fSize, bold, italic, color1,
				                color2, bgColor);
			} catch (NumberFormatException e) {
				return req.sendError404();
			}
		} else {
			return req.sendError404();
		}
	}

	private boolean sendFill(Request req, int i, int fgColor, int bgColor, int imgBg) throws IOException
	{
/*		if (i < FillType.MIN_VALID_ID || i > FillType.MAX_VALID_ID)
			return req.sendError404();

		BufferedImage img = makeImg(Icons.Sizes.FILL_ICON_WIDTH, Icons.Sizes.FILL_ICON_HEIGHT, bgColor);

		if (i == FillType.NONE)
			return sendEmpty(img, req, fgColor);

		Graphics2D g = makeG(img);
		TextureCache cache = new TextureCache();
		g.setPaint(cache.get(bgColor, fgColor, i));
		g.fillRect(0, 0, Icons.Sizes.FILL_ICON_WIDTH, Icons.Sizes.FILL_ICON_HEIGHT);

		return PublicRenderServlet.sendPng(img, req, true);*/
		return false;
	}

	private boolean sendLine(Request req, int lineId, float lineW, int color1, int color2, int imgBg) throws IOException
	{
		if (lineId < LineType.MIN_VALID_ID || lineId > LineType.MAX_VALID_ID)
			return req.sendError404();
		if (lineW < LineType.MIN_WIDTH || lineW > LineType.MAX_WIDTH)
			return req.sendError404();

		BufferedImage img = makeImg(Icons.Sizes.LINE_ICON_WIDTH, Icons.Sizes.LINE_ICON_HEIGHT, imgBg);

		if (lineId == LineType.NONE)
			return sendEmpty(img, req, color1);

		Graphics2D g = makeG(img);

		g.setColor(new Color(color1, true));
	//	g.setStroke(PublicRenderServlet.createStroke(lineId, lineW));
		g.drawLine(5, Icons.Sizes.LINE_ICON_HEIGHT / 2, Icons.Sizes.LINE_ICON_WIDTH - 5, Icons.Sizes.LINE_ICON_HEIGHT / 2);

		return RenderUtils.sendImage(img, req, true, MimeType.MIME_IMAGE_PNG);
	}

	static Font createFont(int fontId, int size, boolean bold, boolean italic)
	{
		switch(fontId) {
		case FontId.DEFAULT:
			return new Font("SansSerif", (bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0), size);
		}
		throw new IllegalStateException();
	}

	private boolean sendFont(Request req, int fontId, int size, boolean bold, boolean italic, int color1,
	                int color2, int imgBg) throws IOException
	{
		if (fontId < FontId.MIN_VALID_ID || fontId > FontId.MAX_VALID_ID)
			return req.sendError404();
		if (size < FontId.MIN_SIZE || size > FontId.MAX_SIZE)
			return req.sendError404();

		BufferedImage img = makeImg(Icons.Sizes.FONT_ICON_WIDTH, Icons.Sizes.FONT_ICON_HEIGHT, imgBg);

		if (fontId == FontId.NONE)
			return sendEmpty(img, req, color1);

		Graphics2D g = makeG(img);
		g.setColor(new Color(color1, true));
		g.setFont(createFont(fontId, size, bold, italic));
		Rectangle2D rect = g.getFontMetrics().getStringBounds("Tekst", g);
		double px = Icons.Sizes.FONT_ICON_WIDTH * 0.5 - rect.getCenterX();
		double py = Icons.Sizes.FONT_ICON_HEIGHT * 0.5 - rect.getCenterY();
		g.drawString("Tekst", (float) px, (float) py);

		return RenderUtils.sendImage(img, req, true, MimeType.MIME_IMAGE_PNG);
	}

	private boolean sendSym(Request req, int symId, int symSize, int color1, int color2, int imgBg) throws IOException
	{
		if (symId < SymbolId.MIN_VALID_ID) // XXX || symId > SymbolId.MAX_VALID_ID)
			return req.sendError404();
		if (symSize < SymbolId.MIN_SIZE || symSize > SymbolId.MAX_SIZE)
			return req.sendError404();

//		int w = SYM_ICON_WIDTH;
//		int h = SYM_ICON_HEIGHT;
		int w = symSize; // TODO:drejmar
		int h = symSize;

		int[] imgData = mkData(w, h, imgBg);
		BufferedImage img = makeImg(imgData, w, h);

		if (symId == SymbolId.NONE)
			return sendEmpty(img, req, color1);

		SymDraw.draw(imgData, w, h, SymLoader.getSymbolData(symId,symSize), w / 2, h / 2, color1,
		                color2);

		return RenderUtils.sendImage(img, req, true, MimeType.MIME_IMAGE_PNG);
	}

	private int[] mkData(int w, int h, int bgColor)
	{
		int[] out = new int[w * h];
		Arrays.fill(out, bgColor);
		return out;
	}

	private BufferedImage makeImg(int[] data, int w, int h)
	{
		return GraphicsUtils.wrap(data, w, h, false);
	}

	private BufferedImage makeImg(int w, int h, int bgColor)
	{
		return GraphicsUtils.wrap(mkData(w, h, bgColor), w, h, false);
	}

	private boolean sendEmpty(BufferedImage img, Request req, int color1) throws IOException
	{
		Graphics2D g = makeG(img);
		int w = img.getWidth();
		int h = img.getHeight();

		g.setColor(new Color(color1, true));
		g.drawLine(0, 0, w - 1, h - 1);
		g.drawLine(0, h - 1, w - 1, 0);

		return RenderUtils.sendImage(img, req, true, MimeType.MIME_IMAGE_PNG);
	}

	private static Graphics2D makeG(BufferedImage img)
	{
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		return g;
	}
}
