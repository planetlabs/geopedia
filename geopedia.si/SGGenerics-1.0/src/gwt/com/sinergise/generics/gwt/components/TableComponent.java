package com.sinergise.generics.gwt.components;

import java.util.HashMap;

import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.RemoteInspector;
import com.sinergise.generics.gwt.widgetbuilders.GenericWidgetFactory;
import com.sinergise.generics.gwt.widgetbuilders.TableWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.RemoteTableDataProvider;
import com.sinergise.generics.gwt.widgets.table.GenercsTable;
import com.sinergise.generics.gwt.widgets.table.TableRowSelectionHandler;

public class TableComponent {
	
	private GenercsTable genTable;	
	private RemoteTableDataProvider tdProvider;
	private TableRowSelectionHandler trSelectionHandler;
	
	
	public TableComponent(String widgetName, String datasourceName) {
		this(widgetName, datasourceName, new RemoteTableDataProvider(datasourceName));
	}
	
	public TableComponent(String widgetName, String datasourceName, RemoteTableDataProvider rDataProvider ) {
		genTable=new GenercsTable();
		tdProvider = rDataProvider;
		genTable.setDataProvider(tdProvider);
		genTable.addWidgetProcessor(tdProvider);
		genTable.setWidgetBuilder(new TableWidgetBuilder());
		
		trSelectionHandler = new TableRowSelectionHandler(genTable);
		genTable.addEventsHandler(trSelectionHandler);
		GenericWidgetFactory.buildWidget(
				genTable,
				new RemoteInspector(widgetName),
				new HashMap<String,String>()
				);
		
		genTable.addCreationListener(new CreationListener() {
			@Override
			public void creationCompleted(IsCreationProvider cp) {
				genTable.repaint();
			}
		});
		
	}
	
	public GenercsTable getWidget() {
		return genTable;
	}
	
	public RemoteTableDataProvider getDataProvider() {
		return tdProvider;
	}
	
	public TableRowSelectionHandler getSelectionHandler() {
		return trSelectionHandler;
	}
}
