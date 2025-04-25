package com.sinergise.geopedia.server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.UserAccessControl;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.app.session.SessionDestroyedListener;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.service.ExportService;
import com.sinergise.geopedia.core.service.params.ExportSettings;
import com.sinergise.geopedia.core.service.params.ExportStatus;
import com.sinergise.geopedia.server.PediaRemoteServiceServlet;
import com.sinergise.geopedia.server.ServUtil;
import com.sinergise.geopedia.server.service.export.CSVFileWriter;
import com.sinergise.geopedia.server.service.export.FeaturesExportTask;
import com.sinergise.geopedia.server.service.export.GPX11FileWriter;
import com.sinergise.geopedia.server.service.export.XLSXFileWriter;
import com.sinergise.java.gis.io.ShapefileFeatureWriter;
import com.sinergise.java.util.format.JavaFormatProvider;
import com.sinergise.java.util.io.FileUtilJava;

public class ExportServiceImpl extends PediaRemoteServiceServlet
		implements ExportService {

	private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);
	public static final String PARM_CMD = "cmd";

	private static final long serialVersionUID = 2451544089225780767L;

	private HashMap<String, FeaturesExportTask> exportTasks = new HashMap<String, FeaturesExportTask>();

	
	static {
		JavaFormatProvider.init();
	}
	private SessionDestroyedListener sessDestroyedListener = new SessionDestroyedListener() {

		@Override
		public void onSessionDestroyed(Session ses) {
			synchronized (exportTasks) {
				FeaturesExportTask fet = exportTasks.get(ses.getID());
				if (fet != null) {
					synchronized (fet) {
						purgeDoneTasks(fet, ses, true);
					}
				}
			}

		}
	};

	private ExportStatus purgeDoneTasks(FeaturesExportTask task, Session session) {
		return purgeDoneTasks(task, session, false);
	}

	
	private ExportStatus purgeDoneTasks(FeaturesExportTask task, Session session,
			boolean force) {
		if (!task.getStatus().isFinalStatus() && !force)
			return task.getStatus();
		task.cleanup();
		synchronized (exportTasks) {
			exportTasks.remove(session.getID());
			session.removeDestroyedListener(sessDestroyedListener);
		}
		ExportStatus st = new ExportStatus();
		st.setNOP();
		return st;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
		Session session = ServUtil.extractSession(req);
		if (session == null)
			return;

		String cmd = req.getParameter(PARM_CMD);
		if (CMD_EXPORT_DOWNLOAD.equals(cmd)) {
			FeaturesExportTask fet = null;
			synchronized (exportTasks) {
				fet = exportTasks.get(session.getID());
			}
			if (fet == null || !fet.getStatus().isExported())
				return;

			try {
				String filename = "export.zip";
				resp.setContentType("application/zip");
				resp.setHeader("Content-Disposition", "attachment;filename="+ filename);
				zipFiles(fet.getWorkDirectory(), resp.getOutputStream());
				fet.markAsDownloaded();
			} finally {
				purgeDoneTasks(fet, session);
			}
		}
		} catch (GeopediaException ex) {
			ex.printStackTrace();			
		}
	}

	public static void zipFiles(File fileFolder, OutputStream os)
			throws IOException {
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(os));
		for (File f : fileFolder.listFiles()) {
			addZipEntry(f, f.getName(), out);
			f.delete();
		}
		out.close();
		fileFolder.delete();
	}

	private static final int BUFFER = 16384;

	private static void addZipEntry(File inputFile, String fileName,
			ZipOutputStream out) {
		BufferedInputStream origin = null;

		byte data[] = new byte[BUFFER];

		try {
			FileInputStream fi = new FileInputStream(inputFile);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(fileName);
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ExportStatus doExport(String action, ExportSettings settings)
			throws GeopediaException {
		Session session = getThreadLocalSession();
		if (session == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);

		FeaturesExportTask fet = null;
		synchronized (exportTasks) {
			fet = exportTasks.get(session.getID());
		}

		if (fet != null) {
			synchronized (fet) {
				if (CMD_EXPORT_CANCEL.equals(action)) {
					return purgeDoneTasks(fet, session,true);
				} else {
					try {
						return fet.getStatus();
					} finally {
						purgeDoneTasks(fet, session);
					}
				}
			}
		} else {
			if (CMD_EXPORT_START.equals(action)) {
				try {
				if (UserAccessControl.hasAccess(session, GeopediaEntity.TABLE, settings.tableID,0, Permissions.ADMINPERMS)) {
					return startExport(session, settings);
				} else {
					throw new GeopediaException(
							GeopediaException.Type.PERMISSION_DENIED);
				}
				} catch (SQLException ex) {
					
				}
		}
		}

		ExportStatus nopStatus = new ExportStatus();
		nopStatus.setNOP();
		return nopStatus;
	}

	private ExportStatus startExport(Session session, ExportSettings settings) throws GeopediaException {
		ServerInstance instance = ServerInstance.getInstance(session.getInstanceId());
		int tableId = settings.tableID;
		FeaturesExportTask exportTask;
		File destFolder = FileUtilJava.createTempDirectory();
		File destFile = new File(destFolder, String.valueOf(tableId));
		try {			
			FeatureWriter fWriter = null;
			switch (settings.exportFormat) {
				case ExportSettings.FMT_CSV:
					fWriter = new CSVFileWriter(destFile);
					break;
				case ExportSettings.FMT_XLSX:
					fWriter = new XLSXFileWriter(destFile);
					break;
				case ExportSettings.FMT_GPX:
					//AP- for GPX export the end format has to be always WGS84 (i think...)
					settings.crsTransformID = CRS.WGS84.getDefaultIdentifier(); 
					//the transformer will created and used in the export task
					fWriter = new GPX11FileWriter(destFile);
					
					break;				
				case ExportSettings.FMT_SHP:
				default:
					fWriter = new ShapefileFeatureWriter(destFile);
					break;				
			}
			exportTask = new FeaturesExportTask(fWriter, destFolder, session, instance);
			exportTask.initialize(settings);
			Thread exportThread = new Thread(exportTask);
			exportThread.setName("GPD: ExportFeaturesTask ("+session.getUser().getUsername()+")");
			exportThread.start();

			
			synchronized (exportTasks) {
				exportTasks.put(session.getID(), exportTask);
				session.addDestroyedListener(sessDestroyedListener);
			}
			return exportTask.getStatus();
		} catch (Exception ex) {
			ex.printStackTrace();
			ExportStatus es = new ExportStatus();
			es.setError(ex.getLocalizedMessage());
			return es;
		}

	}
	
	
	

}
