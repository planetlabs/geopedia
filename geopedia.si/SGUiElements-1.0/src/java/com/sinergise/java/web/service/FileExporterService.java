package com.sinergise.java.web.service;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.common.web.service.FileExporterResponse;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.web.ServletUtil;

public class FileExporterService extends SGRemoteServiceServlet {
	private static final long serialVersionUID = -9088401792075598691L;
	
	public static final String REQ_PARAM_FILE="path";
	public static final String REQ_PARAM_FILENAME="filename";
	public static final String REQ_PARAM_MIMETYPE="mimetype";
	
	
	protected static File tempExportFolder = FileUtilJava.createTempDirectory();
	
	protected File beginExport(MimeType exportType, String filename) throws IOException{
		String exportFileBase = FileUtil.getNameNoSuffix(filename);
		
		File exportFile = File.createTempFile(exportFileBase, "."+exportType.getDefaultFileExtension(), tempExportFolder);
		exportFile.deleteOnExit();
		
		return exportFile;
	}
	
	protected FileExporterResponse finishExport(File file, MimeType exportType){
		FileExporterResponse resp = new FileExporterResponse();
		finishExport(resp, file, exportType);
		return resp;
	}
	
	protected <T extends FileExporterResponse> T finishExport(T resp, File file, MimeType exportType) {
		resp.setQueryString(REQ_PARAM_FILE+"="+file.getName()+"&"+ REQ_PARAM_MIMETYPE+"="+exportType.createContentTypeString());
		return resp;
	}
	
	protected <T extends FileExporterResponse> T finishExport(T resp, File file, MimeType exportType, String filename) {
		finishExport(resp, file, exportType);
		resp.setQueryString(resp.getQueryString() + "&"+REQ_PARAM_FILENAME+"="+filename);
		return resp;
	}
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String filename = req.getParameter(REQ_PARAM_FILE);
		String mimeType = req.getParameter(REQ_PARAM_MIMETYPE);
		File fileToDownload = new File(tempExportFolder, filename);
		resp.setContentType(mimeType);
		ServletUtil.setAttachedFileName(resp, getFileName(req));
		FileUtilJava.copyFile(fileToDownload, resp.getOutputStream());
		//this doesn't guarantee the actual deletion of the file... the method may return false
		//nonetheless this file should be the same as the created above exportFile.deleteOnExit()
		fileToDownload.delete();
	}

	protected String getFileName(HttpServletRequest req) {
		String filename = req.getParameter(REQ_PARAM_FILENAME);
		if (isNullOrEmpty(filename)) {
			filename = req.getParameter(REQ_PARAM_FILE);
		}
		return filename;
	}
}
