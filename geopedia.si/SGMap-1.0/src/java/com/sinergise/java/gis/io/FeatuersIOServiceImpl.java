package com.sinergise.java.gis.io;


import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.common.gis.io.TransformingFeatureWriter;
import com.sinergise.common.gis.io.rpc.ExportFeaturesRequest;
import com.sinergise.common.gis.io.rpc.ExportFeaturesResponse;
import com.sinergise.common.gis.io.rpc.FeaturesIOService;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.web.ServletUtil;
import com.sinergise.java.web.service.FileExporterService;

public class FeatuersIOServiceImpl extends FileExporterService  implements FeaturesIOService {
	
	private static final String PARAM_DEFAULT_CRS = "defaultCRS";
	
	private final Logger logger = LoggerFactory.getLogger(FeatuersIOServiceImpl.class);
	
	private CRS defaultCRS = CRS.D48_GK;
	
	@Override
	public ExportFeaturesResponse exportFeatures(ExportFeaturesRequest request) throws ServiceException {
		if (request.isEmpty()) {
			throw new ServiceException("Nothing to export!");
		}
		
		FeatureWriter featWriter = null;
		try {				
			MimeType exportType = request.getExportType();
			
			File exportFile = beginExport(exportType, request.getFilename());
			
			if (exportType.equals(MimeType.MIME_OPENDOCUMENT_SPREADSHEET)) {
				
				featWriter = new XLSXFeatureWriter(new FileOutputStream(exportFile));
				
			} else if (exportType.equals(MimeType.MIME_CSV)) {
				
				featWriter = new CSVFeatureWriter(new FileWriter(exportFile));
			
			} else if (exportType.equals(MimeType.MIME_GPX)) {
				
				Transform<?, ?> tr = Transforms.find(defaultCRS, CRS.WGS84);
				if (tr == null) {
					throw new ServiceException("Could not find WGS84 transform for CRS: "+defaultCRS);
				}
				featWriter = new TransformingFeatureWriter(new GPX11FeatureWriter(new FileWriter(exportFile)), tr);
				
			} else if (exportType.equals(MimeType.MIME_ZIPPED_ESRI_SHP)) {
				
				featWriter = new ZippedShapefileFeatureWriter(exportFile);
				((ShapefileFeatureWriter)featWriter).initShapeType(request.getFeatures());
				
			} else  {
				throw new ServiceException("Unsupported export type: '"+exportType+"'");
			}
			
			for (CFeature feat : request.getFeatures()) {
				featWriter.append(feat);
			}
			
			return finishExport(new ExportFeaturesResponse(exportFile.getName()),exportFile, exportType);
		} catch (Exception ex) {
			String msg = "Error while exporting: "+ex.getMessage();
			logger.error(msg, ex);
			throw new ServiceException(msg);
		} finally {
			if (featWriter != null) {
				try {
					featWriter.close();
				} catch(IOException ignore) { }
			}
		}
	} 
	
	
	
	@Override
	public void init() throws ServletException {
		super.init();
		UtilJava.initStaticUtils();
		
		//TODO: remove this when proper SRID is set to every geometry
		String defaultCRSStr = ServletUtil.findInitParameter(this, PARAM_DEFAULT_CRS);
		if (!isNullOrEmpty(defaultCRSStr)) {
			CRS crs = CrsRepository.INSTANCE.get(defaultCRSStr);
			if (crs != null) {
				defaultCRS = crs;
			} else {
				logger.warn("Could not find specified defaultCRS: "+defaultCRSStr+", will use CRS: "+defaultCRS);
			}
		}
	}
}
