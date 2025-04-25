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
import com.sinergise.geopedia.server.PediaServlet;
import com.sinergise.geopedia.server.RenderWorkload;
import com.sinergise.geopedia.server.Request;

public class PublicRenderServletImpl extends PediaServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 895839812812806953L;
	static final Logger logger = LoggerFactory.getLogger(PublicRenderServletImpl.class);

	

	@Override
	protected boolean executePedia(Request req) throws ServletException, IOException, GeopediaException {

		Session session = req.getSession();

		if (session == null) {
			logger.debug("No session!");
			return req.sendError404();
		}
		ServerInstance instance = session.getServerInstance();
		TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();
		// XXX check permissions!
		String path = req.getPathInfo();
		if (path == null) {
			logger.debug("No path info!");
			return req.sendError404();
		}

		RenderWorkload work = null;


		if (!RenderWorkload.isValidRenderPath(instance.getId(), path)) {
			logger.debug("Invalid render path!");
			return req.sendError404();
		}
		work = new RenderWorkload(path, tiledCRS);
		work.opaque = "1".equals(req.get(Request.M_GET, "opaque"));

		work.mime = req.get(Request.GET, "type");
		if (work.mime == null)
			work.mime = MimeType.MIME_IMAGE_PNG;
		if (MimeType.MIME_IMAGE_JPG.equals(work.mime))
			work.opaque = true;

		// check image size limit
		if (!RenderUtils.checkImageSizeLimit(req, work)) {
			return true;
		}

		WorkloadRenderer renderer = new WorkloadRenderer(work, req.getSession(), instance);
		if (!renderer.go()) {
			return req.sendError404();
			// if (!renderer.success) {
			// if (renderer.error == WorkloadRenderer.ERROR_404) {
			// return req.sendError404();
			// }
			// return true;
		}

		RenderUtils.sendImage(renderer.getImage(), req, true, work.mime);

		long endTime = System.currentTimeMillis();

		// logger.info("Query: " + (renderer.queryTime) + "ms   Rendering: " +
		// (renderer.drawTime) + "ms    Encoding: " + (endTime -
		// renderer.doneTime)
		// + "ms");

		return true;
	}

}
