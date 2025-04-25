package com.sinergise.java.web.logging;

import java.io.File;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.gwt.logging.server.StackTraceDeobfuscator;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * <p>GWT's RemoteLoggingService implementation for logging with SL4J.</p>
 * <p>Use this servlet instead of the default {@link RemoteLoggingServiceImpl} when logging
 * to SL4J to avoid expensive overhead of jul-to-sl4j bridge.</p>
 * 
 * <p>Servlet Parameters:
 * <li><b>gwtSymbolsMapDir:</b> directory containing GWT symbolMap files. Used to deobfuscate GWT stack traces.</li>
 * <li><b>defaultMarker:</b> marker to append to all log messages.
 * </p>
 * 
 * @author tcerovski
 */
public class SL4JRemoteLoggingService extends RemoteServiceServlet implements RemoteLoggingService {
	
	private static final long serialVersionUID = 1L;
	
	private static final String PARAM_GWT_SYMBOLS_MAP_DIR = "gwtSymbolsMapDir";
	private static final String PARAM_DEFAULT_MARKER = "defaultMarker";
	
	private final Logger logger = LoggerFactory.getLogger(SL4JRemoteLoggingService.class);
	
	private StackTraceDeobfuscator deobfuscator = null;
	private Marker defaultMarker = MarkerFactory.getMarker("GWT");
	

	public String logOnServer(LogRecord lr) {
		
		String strongName = getPermutationStrongName();
		try {
			if (deobfuscator != null) {
				lr = deobfuscator.deobfuscateLogRecord(lr, strongName);
			}
			Logger clientLogger = getLogger(lr);
			onBeforeLog(clientLogger, lr);
			logMessage(clientLogger, lr, defaultMarker);
		} catch (Exception ex) {
			logger.error("Remote logging failed!", ex);
			return "Remote logging failed!";
		}
		return null;
	}
	
	protected Logger getLogger(LogRecord record) {
		String name = record.getLoggerName();
		if (name == null) {
			name = "unknown.gwt.logger";
		}
		return LoggerFactory.getLogger(name);
	}
	
	private static String getMessageI18N(LogRecord record) {
		String message = record.getMessage();

		if (message == null) {
			return null;
		}

		ResourceBundle bundle = record.getResourceBundle();
		if (bundle != null) {
			try {
				message = bundle.getString(message);
			} catch (MissingResourceException e) {
			}
		}
		Object[] params = record.getParameters();
		if (params != null) {
			message = MessageFormat.format(message, params);
		}
		return message;
	}
	
	protected void logMessage(Logger slf4jLogger, LogRecord record, Marker marker) {
		String i18nMessage = getMessageI18N(record);
		int julLevelValue = record.getLevel().intValue();
		if (julLevelValue <= Level.FINEST.intValue()) {
			slf4jLogger.trace(marker, i18nMessage, record.getThrown());
		} else if (julLevelValue <=  Level.FINER.intValue()) {
			slf4jLogger.debug(marker, i18nMessage, record.getThrown());
		} else if (julLevelValue <= Level.INFO.intValue()) {
			slf4jLogger.info(marker, i18nMessage, record.getThrown());
		} else if (julLevelValue <= Level.WARNING.intValue()) {
			slf4jLogger.warn(marker, i18nMessage, record.getThrown());
		} else {
			slf4jLogger.error(marker, i18nMessage, record.getThrown());
		}
	}
	
	
	
	/**
	 * Update MDC or any other actions..
	 * 
	 * @param clientLogger  - SLF4J logger
	 * @param lr - log record
	 */
	protected void onBeforeLog(Logger clientLogger, LogRecord lr) {
	}

	
	@Override
	public void init() throws ServletException {
		logger.info("Initializing SL4JRemoteLoggingService...");
		super.init();
		
		String gwtSymbolsMap = getServletConfig().getInitParameter(PARAM_GWT_SYMBOLS_MAP_DIR);
		if (gwtSymbolsMap != null && gwtSymbolsMap.trim().length() > 0) {
			File gwtSymbolsMapFile = new File(getServletContext().getRealPath(gwtSymbolsMap));
			if (gwtSymbolsMapFile.exists() && gwtSymbolsMapFile.isDirectory()) {
				deobfuscator = new StackTraceDeobfuscator(gwtSymbolsMapFile.getAbsolutePath()+File.separator);
				logger.info("GWT Deobfuscator symbols map directory set to: {}", gwtSymbolsMapFile.getAbsolutePath());
			} else {
				logger.warn("Could not find GWT Deobfuscator symbols map at: {}", gwtSymbolsMapFile.getAbsolutePath());
			}
		}
		
		String defaultMarkerStr = getServletConfig().getInitParameter(PARAM_DEFAULT_MARKER);
		if (defaultMarkerStr != null && defaultMarkerStr.trim().length() > 0) {
			defaultMarker = MarkerFactory.getMarker(defaultMarkerStr);
			logger.info("Default marker value set to: {}", defaultMarkerStr);
		}
		
		logger.info("SL4JRemoteLoggingService initialized.");
	}
	
	
	

}
