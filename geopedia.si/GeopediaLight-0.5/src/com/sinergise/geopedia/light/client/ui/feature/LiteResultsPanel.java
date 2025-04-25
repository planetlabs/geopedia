package com.sinergise.geopedia.light.client.ui.feature;

import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.layers.MapLayers;
import com.sinergise.geopedia.client.ui.panels.results.FeatureResultPanel;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.light.client.ui.EntityDisplayerWidget;

public class LiteResultsPanel extends FeatureResultPanel {

	public LiteResultsPanel(MapLayers mapState) {
		super(mapState);
	}

	
	@Override
	public void themesSearchResults(Theme[] themes, boolean error,
			String errorMessage) {
		TitleGroupPanel fgp = new TitleGroupPanel(Messages.INSTANCE.ResultWidget_Themes());
		if (error) {
			fgp.setError(true, errorMessage);
			searchResults.add(fgp);
		}
		
		if (themes!=null&&themes.length>0) {
			for (Theme th:themes) {
				EntityDisplayerWidget tiw = EntityDisplayerWidget.createThemeWidget(th);
//				tiw.addButton(ButtonFactory.Global.createModifyPersonalGroupButton(th, PersonalGroup.FAVOURITE, false));
//				tiw.addButton(ButtonFactory.Global.createModifyPersonalGroupButton(th, PersonalGroup.PERSONAL, false));
				fgp.add(tiw);
			}
			searchResults.add(fgp);
		}
		
	}

	@Override
	public void tablesSearchResults(Table[] tables, boolean error,
			String errorMessage) {
		TitleGroupPanel fgp = new TitleGroupPanel(Messages.INSTANCE.ResultWidget_Layers());
		if (error) {
			fgp.setError(true, errorMessage);
			searchResults.add(fgp);
		}
		
		if (tables!=null&&tables.length>0) {
			for (Table tbl:tables) {
				EntityDisplayerWidget tiw = EntityDisplayerWidget.createTableWidget(tbl, mapState);
//				tiw.addButton(ButtonFactory.Global.createModifyPersonalGroupButton (tbl, PersonalGroup.FAVOURITE, false));
//				tiw.addButton(ButtonFactory.Global.createModifyPersonalGroupButton (tbl, PersonalGroup.PERSONAL, false));

				fgp.add(tiw);
			}
			searchResults.add(fgp);
		}
		
	}
}
