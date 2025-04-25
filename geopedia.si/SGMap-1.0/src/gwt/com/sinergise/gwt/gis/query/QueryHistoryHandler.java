package com.sinergise.gwt.gis.query;

import static com.sinergise.common.util.string.StringUtil.trimNullEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.string.PercentSequenceEncoder;
import com.sinergise.gwt.util.history.HistoryHandler;
import com.sinergise.gwt.util.history.HistoryManager;

/**
 * @author tcerovski
 *
 */
public class QueryHistoryHandler implements HistoryHandler {
	private final Logger logger = LoggerFactory.getLogger(QueryHistoryHandler.class);
	
	public static final String HISTORY_PARAM_KEY_QUERY = "query";
	public static final String PAIR_SEPARATOR = ":";
	public static final String LAYER_SEPARATOR = ":";
	public static final String QUERY_ALL_VALUE = "*";
	public static final PercentSequenceEncoder FIELD_QUERY_ENCODER = new PercentSequenceEncoder('%', '@');
	
	public static void bind(QueryHistoryListener listener) {
		HistoryManager.getInstance().registerHandler(new QueryHistoryHandler(listener));
	}
	
	private final QueryHistoryListener listener;
	
	private QueryHistoryHandler(QueryHistoryListener listener) {
		this.listener = listener;
	}

	@Override
	public Collection<String> getHandledHistoryParams() {
		return Arrays.asList(HISTORY_PARAM_KEY_QUERY);
	}

	@Override
	public void handleHistoryChange(HistoryManager manager) {
		String queryParam = trimNullEmpty(manager.getHistoryParam(HISTORY_PARAM_KEY_QUERY));
		if (queryParam == null) {
			return; //no query params
		}
		try {
			String[] s = queryParam.split(LAYER_SEPARATOR, 2);
			String featureType = s[0];
			String queryValues = s[1];

			if (queryValues.equals(QUERY_ALL_VALUE)) {
				listener.executeQuery(featureType, Collections.<String, String>emptyMap());
				return;
			}
			
			String[] fieldValues = splitFieldQueries(queryValues);
			Map<String, String> valuesMap = new HashMap<String, String>(fieldValues.length);
			for (String fieldValue : fieldValues) {
				s = fieldValue.split(PAIR_SEPARATOR,2);
				valuesMap.put(s[0], s[1]);
			}
			listener.executeQuery(featureType, valuesMap);
		} catch (Throwable t) {
			logger.warn("Error parsing "+HISTORY_PARAM_KEY_QUERY+" history param: "+queryParam, t);
		}
	}
	
	public static String[] splitFieldQueries(String queryValues) {
		if (queryValues.isEmpty()) {
			return new String[0];
		}
		return FIELD_QUERY_ENCODER.decode(queryValues);
	}
	
	public static void setQueryParamValue(String featureType, Map<String, String> fieldValues) {
		if (featureType == null || fieldValues == null || fieldValues.isEmpty()) {
			HistoryManager.getInstance().removeHistoryParam(HISTORY_PARAM_KEY_QUERY);
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(featureType).append(LAYER_SEPARATOR);
		if (!fieldValues.isEmpty()) {
			String[] fieldQueries = CollectionUtil.map(fieldValues.entrySet(), new String[fieldValues.size()], new Function<Entry<String, String>, String>() {
				@Override
				public String execute(Entry<String, String> param) {
					return param.getKey() + PAIR_SEPARATOR + param.getValue();
				}
			});
			FIELD_QUERY_ENCODER.encodeAndAppend(sb, fieldQueries);
		}
		HistoryManager.getInstance().setHistoryParam(HISTORY_PARAM_KEY_QUERY, sb.toString());
	}
}
