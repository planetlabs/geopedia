package com.sinergise.geopedia.client.ui.widgets;

import com.google.gwt.core.client.Callback;
import com.sinergise.geopedia.client.resources.codemirror.GeopediaCodeMirrorStyle;
import com.sinergise.gwt.util.ExternalJSLoader;

public class CodeMirrorJSEditor extends CodeMirrorEditor {

	private static ExternalJSLoader multiJSLoader;

	public static void setJavascriptModeURL(String url) {
		multiJSLoader = new ExternalJSLoader.Multiple(new ExternalJSLoader[] { extJSLoader,
				new ExternalJSLoader.Single(url) });
	}

	@Override
	protected void loadCodeMirror() {
		GeopediaCodeMirrorStyle.INSTANCE.gpdCodeMirror().ensureInjected(); //inject custom style into code mirror
		multiJSLoader.ensureLoaded(new Callback<Void, Exception>() {

			@Override
			public void onFailure(Exception reason) {
				// ignore, there's ordinary TA anyway
			}

			@Override
			public void onSuccess(Void result) {
				initializeCodeMirror();
			}
		});
	}

}
