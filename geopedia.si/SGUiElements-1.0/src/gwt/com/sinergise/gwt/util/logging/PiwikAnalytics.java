package com.sinergise.gwt.util.logging;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.ScriptElement;
import com.sinergise.gwt.util.UtilGWT;

/**
 * Before using this, you'll have to include the PA code in a meta element of the head of the HTML host page:
 * <p>
 * <code>
 *   &lt;meta name="sgutil:pa:code"  content="x"/&gt;
 *   &lt;meta name="sgutil:pa:url"   content="www.xxxxx.xxx"/&gt;
 * </code>
 * </p>
 * Importaint note: You should omit http:// or https:// prefix in sgutil:pa:url content!
 * If the code is found the PA is initialized upon loading the <code>com.sinergise.Util</code> module.
 * 
 * @author miha, jani
 */
public class PiwikAnalytics {
	static boolean loadStarted = false;
	static String code;
	static String baseUrl;
	static JavaScriptObject tracker;
	static ArrayList<String> eventsQueue = new ArrayList<String>();
	static int retryCount = 0;
	static final int MAX_RETRY_COUNT = 10;


	public static void initialize() {
		if (loadStarted) return;

		code = UtilGWT.getMetaContent("sgutil:pa:code");
		baseUrl = UtilGWT.getMetaContent("sgutil:pa:url");
		loadStarted = true;
		if ((code == null) || (baseUrl == null)) {
			return;
		}

		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
			@Override
			public boolean execute() {
				System.out.println("[PA] Loading " + code);
				
				ScriptElement se = Document.get().createScriptElement();
				se.setType("text/javascript");
				se.setSrc("http://" + baseUrl + "piwik.js");
				HeadElement head = UtilGWT.getHead();
				if (head != null) {
					head.appendChild(se);
				} else {
					Document.get().getBody().appendChild(se);
				}

				Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
					@Override
					public boolean execute() {
						retryCount++;
						if (retryCount > MAX_RETRY_COUNT) {
							System.out.println("[PA] maximum number of attempts reached!");
							return false;
						}
						
						System.out.println("[PA] Initializing " + code);
						try {
							tracker = internal_init(code, baseUrl);
						} catch (Throwable e) {
							// retry in case of error
							// piwik.js might not be loaded
							return true;
						}
						if (tracker == null) return true; //Try again in a while

						for (String s : eventsQueue) {
							trackPageView(s);
						}
						eventsQueue.clear();
						return false;
					}
				}, 1000);
				
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
				System.out.println("[PA] TrackPageView: " + event + " (" + (tracker != null) + ")");
				GWT.log("trackPageView(" + event + ")", null);
				try {
					internal_trackPageview(tracker, event);
				} catch(Exception e) {
					// put event in queue in case of error
					// piwik.js might not be loaded
					eventsQueue.add(event);
				}
				
			} else {
				eventsQueue.add(event);
			}
		} catch(final Exception e) {
			GWT.log("trackPageView " + event, e);
		}
	}


	
	static native JavaScriptObject internal_init(String initCode, String url) 
	/*-{
		var pkBaseURL = (("https:" == document.location.protocol) ? "https://" : "http://") + url;
		var piwikTracker = $wnd.Piwik.getTracker(pkBaseURL + "piwik.php", initCode);
		return piwikTracker;
	}-*/;


	static native void internal_trackPageview(JavaScriptObject trck, String event)
	/*-{
	    if (trck) {
	    	trck.setDocumentTitle(event);
	       	trck.trackPageView();
			trck.enableLinkTracking();
	    }
	}-*/;


}
