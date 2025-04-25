/*
 *
 */
package com.sinergise.common.gis.feature.auxprops;

import com.sinergise.common.util.state.gwt.DefaultStateOriginator;
import com.sinergise.common.util.state.gwt.StateGWT;

public class FeatureTransactionProperties extends DefaultStateOriginator {
	
	private static final long serialVersionUID = 1L;
	
    public static final String KEY_DATE="Date";
    public static final String KEY_UPDATE_TYPE="UpdateType";
    public static final String KEY_USER_ID="UserId";
    
    /**
     * @deprecated Serialization only
     */
    @Deprecated
	public FeatureTransactionProperties() {
    }
    
    public FeatureTransactionProperties(StateGWT source) {
        super(source);
    }
}
