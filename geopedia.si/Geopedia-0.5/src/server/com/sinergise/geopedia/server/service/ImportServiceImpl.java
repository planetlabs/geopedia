package com.sinergise.geopedia.server.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;

import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.exceptions.ImportExportException;
import com.sinergise.geopedia.core.service.ImportService;
import com.sinergise.geopedia.core.service.params.ImportSettings;
import com.sinergise.geopedia.core.service.params.ImportStatus;
import com.sinergise.geopedia.core.service.params.TaskStatus.Status;
import com.sinergise.geopedia.server.PediaRemoteServiceServlet;
import com.sinergise.geopedia.server.SessionLifetimeTasks;
import com.sinergise.geopedia.server.service.importer.ImportFeaturesTask;
import com.sinergise.geopedia.server.service.importer.ShapefileReader;
import com.sinergise.java.util.format.JavaFormatProvider;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.web.upload.UploadItemServlet.ServletUploadedFiles;

public class ImportServiceImpl  extends PediaRemoteServiceServlet implements ImportService {

	private static final long serialVersionUID = 9171092272454900212L;

	private SessionLifetimeTasks<ImportFeaturesTask> tasks = new SessionLifetimeTasks<ImportFeaturesTask>();

	static {
		JavaFormatProvider.init();
	}

	
	@Override
	public ImportSettings analyzeUploadedFile(String fileToken)
			throws GeopediaException {
		Session session = getThreadLocalSession();
		if (session == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);

		ImportFeaturesTask currentTask = tasks.getTask(session);
		if (currentTask!=null) { // single  sfile per user only!
			if (!tasks.purgeDoneTasks(currentTask, session,true)) { 
				//TODO log error!!
				//TOOD throw an exception that makes sense
				throw new GeopediaException(GeopediaException.Type.UNKNOWN);
			}
		}

		try {
			ServletUploadedFiles items = FileUploadServiceImpl.INSTANCE.getUploadedFiles(fileToken);
			FileItem fileItem = items.getSingleFileItem();
			
			File tempDir = writeFileItemToTempDirectory(fileItem);
			
			File toImport = getFile(tempDir);
			ImportSettings iSettings = null;
			if (toImport!=null) {
				String name = toImport.getName().toLowerCase();
				ShapefileReader reader=null;
				if (name.endsWith("shp")) {
					reader = new ShapefileReader(toImport);
				}
				if (reader!=null) {
					ImportFeaturesTask ift = new ImportFeaturesTask(reader,tempDir, session);
					tasks.addTask(session, ift);
					iSettings = ift.getImportSettings();
					iSettings.tableName = name.substring(0,name.lastIndexOf('.'))+" "+
						DateFormatter.FORMATTER_ISO_DATETIME.formatDate(new Date());
				}
			}
			
			if (iSettings == null) // unknown file type
				throw ImportExportException.create(ImportExportException.Type.UNKNOWN_OR_CORRUPTED_FILE);
			
			FileUploadServiceImpl.INSTANCE.finishedWithItem(fileToken); // we have the file on the filesystem, release it from upload service
			return iSettings;
		} catch (Exception e) {
			e.printStackTrace();
			//TODO: log to proper logger
			throw ImportExportException.create(ImportExportException.Type.UNKNOWN_OR_CORRUPTED_FILE);
		}
	}
	
	public static File getFile (File directory) {
		File[] shpFiles =directory.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				if (!name.startsWith("._") && name.endsWith("shp"))
					return true;
				return false;
			}
		});
		if (shpFiles.length==1) {
			return shpFiles[0];
		} 
		return null;
	}
	
	private File writeFileItemToTempDirectory(FileItem fileItem) throws IOException {
		File tempDir = FileUtilJava.createTempDirectory();
		String contentType = fileItem.getContentType().toLowerCase();
		if (contentType.contains("x-zip-compressed") ||
				contentType.contains("application/zip") || contentType.contains("application/octet-stream") || contentType.contains("application/x-zip")){
			ZipInputStream zis = new ZipInputStream(fileItem.getInputStream());
			ZipEntry zEntry = null;
			while ((zEntry=zis.getNextEntry())!=null) {
				if (zEntry.isDirectory()) // flatten directories
					continue;			
				String justName = zEntry.getName();
				if (justName.contains("/")) {
					justName = zEntry.getName().substring(justName.lastIndexOf("/"));
				}
				FileOutputStream fos = new FileOutputStream(new File(tempDir, justName));
				byte buffer[] = new byte[1024];
				int len;
				try {
					while ((len=zis.read(buffer))>0)  
						fos.write(buffer,0,len);
					fos.flush();
				} finally {
					fos.close();
				}
				
			}
			
		} else {
			File outFile = new File(tempDir,fileItem.getName());
			OutputStream os = new FileOutputStream(outFile);
			InputStream is = fileItem.getInputStream();
			byte buffer[] = new byte[1024];
			int len;
			while ((len=is.read(buffer))>0) { 
				os.write(buffer,0,len);
			}
			os.close();
			is.close();
		}
		
		return tempDir;
	}

	@Override
	public ImportStatus doImport(String action, ImportSettings settings)
			throws UpdateException {
		Session session = getThreadLocalSession();
		if (session == null)
			throw new UpdateException(UpdateException.T_NO_SESSION);
		
		ImportFeaturesTask ift = tasks.getTask(session);
		if (ift!=null) {
			ImportStatus taskStatus = ift.getTaskStatus();
			if (CMD_IMPORT_START.equals(action) && taskStatus.getStatus() == Status.NOP ) {
				ift.setImportSettings(settings);
				ift.initialize();
				Thread importThread = new Thread(ift);
				importThread.setName("GPD: ImportFeaturesTask ("+session.getUser().getUsername()+")");
				importThread.start();
				return taskStatus;
			} else if (CMD_IMPORT_CANCEL.equals(action) && taskStatus.getStatus().isBefore(Status.DONE)) {
				if (tasks.purgeDoneTasks(ift, session,true)) {
					return new ImportStatus();
				}				
			} else {
				try {
					return taskStatus;
				} finally {
					tasks.purgeDoneTasks(ift, session);
				}
			}
				
		} 
		throw new UpdateException(UpdateException.T_INVALID_STATE);
		
	}
	
	
}