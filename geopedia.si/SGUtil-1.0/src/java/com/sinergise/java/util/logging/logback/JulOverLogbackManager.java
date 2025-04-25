package com.sinergise.java.util.logging.logback;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.core.Context;

public class JulOverLogbackManager extends LevelChangePropagator {
	private boolean removeAllHandlers = false;
	private AtomicBoolean inited = new AtomicBoolean(false);
	
	public JulOverLogbackManager() {
		super();
		setResetJUL(true);
	}
	
	@Override
	public void setContext(Context context) {
		super.setContext(context);
		initJulBridge();
	}
	
	public void setRemoveAllHandlers(boolean removeAllHandlers) {
		if (this.removeAllHandlers != removeAllHandlers) {
			this.removeAllHandlers = removeAllHandlers;
			if (inited.get() && removeAllHandlers) {
				removeAllHandlers();
			}
		}
	}
	
	@Override
	public void onStart(LoggerContext ctx) {
		super.onStart(ctx);
		initJulBridge();
	}

	public void initJulBridge() {
		if (inited.get()) return;
		try {
			org.slf4j.bridge.SLF4JBridgeHandler.uninstall();
			if (removeAllHandlers) {
				removeAllHandlers();
			}
			org.slf4j.bridge.SLF4JBridgeHandler.install();
			inited.set(true);
		} catch (Exception e) {
			LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn("Failed to install SLF4JBridgeHandler", e);
		}
	}
	
	private static void removeAllHandlers() {
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (int i = 0; i < handlers.length; ++i) {
			if (!(handlers[i] instanceof org.slf4j.bridge.SLF4JBridgeHandler)) {
				rootLogger.removeHandler(handlers[i]);
			}
		}
	}
	
	@Override
	public void onReset(LoggerContext ctx) {
		initJulBridge();
		super.onReset(ctx);
	}

	@Override
	public void onStop(LoggerContext ctx) {
		super.onStop(ctx);
		try {
			org.slf4j.bridge.SLF4JBridgeHandler.uninstall();
			inited.set(false);
		} catch (Exception e) {
			LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn("Failed to uninstall SLF4JBridgeHandler", e);
		}
	}
}
