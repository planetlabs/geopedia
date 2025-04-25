package com.sinergise.common.gis.map.print;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.state.gwt.StateGWTOriginator;

public class TemplateSpec implements StateGWTOriginator {
	
	private static final String PARAM_NAME = "name";
	private static final String PARAM_TITLE = "title";
	private static final String PARAM_CONFIG_NAME = "configName";
	private static final String PARAM_SUPPORTS_ATTRIBUTES = "supportsAttributes";
	private static final String PARAM_DEFAULT_PAPER_SIZE = "defaultPaperSize";
	private static final String USER_PARAM_NAME = "name";
	private static final String USER_PARAM_UILABEL = "uiLabel";
	private static final String USER_PARAM_LENGTH = "length";
	
	public static final String STATE_KEY_TEMPLATES = "PrintTemplates";
	public static final String STATE_KEY_TEMPLATE = "Template";
	public static final String STATE_KEY_USER_PARAM = "UserParam";
	
	
	public static enum PaperSize {A0, A1, A2, A3, A4;
		public String getLabel() {
			return name();
		}
	}
	
	public static class TemplateUserInput implements StateGWTOriginator {
		public TemplateParam param;
		public String uiLabel;
		public int length;
		
		@Override
		public void loadInternalState(StateGWT st) {
			this.param = TemplateParam.create(st.getString(USER_PARAM_NAME, null));
			this.uiLabel = st.getString(USER_PARAM_UILABEL, null);
			this.length = st.getInt(USER_PARAM_LENGTH, 32);
		}
		
		@Override
		public StateGWT storeInternalState(StateGWT target) {
			target.putString(USER_PARAM_NAME, param.getParamName());
			target.putString(USER_PARAM_UILABEL, uiLabel);
			target.putInt(USER_PARAM_LENGTH, length);
			return target;
		}
	}
	
	public static final TemplateSpec DEFAULT = new TemplateSpec(); 
	
	public String templateXML;
	public String templateTitle;
	public EnumSet<PaperSize> availableSizes = EnumSet.noneOf(PaperSize.class);
	public TemplateUserInput[] options = new TemplateUserInput[0];
	public PaperSize defaultPaperSize = null;
	public boolean hasAttributes = false;
	public String configName = null;
	
	@Override
	public StateGWT storeInternalState(StateGWT st) {
		st.putString(PARAM_NAME, templateXML);
		st.putString(PARAM_TITLE, templateTitle);
		st.putString(PARAM_CONFIG_NAME, configName);
		if (hasAttributes) {
			st.putString(PARAM_SUPPORTS_ATTRIBUTES, "true");
		}
		storePaperSizeState(st);
		storeUserParamsState(st);
		return st;
	}
	
	@Override
	public void loadInternalState(StateGWT state) {
		this.templateXML = state.getString(PARAM_NAME, null);
		this.templateTitle = state.getString(PARAM_TITLE, templateTitle);
		this.configName = state.getString(PARAM_CONFIG_NAME, null);
		
		this.hasAttributes = state.getBoolean(PARAM_SUPPORTS_ATTRIBUTES, false);
		
		loadPaperSizeState(state);
		loadUserParamState(state);
	}

	private void storeUserParamsState(StateGWT st) {
		int i =0;
		for (TemplateUserInput userParam : options) {
			userParam.storeInternalState(st.createState(STATE_KEY_USER_PARAM+i++));
		}
	}

	private void storePaperSizeState(StateGWT st) {
		for (PaperSize size : availableSizes) {
			st.putBoolean(size.name(), true);
		}
		if (defaultPaperSize != null) {
			st.putString(PARAM_DEFAULT_PAPER_SIZE, defaultPaperSize.name());
		}
	}

	private void loadUserParamState(StateGWT state) {
		List<StateGWT> optionStates = StateGWT.readElementsWithPrefix(state, STATE_KEY_USER_PARAM);
		if (optionStates == null) {
			optionStates = Collections.emptyList();
		}
		options = new TemplateUserInput[optionStates.size()];
		for (int i = 0; i < optionStates.size(); i++) {
			options[i] = new TemplateUserInput();
			options[i].loadInternalState(optionStates.get(i));
		}
	}

	private void loadPaperSizeState(StateGWT state) {
		availableSizes.clear();
		for (PaperSize size : PaperSize.values()) {
			if (state.getBoolean(size.name(), false)) {
				availableSizes.add(size);
			}
		}
		String defaultPaperSizeString = state.getString(PARAM_DEFAULT_PAPER_SIZE, null);
		if (defaultPaperSizeString != null) {
			defaultPaperSize = PaperSize.valueOf(defaultPaperSizeString);
		}
	}
	
	@Override
	public String toString() {
		return templateTitle;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((templateTitle == null) ? 0 : templateTitle.hashCode());
		result = prime * result + ((templateXML == null) ? 0 : templateXML.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		TemplateSpec other = (TemplateSpec) obj;
		
		if (templateTitle == null) {
			if (other.templateTitle != null) return false;
		} else if (!templateTitle.equals(other.templateTitle)) return false;
		
		if (templateXML == null) {
			if (other.templateXML != null) return false;
		} else if (!templateXML.equals(other.templateXML)) return false;
		
		return true;
	}
	
	public static TemplateSpec[] loadTemplatesFromConfig(StateGWT templatesState) {
		StateGWT childState = templatesState.getState(STATE_KEY_TEMPLATES);
		if (childState != null) {
			return loadTemplatesState(childState);
		}
		return loadTemplatesState(templatesState);
	}
	
	private static TemplateSpec[] loadTemplatesState(StateGWT templatesState) {
		if (templatesState == null) return null;
		List<StateGWT> stList = StateGWT.readElementsWithPrefix(templatesState, STATE_KEY_TEMPLATE);
		if (stList == null || stList.isEmpty()) return new TemplateSpec[0];
		TemplateSpec[] ret = new TemplateSpec[stList.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new TemplateSpec();
			ret[i].loadInternalState(stList.get(i));
		}
		return ret;
	}

	public boolean hasSize(PaperSize size) {
		return availableSizes.contains(size);
	}
}