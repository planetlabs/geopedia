package com.sinergise.geopedia.client.core.util;


public class GoogleAnalytics {
    
    public static void trackEvent(final String category, final String action, final String opt_label, final int opt_value) {
        try {
//            System.out.println("[GA] TrackEvent: " + action + " (" + (loaded) + ")");
//            GWT.log("trackEvent(" + category + "," + action + "," + opt_label + "," + opt_value + ")", null);
            internal_trackEvent(category, action, opt_label, opt_value);
        } catch(final Exception e) {
//            GWT.log("trackEvent(" + category + "," + action + "," + opt_label + "," + opt_value + ")", e);
        }
    }
    
    public static void trackPageview(final String event) {
        try {
//            System.out.println("[GA] TrackPageview: " + event + " (" + (loaded) + ")");
//            GWT.log("trackPageview(" + event + ")", null);
            internal_trackPageview(event);
        } catch(final Exception e) {
//            GWT.log("trackPageview " + event, e);
        }
    }
    
    static native void internal_trackPageview(String event) /*-{
        $wnd._gaq.push(['_trackPageview', event]);
    }-*/;
    
    static native void internal_trackEvent(String category, String action, String opt_label, int opt_value) /*-{
        $wnd._gaq.push([category, action, opt_label])
    }-*/;
    
    private static native void load(String initCode) /*-{
        $wnd._gaq.push(['_setAccount', initCode]);
    }-*/;
}