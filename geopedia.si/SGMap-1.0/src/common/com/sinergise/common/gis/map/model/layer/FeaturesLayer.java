package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.util.naming.Identifiable;


public interface FeaturesLayer extends Identifiable {

	public FeaturesSource getFeaturesSource();

}
