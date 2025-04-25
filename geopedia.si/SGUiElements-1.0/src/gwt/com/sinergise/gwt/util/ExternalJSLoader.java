package com.sinergise.gwt.util;

import java.util.ArrayList;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;

public abstract class ExternalJSLoader {
	protected ArrayList<Callback<Void, Exception>> afterLoadedCBList = new ArrayList<Callback<Void, Exception>>();
	protected boolean isLoaded=false;
	
	protected abstract void load();
	
	public void ensureLoaded(Callback<Void,Exception> callback) {
		if (isLoaded) {
			callback.onSuccess(null);
		} else {
			synchronized (afterLoadedCBList) {				
				afterLoadedCBList.add(callback);
				load();
			}
		}
	}

	public static class Multiple extends ExternalJSLoader{
		private ArrayList<ExternalJSLoader> loaders = new ArrayList<ExternalJSLoader>();
		private boolean isLoading =false;
		private Callback<Void, Exception> loadedCB = new Callback<Void, Exception>() {

			@Override
			public void onFailure(Exception reason) {
				synchronized (afterLoadedCBList) {
					for (Callback<Void, Exception> cb:afterLoadedCBList) {
						cb.onFailure(reason);
					}
					afterLoadedCBList.clear();
				}
			}

			@Override
			public void onSuccess(Void result) {
				if (loaders.size()>0) {
					loaders.remove(0);
				}
				if (loaders.size()>0) {
					internalLoad();
				} else {
					isLoaded=true;
					isLoading=false;
					synchronized (afterLoadedCBList) {
						for (Callback<Void, Exception> cb:afterLoadedCBList) {
							cb.onSuccess(result);
						}
						afterLoadedCBList.clear();
					}
				}

				
			}
		};
		
		public Multiple(ExternalJSLoader[] loaders) {
			for (ExternalJSLoader l:loaders)
				this.loaders.add(l);
		}
		
		
		@Override
		protected void load() {
			if (isLoading || isLoaded) return;
			isLoading=true;
			internalLoad();
		}
		
		protected void internalLoad() {
			if (loaders.size()>0) {
				loaders.get(0).ensureLoaded(loadedCB);
			}
		}
		
		
	}
	
	public static class Single extends ExternalJSLoader{
		private boolean isLoading =false;
		private String scriptURL=null;
		
		public Single(String scriptURL) {
			this.scriptURL=scriptURL;
		}
		
		@Override
		protected void load() {
			if (isLoading || isLoaded) return;
			isLoading=true;
			ScriptInjector.fromUrl(scriptURL).setCallback(new Callback<Void, Exception>() {
				
				@Override
				public void onSuccess(Void result) {
					isLoaded=true;
					isLoading=false;
					synchronized (afterLoadedCBList) {
						for (Callback<Void, Exception> cb:afterLoadedCBList) {
							cb.onSuccess(result);
						}
						afterLoadedCBList.clear();
					}
				}
				
				@Override
				public void onFailure(Exception reason) {
					synchronized (afterLoadedCBList) {
						for (Callback<Void, Exception> cb:afterLoadedCBList) {
							cb.onFailure(reason);
						}
						afterLoadedCBList.clear();
					}
				}
			}).setWindow(ScriptInjector.TOP_WINDOW).inject();
		}		
	}
}
