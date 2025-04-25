package com.sinergise.gwt.util.history;

import static com.sinergise.common.util.collections.CollectionUtil.mapToList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.string.PercentSequenceEncoder;

/**
 * @author tcerovski
 */
public class HistoryManager implements ValueChangeHandler<String> {

	private static HistoryManager instance = null;

	public static synchronized HistoryManager getInstance() {
		if (instance == null) {
			instance = new HistoryManager();
		}
		return instance;
	}

	private static final String PAIR_SEPARATOR = "=";
	private static final PercentSequenceEncoder PART_ENCODER = new PercentSequenceEncoder('%', '&');
	private static final Function<Entry<String, String>, String> FUN_PARAM_TO_STRING = new Function<Entry<String, String>, String>() {
		public String execute(Entry<String, String> param) {
			String val = param.getValue();
			if (val != null) {
				return param.getKey() + PAIR_SEPARATOR + val;
			}
			return param.getKey();
		}
	};
	private Logger logger = LoggerFactory.getLogger(HistoryManager.class);
	protected Map<String, String> params = new LinkedHashMap<String, String>();
	protected Map<String, List<PrioritizedHandler>> handlers = new LinkedHashMap<String, List<PrioritizedHandler>>();

	private HistoryManager() {
		History.addValueChangeHandler(this);
		handleHistoryToken(History.getToken());
	}

	public void registerHandler(HistoryHandler handler) {
		registerHandler(handler, 0);
	}

	/**
	 * @param handler to register
	 * @param priority - Higher priority handlers should be called first.
	 */
	public void registerHandler(HistoryHandler handler, int priority) {
		if (handler == null) {
			return;
		}
		PrioritizedHandler pHandler = new PrioritizedHandler(handler, priority);
		for (String param : handler.getHandledHistoryParams()) {
			List<PrioritizedHandler> paramHandlers = handlers.get(param);
			if (paramHandlers == null) {
				handlers.put(param, paramHandlers = new ArrayList<PrioritizedHandler>());
			}
			paramHandlers.add(pHandler);
			Collections.sort(paramHandlers);
		}

		//handle existing token to fire changes on new handler
		handler.handleHistoryChange(this);
	}

	public void setHistoryParam(String key, String value) {
		if (key == null) {
			return;
		}

		setHistoryParams(new String[]{key}, new String[]{value});
	}

	public void setHistoryParams(String[] keys, String[] values) {
		if (keys == null || values == null) {
			return;
		}

		Map<String, String> tempParams = new HashMap<String, String>(params);
		for (int i = 0; i < Math.min(keys.length, values.length); i++) {
			if (keys[i] == null)
				continue;
			tempParams.put(keys[i], values[i]);
		}
		updateHistoryToken(tempParams);
	}

	public void setHistoryParams(Map<String, String> toSet, boolean cleanFirst) {
		if (cleanFirst) {
			Map<String, String> tempParams = new HashMap<String, String>(params);
			for (String key : toSet.keySet()) {
				tempParams.put(key, null);
			}
			updateHistoryToken(tempParams);
		}
		setHistoryParams(toSet);
	}

	public void setHistoryParams(Map<String, String> toSet) {
		if (toSet == null) {
			return;
		}

		Map<String, String> tempParams = new HashMap<String, String>(params);

		for (String key : toSet.keySet()) {
			tempParams.put(key, toSet.get(key));
		}
		updateHistoryToken(tempParams);
	}

	public void removeHistoryParam(String key) {
		if (key == null) {
			return;
		}

		Map<String, String> tempParams = new HashMap<String, String>(params);
		tempParams.remove(key);
		updateHistoryToken(tempParams);
	}

	public String getHistoryParam(String key) {
		if (key == null) {
			return null;
		}

		return params.get(key);
	}
	
	public Double getHistoryDoubleParam(String key) {
		String str = getHistoryParam(key);		
		if (str == null) {
			return null;
		}
		
		return Double.valueOf(str);
	}

	public boolean hasHistoryParam(String key) {
		if (key == null) {
			return false;
		}

		return params.containsKey(key);
	}

	public String getUrlFragment() {
		return getUrlFragment(params);
	}

	private static String getUrlFragment(Map<String, String> fromParams) {
		if (fromParams.isEmpty()) {
			return "";
		}
		return PART_ENCODER.encode(mapToList(fromParams.entrySet(), FUN_PARAM_TO_STRING));
	}

	private static void updateHistoryToken(Map<String, String> fromParams) {
		History.newItem(getUrlFragment(fromParams));
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		handleHistoryToken(event.getValue());
	}

	public void fireCurrentHistoryState() {
		handleHistoryToken(History.getToken(), params.keySet());
	}

	void handleHistoryToken(String token) {
		handleHistoryToken(token, null);
	}

	/**
	 * @param token to handle
	 * @param forceHandle - parameter keys to handle even if unchanged (should be used when binding new handler)
	 */
	void handleHistoryToken(String token, Collection<String> forceHandle) {
		if (forceHandle == null) {
			forceHandle = Collections.emptyList();
		}
		Set<String> changed = new HashSet<String>();

		//update parameters from URL
		Map<String, String> tokenParams = parseHistoryToken(token);
		for (String key : tokenParams.keySet()) {
			String val = tokenParams.get(key);

			if (!params.containsKey(key) || (val == null && params.get(key) != null)
				|| (val != null && (!val.equals(params.get(key))) || forceHandle.contains(key))) {
				changed.add(key);
			}
			params.put(key, val);
		}


		//check if any parameters were removed
		Collection<String> toRemove = new ArrayList<String>();
		for (String key : params.keySet()) {
			if (!tokenParams.containsKey(key)) {
				toRemove.add(key);
			}
		}
		logger.debug("HistoryManager changed: {} | removed: {}", changed, toRemove);
		for (String key : toRemove) {
			params.remove(key);
			changed.add(key);
		}

		Set<PrioritizedHandler> toCall = new LinkedHashSet<PrioritizedHandler>();
		for (String key : changed) {
			if (handlers.get(key) != null) {
				toCall.addAll(handlers.get(key));
			}
		}
		logger.debug("HistoryManager calling {} handlers", Integer.valueOf(toCall.size()));
		for (PrioritizedHandler pHandler : toCall) {
			pHandler.handler.handleHistoryChange(this);
		}
	}

	public static Map<String, String> parseHistoryToken(String token) {
		if (token.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> tokenParams = new HashMap<String, String>();

		//replace old separator for existing links
		token = token.replaceAll("\\|", "&");

		if (token != null && token.length() > 0) {
			for (String param : PART_ENCODER.decode(token)) {
				if (param.isEmpty()) {
					continue;
				}
				// Only first '=' is separator, others should be treated as data
				String[] pair = param.split(PAIR_SEPARATOR, 2);
				if (pair.length == 2) {
					tokenParams.put(pair[0], pair[1]);
				} else {
					tokenParams.put(param, null);
				}
			}
		}

		return tokenParams;
	}

	private static class PrioritizedHandler implements Comparable<PrioritizedHandler> {
		final HistoryHandler handler;
		final int priority;

		PrioritizedHandler(HistoryHandler handler, int priority) {
			super();
			this.handler = handler;
			this.priority = priority;
		}

		@Override
		public boolean equals(Object obj) {
			return handler.equals(obj);
		}

		@Override
		public int hashCode() {
			return handler.hashCode();
		}

		@Override
		public int compareTo(PrioritizedHandler o) {
			return o.priority - priority;
		}

	}

}
