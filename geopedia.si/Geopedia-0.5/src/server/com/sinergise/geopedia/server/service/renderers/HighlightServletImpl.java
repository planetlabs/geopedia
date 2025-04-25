package com.sinergise.geopedia.server.service.renderers;

import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.constants.MimeType;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.server.HighlightWorkload;
import com.sinergise.geopedia.server.PediaServlet;
import com.sinergise.geopedia.server.Request;

public class HighlightServletImpl extends PediaServlet {

	private static final long serialVersionUID = -5668773789351879517L;
	static final Logger logger = LoggerFactory.getLogger(HighlightServletImpl.class);


	
	@Override
	protected boolean executePedia(Request req) throws ServletException, IOException, GeopediaException {
		Session session = req.getSession();
		if (session == null)
			return req.sendError404();
		ServerInstance instance = session.getServerInstance();
		TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();
		
	
		
		String path = req.getPathInfo();
		if (!HighlightWorkload.isValidRenderPath(instance.getId(), path))
			return req.sendError404();

		
		HighlightWorkload work = new HighlightWorkload(path, tiledCRS);
//		boolean opaque = "1".equals(req.get(Request.M_GET, "opaque"));		

		
		WorkloadHighlightRenderer renderer = new WorkloadHighlightRenderer(work, req.getSession(), instance);
		if (!renderer.go()) {
			return req.sendError404();
		}		
		RenderUtils.sendImage(renderer.getImage(), req, true,MimeType.MIME_IMAGE_PNG);
		long doneTime = System.currentTimeMillis();

		long endTime = System.currentTimeMillis();

		logger.info("Encoding: " + (endTime - doneTime) + "ms");

		return true;
	}
}
