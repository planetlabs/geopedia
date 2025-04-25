package com.sinergise.common.web.i18n;

import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.util.format.Locale;
import com.sinergise.common.util.lang.AsyncFunction;
import com.sinergise.common.util.lang.SGAsyncCallback;

@RemoteServiceRelativePath("dictionary")
public interface DictionaryService extends RemoteService {

	GetDictionaryResponse getDictionary(GetDictionaryRequest request) throws ServiceException;
	
	
	public static class App {
        public static synchronized DictionaryServiceAsync createInstance() {
        	if (GWT.isClient()) {
        		return GWT.create(DictionaryService.class);
            }
            return null;
        }
    }
	
	public static DictionaryServiceAsync INSTANCE = App.createInstance();
	
	
	public static class LoadDictonaryAsync implements AsyncFunction<Object, Object> {
		
		final String dictonaryName;
		
		public LoadDictonaryAsync(String dictionaryName) {
			this.dictonaryName = dictionaryName;
		}
		
		@Override
		public void executeAsync(Object param, final SGAsyncCallback<? super Object> callback) throws Exception {
			DictionaryService.INSTANCE.getDictionary(
				new GetDictionaryRequest(dictonaryName, 
						Locale.forName(LocaleInfo.getCurrentLocale().getLocaleName())), 
						
				new AsyncCallback<GetDictionaryResponse>() {
					@Override
					public void onSuccess(GetDictionaryResponse result) {
						LookupStringProvider.FromDictionary.createDictionary(
								result.getDictionaryName(), result.getDictionaryMap());
						callback.onSuccess(null);
					}
					
					@Override
					public void onFailure(Throwable ignore) {
						LoggerFactory.getLogger(getClass()).error("Failed to load dictionary", ignore);
						callback.onSuccess(null); //still call on success, not essential
					}
				}
			);
		}
	}
	
}
