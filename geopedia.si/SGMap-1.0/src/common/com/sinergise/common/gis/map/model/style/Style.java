/*
 *
 */
package com.sinergise.common.gis.map.model.style;

import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;

public interface Style extends SourcesPropertyChangeEvents<Object> {
    public static final String PROP_LEGEND_URL="legendURL";

    void reset();
}
