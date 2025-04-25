package com.sinergise.gwt.util.logging;

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.sinergise.gwt.util.UtilGWT;

/**
 * Before using this, you'll have to include the GA code in a meta element of the head of the HTML host page:
 * <p>
 * <code>
 *   &lt;meta name="sgutil:ga:code" content="UA-xxxxxx-x"/&gt;
 * </code>
 * </p>
 * If the code is found the GA is initialized upon loading the <code>com.sinergise.Util</code> module.
 * 
 * @author Miha
 */
public class GoogleAnalytics {
	public static final class GAEvent {
		final String category;
		final String action;
		final String opt_label;
		final int opt_value;
		final boolean pageView;

		public GAEvent(String pageViewEvent) {
			pageView = true;
			action = pageViewEvent;
			category = pageViewEvent;
			opt_label = null;
			opt_value = 0;
		}

		public GAEvent(String category, String action, String opt_label, int opt_value) {
			this.category = category;
			this.action = action;
			this.opt_label = opt_label;
			this.opt_value = opt_value;
			this.pageView = false;
		}

	}

	static boolean loadStarted = false;
	static String code;
	static JavaScriptObject tracker;
	static ArrayList<GAEvent> eventsQueue = new ArrayList<GAEvent>();
	static org.slf4j.Logger logger = LoggerFactory.getLogger(GoogleAnalytics.class);

	public static void initialize() {
		if (loadStarted) return;
		
		code = UtilGWT.getMetaContent("sgutil:ga:code");
		loadStarted = true;
		if (code == null) return;
		
		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
			@Override
			public boolean execute() {
				logger.info("loading " + code);
				internal_load();
				Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
					int nTries = 0;
					@Override
					public boolean execute() {
						nTries++;
						if (nTries == 1) {
							logger.info("initializing {}", code);
						} else {
							logger.debug("initializing again {} try no. {}", code, Integer.valueOf(nTries));
						}
						tracker = internal_init(code);
						if (tracker == null) {
							//Try again in a while, give up after two minutes
							if (nTries >= 60) {
								logger.info("failed to initialize GA {} after {} tries. Giving up.", code, Integer.valueOf(nTries));
								return false;
							}
							return true;
						}

						logger.info("initialized {}", code);
						for (GAEvent e : eventsQueue) {
							if (e.pageView) trackPageView(e.action);
							else trackEvent(e.category, e.action, e.opt_label, e.opt_value);
						}
						eventsQueue.clear();
						return false;
					}
				}, 2000);
				return false;
			}
		}, 1000);
	}

	public static boolean isEnabled() {
		checkInit();
		return code != null;
	}

	protected static boolean checkInit() {
		try {
			if (!loadStarted) {
				initialize();
				return false;
			}
			return tracker != null;
		} catch(final Exception e) {
			e.printStackTrace();
			tracker = null;
			return false;
		}
	}

	public static void trackPageView(final String event) {
		try {
			if (!isEnabled()) return;
			if (checkInit()) {
				logger.info("trackPageView (inited): {}", event);
				internal_trackPageview(tracker, event);
			} else {
				logger.info("trackPageView (queued): {}", event);
				eventsQueue.add(new GAEvent(event));
			}
		} catch (final Exception e) {
			logger.warn("trackPageView (excepn): {}", event, e);
		}
	}

	public static void trackEvent(final String category, final String action, final String opt_label,
			final int opt_value) {
		try {
			if (!isEnabled()) return;
			if (checkInit()) {
				logger.info("trackEvent (inited): {}, {}, {}, {}", new Object[] {category, action, opt_label, Integer.valueOf(opt_value)});
				internal_trackEvent(tracker, category, action, opt_label, opt_value);
			} else {
				logger.info("trackEvent (queued): {}, {}, {}, {}", new Object[] {category, action, opt_label, Integer.valueOf(opt_value)});
				eventsQueue.add(new GAEvent(category, action, opt_label, opt_value));
			}
		} catch(final Exception e) {
			logger.warn("trackEvent (excepn): {}, {}, {}, {}", new Object[] {category, action, opt_label, Integer.valueOf(opt_value), e});
		}
	}	

	static native void internal_load()
	/*-{
	    var e = $doc.createElement("script");
	    e.src = (("https:" == $doc.location.protocol) ? "https://ssl." : "http://www.")+"google-analytics.com/ga.js";
	    e.type="text/javascript";
		$doc.getElementsByTagName("head")[0].appendChild(e); 
	}-*/;

	static native JavaScriptObject internal_init(String initCode)
	/*-{
		if ($wnd._gat) {
	    	if ($wnd._gat==undefined) return null;
	        
	        var pageTracker = $wnd._gat._getTracker(initCode);
	        pageTracker._setAllowAnchor(true);
	        pageTracker._initData();
	        return pageTracker;
	    }
	    return null;
	}-*/;

	static native void internal_trackEvent(JavaScriptObject trck, String category, String action, String opt_label,
			int opt_value)
	/*-{
	    if (trck) {
	        trck._trackEvent(category, action, opt_label, opt_value);
	    }
	}-*/;
	
	static native void internal_trackPageview(JavaScriptObject trck, String event)
	/*-{
	    if (trck) {
	        trck._trackPageview(event);
	    } else {
	        // do nothing
	    }
	}-*/;
}
