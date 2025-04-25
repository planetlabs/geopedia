package com.sinergise.java.geometry.service;

import static com.sinergise.java.geometry.io.jts.JtsConversionUtil.gwtFromJTS;
import static com.sinergise.java.geometry.io.jts.JtsConversionUtil.jtsFromGWT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.common.geometry.service.util.BinaryGeomOpRequest;
import com.sinergise.common.geometry.service.util.GeomOpRequest;
import com.sinergise.common.geometry.service.util.GeomOpResult;
import com.sinergise.common.geometry.service.util.GeomUtilService;
import com.sinergise.common.geometry.service.util.GetGeomBufferRequest;
import com.sinergise.common.util.ServiceException;
import com.sinergise.java.geometry.io.jts.JtsConversionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import com.vividsolutions.jts.operation.overlay.snap.SnapOverlayOp;

@SuppressWarnings("serial")
public class GeomUtilServiceImpl extends RemoteServiceServlet implements GeomUtilService {
	
	private final Logger logger = LoggerFactory.getLogger(GeomUtilServiceImpl.class);

	@Override
	public GeomOpResult getGeomBuffer(GetGeomBufferRequest request) throws ServiceException {
		try {
			GeometryFactory factory = createFactory(request);
			
			Geometry geom = JtsConversionUtil.jtsFromGWT(request.getGeometry(), factory);
			Geometry bufferGeom = geom.buffer(request.getDistance());
			return new GeomOpResult(JtsConversionUtil.gwtFromJTS(bufferGeom));
		} catch(Throwable e) {
			String msg = "Error while calculating buffer: "+e.getMessage();
			logger.error(msg, e);
			throw new ServiceException(msg, e);
		}
	}
	
	@Override
	public GeomOpResult getGeomUnion(BinaryGeomOpRequest request) throws ServiceException {
		try {
			return new GeomOpResult( 
				executeOverlayOp(request, request.getArguments(), OverlayOp.UNION)
			);
		} catch(Throwable e) {
			String msg = "Error while calculating geometry union: "+e.getMessage();
			logger.error(msg, e);
			throw new ServiceException(msg, e);
		}
	}
	
	@Override
	public GeomOpResult getGeomIntersection(BinaryGeomOpRequest request) throws ServiceException {
		try {
			return new GeomOpResult( 
				executeOverlayOp(request, request.getArguments(), OverlayOp.INTERSECTION)
			);
		} catch(Throwable e) {
			String msg = "Error while calculating geometry intersection: "+e.getMessage();
			logger.error(msg, e);
			throw new ServiceException(msg, e);
		}
	}
	
	@Override
	public GeomOpResult getGeomDifference(BinaryGeomOpRequest request) throws ServiceException {
		try {
			return new GeomOpResult( 
				executeOverlayOp(request, request.getArguments(), OverlayOp.DIFFERENCE)
			);
		} catch(Throwable e) {
			String msg = "Error while calculating geometry difference: "+e.getMessage();
			logger.error(msg, e);
			throw new ServiceException(msg, e);
		}
	}
	
	private static com.sinergise.common.geometry.geom.Geometry executeOverlayOp(GeomOpRequest req,
		com.sinergise.common.geometry.geom.Geometry[] args, int opCode) 
	{
		GeometryFactory factory = createFactory(req);
		
		Geometry result = jtsFromGWT(args[0], factory);
		for (int i=1; i<args.length; i++) {
			result = SnapOverlayOp.overlayOp(result, jtsFromGWT(args[i], factory), opCode); 
		}
		
		return gwtFromJTS(result);
	}
	
	private static GeometryFactory createFactory(GeomOpRequest req) {
		double scale = req.getScale();
		if (scale != 0) {
			return new GeometryFactory(new PrecisionModel(scale));
		}
		
		return new GeometryFactory();
	}

}
