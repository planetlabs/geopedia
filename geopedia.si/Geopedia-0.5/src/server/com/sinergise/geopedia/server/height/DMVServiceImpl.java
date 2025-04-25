package com.sinergise.geopedia.server.height;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.service.DMVService;
import com.sinergise.geopedia.core.service.params.GetContour;
import com.sinergise.geopedia.core.service.params.GetDMVRequest;
import com.sinergise.geopedia.core.service.params.GetHeight;
import com.sinergise.geopedia.core.service.result.GetContourResult;
import com.sinergise.geopedia.core.service.result.GetDMVResult;
import com.sinergise.geopedia.core.service.result.GetHeightResult;
import com.sinergise.geopedia.geometry.height.DMVHolder;
import com.sinergise.geopedia.geometry.height.Heights;

public class DMVServiceImpl extends RemoteServiceServlet implements DMVService {
	private static final long serialVersionUID = 3841279547409473005L;

	private static final Logger logger = LoggerFactory.getLogger(DMVServiceImpl.class);

	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if(data == null){
			String dmvFile = config.getInitParameter("dmvFile");
			if(!StringUtil.isNullOrEmpty(dmvFile)){
				double step = Double.parseDouble(config.getInitParameter("step"));
				logger.debug("creating dmvHolder original with:"+step+","+dmvFile);
				try {
					data = DMVHolder.loadRaw(dmvFile, step);
				} catch (Exception e) {
					logger.error("Something happened while loading the dmv file@"+dmvFile, e);
					//i dont throw any exception in this case, so that the application can normally deploy and be used except for this functionality.
				}
				
			} else {
				try {
					ServerInstance si = ServerInstance.getInstance(ServerInstance.INSTANCE_ID_GEOPEDIASI);
					heights = si.getHeights();
					data = si.getHeights().getData();
					
				} catch (GeopediaException e) {
					throw new ServletException(e);
				}
			}
		}
	}

	Heights heights;
	DMVHolder data;

	private static final int minScale = DMVHolder.minScale;
	private static final double txPxSizeAtMinScale = DMVHolder.txPxSizeAtMinScale;
	private static final double dmrStepAtMinScale = DMVHolder.dmrStepAtMinScale;
	
	@Override
	public GetDMVResult getDMV(GetDMVRequest request) {
		logger.debug("getting the dmv with DEMRequest:"+request);
		
		double wCenterX = request.wCenterX;
		double wCenterY = request.wCenterY;
		short scale = request.scale;
		int txSize = request.txSize;
		
		//adapting the steps to new scale
		//one px is eq to txPxSize meters
		double txPxSize = ( scale == minScale ? txPxSizeAtMinScale : txPxSizeAtMinScale / (1 << (scale - minScale)) );//meters per one pixel
		//one sample is eq to dmrStep meters
		double dmrStep = ( scale == minScale ? dmrStepAtMinScale : Math.max(data.step, dmrStepAtMinScale / (1 << (scale - minScale))) );//meters
		
		//TEXTURE		
		double wTxMinX = wCenterX - ((txSize/2)* txPxSize);//((txSize/2)* txPxSize) is the length in raster unit of half the texture (the texture is a square so the calculation is the same for x and y)
		double wTxMinY = wCenterY - ((txSize/2)* txPxSize);
		
		double wTxMaxX = wTxMinX + (txSize * txPxSize);//((txSize)* txPxSize) is the length of the texture (the texture is a square so the calculation is the same for x and y)
		double wTxMaxY = wTxMinY + (txSize * txPxSize);
		
		
		//DMR
		double wDmrMinX = Math.ceil(wTxMinX / dmrStep) * dmrStep; //here we want to assure the size of the dmr will fall into the size of the texture in case the texture size does not return a dmr thats fits
		wTxMinX = wTxMinX < data.minx*data.step ? data.minx*data.step : wTxMinX;
		double wDmrMinY = Math.ceil(wTxMinY / dmrStep) * dmrStep;
		wDmrMinY = wDmrMinY < data.miny*data.step ? data.miny*data.step : wDmrMinY;
		
		double wDmrMaxX = Math.floor(wTxMaxX / dmrStep) * dmrStep; //here we want to assure the size of the dmr will fall into the size of the texture in case the texture size does not return a dmr thats fits
		wDmrMaxX = wDmrMaxX > data.maxx*data.step ? data.maxx*data.step : wDmrMaxX;
		double wDmrMaxY = Math.floor(wTxMaxY / dmrStep) * dmrStep;
		wDmrMaxY = wDmrMaxY > data.maxy*data.step ? data.maxy*data.step : wDmrMaxY;
		
		long copyMinX = Math.round(wDmrMinX / data.step);
		long copyMinY = Math.round(wDmrMinY / data.step);
		int outWSamp = (int) (Math.round((wDmrMaxX - wDmrMinX) / dmrStep) + 1);//(wDmrMaxX - wDmrMinX) / dmrStep is the number of samples in for x. +1 to account for the first sample/point
		int outHSamp = (int) (Math.round((wDmrMaxY - wDmrMinY) / dmrStep) + 1);
		int samples = Math.min(180, Math.min(outWSamp, outHSamp));
		wDmrMaxX = wDmrMinX + (samples * dmrStep);
		wDmrMaxY = wDmrMinY + (samples * dmrStep);
		/*wTxMaxX = wTxMinX + (samples * dmrStep);
		wTxMaxY = wTxMinY + (samples * dmrStep);*/
				
		DMVHolder holder = data.subsample(copyMinX, copyMinY, scale, samples, samples);
		
		GetDMVResult result = new GetDMVResult();
		//the actual step
		result.step = holder.step;
		//the actual heights
		result.data = holder.data;
		//step for the texture (in px/m)
		result.txPxSize = txPxSize;
		//texture coordinates
		result.wTxMinX = wTxMinX;
		result.wTxMinY = wTxMinY;
		result.wTxMaxX = wTxMaxX;
		result.wTxMaxY = wTxMaxY;
		//dmr coordinates (actual envelope)
		result.wDmrMinX = wDmrMinX;
		result.wDmrMinY = wDmrMinY;
		result.wDmrMaxX = wDmrMaxX;
		result.wDmrMaxY = wDmrMaxY;
		
		result.maxZ = holder.maxz;
		result.minZ = holder.minz;
		
		logger.debug("Result was:"+result);
		
		return result;
	}
	
	@Override
	public GetHeightResult getHeight(GetHeight req){
		if(req.point != null){
			HasCoordinate point = req.point;
			double resolution = heights.getData().step;

			double z = heights.calcZ(point.x(), point.y(), resolution);
			logger.info("point coords: " + point.x() + ", " + point.y() + "; height = " + z);
			
			return new GetHeightResult(z);
		}
		return new GetHeightResult(null);
	}
	
	@Override
	public GetContourResult getContour(GetContour req){
		
		DMVHolder dmvHolder = heights.getData();
		
		HasCoordinate point = req.point;
		double envelopeSizeX = req.envelope.getWidth();
		double envelopeSizeY = req.envelope.getHeight();

		double resolution = dmvHolder.step;
		int minX = (int) dmvHolder.minx;
		int minY = (int) dmvHolder.miny;

		//point is in real world coordinates, so translate it to dmv "coordinates" first
		logger.debug("point coords: " + point.x() + ", " + point.y());
		int startX = (int) (point.x()/resolution - minX);
		int startY = (int) (point.y()/resolution - minY);
		Double z = heights.calcZ(point.x(), point.y(), resolution);
		int startZ = z.intValue();
		logger.debug("start X,Y,Z = " + startX + ", " + startY + ", " + startZ); 
		logger.debug("dmvHolder.data[startY][startX] = " + dmvHolder.data[startY][startX]);
		
		int halfEnvelopeSizeX = (int) (envelopeSizeX/resolution/2.);
		int halfEnvelopeSizeY = (int) (envelopeSizeY/resolution/2.);

		DMVHolder subset = dmvHolder.subset(startX+minX-halfEnvelopeSizeX, startX+minX+halfEnvelopeSizeX, startY+minY-halfEnvelopeSizeY, startY+minY+halfEnvelopeSizeY);
		if(subset!=null){
			ContourBuilder cb = new ContourBuilder(subset, halfEnvelopeSizeX, halfEnvelopeSizeY, req.itForSimplification, req.simplificationTolerance);
			cb.contour();
			return new GetContourResult(cb.getMultiLineStringContour(), z);
		}

		return null;
	}
	
}
